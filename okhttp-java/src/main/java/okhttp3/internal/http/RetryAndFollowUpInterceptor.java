/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3.internal.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;

import javax.annotation.Nullable;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Internal;
import okhttp3.internal.connection.Exchange;
import okhttp3.internal.connection.RouteException;
import okhttp3.internal.connection.Transmitter;
import okhttp3.internal.http2.ConnectionShutdownException;

import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_PROXY_AUTH;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static okhttp3.internal.Util.closeQuietly;
import static okhttp3.internal.Util.sameConnection;
import static okhttp3.internal.http.StatusLine.HTTP_PERM_REDIRECT;
import static okhttp3.internal.http.StatusLine.HTTP_TEMP_REDIRECT;

/**
 * This interceptor recovers from failures and follows redirects as necessary. It may throw an
 * {@link IOException} if the call was canceled.
 */
public final class RetryAndFollowUpInterceptor implements Interceptor {
    /**
     * How many redirects and auth challenges should we attempt? Chrome follows 21 redirects; Firefox,
     * curl, and wget follow 20; Safari follows 16; and HTTP/1.0 recommends 5.
     */
    private static final int MAX_FOLLOW_UPS = 20;

    private final OkHttpClient client;

    public RetryAndFollowUpInterceptor(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Transmitter transmitter = realChain.transmitter();

        int followUpCount = 0;
        Response priorResponse = null;
        while (true) {
            // TODO 创建 ExchangeFinder 预备连接
            transmitter.prepareToConnect(request);

            if (transmitter.isCanceled()) {
                throw new IOException("Canceled");
            }

            Response response;
            boolean success = false;
            try {
                response = realChain.proceed(request, transmitter, null);
                success = true;
            } catch (RouteException e) {
                // TODO 路由异常，连接为成功，请求还没发出去
                // The attempt to connect via a route failed. The request will not have been sent.
                if (!recover(e.getLastConnectException(), transmitter, false, request)) {
                    throw e.getFirstConnectException();
                }
                continue;
            } catch (IOException e) {
                // An attempt to communicate with a server failed. The request may have been sent.
                // TODO 请求发出去了，但是和服务器通信失败了。(socket流正在读写数据的时候断开连接)
                // TODO HTTP2才会抛出 ConnectionShutdownException 所以对于HTTP1 requestSendStarted一定是true
                boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
                if (!recover(e, transmitter, requestSendStarted, request)) throw e;
                continue;
            } finally {
                // The network call threw an exception. Release any resources.
                if (!success) {
                    transmitter.exchangeDoneDueToException();
                }
            }

            // Attach the prior response if it exists. Such responses never have a body.
            if (priorResponse != null) {
                response = response.newBuilder()
                        .priorResponse(priorResponse.newBuilder()
                                .body(null)
                                .build())
                        .build();
            }

            Exchange exchange = Internal.instance.exchange(response);
            Route route = exchange != null ? exchange.connection().route() : null;

            // TODO 重定向
            Request followUp = followUpRequest(response, route);

            if (followUp == null) {
                if (exchange != null && exchange.isDuplex()) {
                    transmitter.timeoutEarlyExit();
                }
                return response;
            }

            RequestBody followUpBody = followUp.body();
            if (followUpBody != null && followUpBody.isOneShot()) {
                return response;
            }

            closeQuietly(response.body());
            if (transmitter.hasExchange()) {
                exchange.detachWithViolence();
            }

            if (++followUpCount > MAX_FOLLOW_UPS) {
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }

            request = followUp;
            priorResponse = response;
        }
    }

    /**
     * Report and attempt to recover from a failure to communicate with a server. Returns true if
     * {@code e} is recoverable, or false if the failure is permanent. Requests with a body can only
     * be recovered if the body is buffered or if the failure occurred before the request has been
     * sent.
     */
    private boolean recover(IOException e, Transmitter transmitter,
                            boolean requestSendStarted, Request userRequest) {
        // The application layer has forbidden retries.
        // TODO 1、在配置OkhttpClient是设置了不允许重试（默认允许），则一旦发生请求失败就不再重试
        if (!client.retryOnConnectionFailure()) return false;

        //TODO 2、如果是RouteException，不用管这个条件，
        // 如果是IOException，由于requestSendStarted只在http2的io异常中可能为false，所以主要是第二个条件
        // We can't send the request body again.
        if (requestSendStarted && requestIsOneShot(e, userRequest)) return false;

        // TODO 3、判断是不是属于重试的异常
        // This exception is fatal.
        if (!isRecoverable(e, requestSendStarted)) return false;

        // TODO 4、有没有可以用来连接的路由路线
        // No more routes to attempt.
        if (!transmitter.canRetry()) return false;

        // For failure recovery, use the same route selector with a new connection.
        return true;
    }

    private boolean requestIsOneShot(IOException e, Request userRequest) {
        RequestBody requestBody = userRequest.body();
        return (requestBody != null && requestBody.isOneShot())
                || e instanceof FileNotFoundException;
    }

    private boolean isRecoverable(IOException e, boolean requestSendStarted) {
        // If there was a protocol problem, don't recover.
        if (e instanceof ProtocolException) {
            return false;
        }

        // If there was an interruption don't recover, but if there was a timeout connecting to a route
        // we should try the next route (if there is one).
        if (e instanceof InterruptedIOException) {
            return e instanceof SocketTimeoutException && !requestSendStarted;
        }

        // Look for known client-side or negotiation errors that are unlikely to be fixed by trying
        // again with a different route.
        // TODO SSL握手异常中，证书出现问题，不能重试
        if (e instanceof SSLHandshakeException) {
            // If the problem was a CertificateException from the X509TrustManager,
            // do not retry.
            if (e.getCause() instanceof CertificateException) {
                return false;
            }
        }
        // TODO SSL握手未授权异常 不能重试
        if (e instanceof SSLPeerUnverifiedException) {
            // e.g. a certificate pinning error.
            return false;
        }

        // An example of one we might want to retry with a different route is a problem connecting to a
        // proxy and would manifest as a standard IOException. Unless it is one we know we should not
        // retry, we return true and try a new route.
        return true;
    }

    /**
     * Figures out the HTTP request to make in response to receiving {@code userResponse}. This will
     * either add authentication headers, follow redirects or handle a client request timeout. If a
     * follow-up is either unnecessary or not applicable, this returns null.
     */
    private Request followUpRequest(Response userResponse, @Nullable Route route) throws IOException {
        if (userResponse == null) throw new IllegalStateException();
        int responseCode = userResponse.code();

        final String method = userResponse.request().method();
        switch (responseCode) {
            // TODO 407 客户端使用了HTTP代理服务器，在请求头中添加 “Proxy-Authorization”，让代理服务器授权
            case HTTP_PROXY_AUTH:
                Proxy selectedProxy = route != null
                        ? route.proxy()
                        : client.proxy();
                if (selectedProxy.type() != Proxy.Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                return client.proxyAuthenticator().authenticate(route, userResponse);

            // TODO 401 需要身份验证 有些服务器接口需要验证使用者身份 在请求头中添加 “Authorization"
            case HTTP_UNAUTHORIZED:
                return client.authenticator().authenticate(route, userResponse);

            //TODO 308 永久重定向
            //     307 临时重定向
            case HTTP_PERM_REDIRECT:
            case HTTP_TEMP_REDIRECT:
                // "If the 307 or 308 status code is received in response to a request other than GET
                // or HEAD, the user agent MUST NOT automatically redirect the request"
                //TODO 如果请求方式不是GET或者HEAD，框架不会自动重定向请求
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    return null;
                }
                // fall-through
            case HTTP_MULT_CHOICE:
            case HTTP_MOVED_PERM:
            case HTTP_MOVED_TEMP:
            case HTTP_SEE_OTHER:
                // Does the client allow redirects?
                if (!client.followRedirects()) return null;

                String location = userResponse.header("Location");
                if (location == null) return null;
                // TODO 根据location 配置新的请求 url
                HttpUrl url = userResponse.request().url().resolve(location);

                // Don't follow redirects to unsupported protocols.
                if (url == null) return null;

                // If configured, don't follow redirects between SSL and non-SSL.
                // TODO 如果重定向在http到https之间切换，需要检查用户是不是允许(默认允许)
                boolean sameScheme = url.scheme().equals(userResponse.request().url().scheme());
                if (!sameScheme && !client.followSslRedirects()) return null;

                // Most redirects don't include a request body.
                Request.Builder requestBuilder = userResponse.request().newBuilder();
                /***
                 * 重定向请求中 只要不是 PROPFIND 请求，无论是POST还是其他的方法都要改为GET请求方式，
                 * 即只有 PROPFIND 请求才能有请求体
                 */

                // 请求不是get与head
                if (HttpMethod.permitsRequestBody(method)) {
                    final boolean maintainBody = HttpMethod.redirectsWithBody(method);

                    // TODO 除了 PROPFIND 请求之外都改成GET请求 propfind
                    if (HttpMethod.redirectsToGet(method)) {
                        requestBuilder.method("GET", null);
                    } else {
                        RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
                        requestBuilder.method(method, requestBody);
                    }

                    // TODO 不是 PROPFIND 的请求，把请求头中关于请求体的数据删掉
                    if (!maintainBody) {
                        requestBuilder.removeHeader("Transfer-Encoding");
                        requestBuilder.removeHeader("Content-Length");
                        requestBuilder.removeHeader("Content-Type");
                    }
                }

                // When redirecting across hosts, drop all authentication headers. This
                // is potentially annoying to the application layer since they have no
                // way to retain them.
                // TODO 在跨主机重定向时，删除身份验证请求头
                if (!sameConnection(userResponse.request().url(), url)) {
                    requestBuilder.removeHeader("Authorization");
                }

                return requestBuilder.url(url).build();

            case HTTP_CLIENT_TIMEOUT:
                // 408's are rare in practice, but some servers like HAProxy use this response code. The
                // spec says that we may repeat the request without modifications. Modern browsers also
                // repeat the request (even non-idempotent ones.)
                // TODO 408 算是连接失败了，所以判断用户是不是允许重试
                if (!client.retryOnConnectionFailure()) {
                    // The application layer has directed us not to retry the request.
                    return null;
                }
                // TODO UnrepeatableRequestBody 实际并没发现有其他地方用到
                RequestBody requestBody = userResponse.request().body();
                if (requestBody != null && requestBody.isOneShot()) {
                    return null;
                }

                // TODO 如果是本身这次的响应就是重新请求的产物同时上一次之所以重请求还是因为408，那我们这次不再重请求 了
                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == HTTP_CLIENT_TIMEOUT) {
                    // We attempted to retry and got another timeout. Give up.
                    return null;
                }

                // TODO 如果服务器告诉我们了 Retry-After 多久后重试，那框架不管了。
                if (retryAfter(userResponse, 0) > 0) {
                    return null;
                }

                return userResponse.request();

            // TODO 503 服务不可用 和408差不多，但是只在服务器告诉你 Retry-After：0（意思就是立即重试） 才重请求
            case HTTP_UNAVAILABLE:
                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == HTTP_UNAVAILABLE) {
                    // We attempted to retry and got another timeout. Give up.
                    return null;
                }

                if (retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
                    // specifically received an instruction to retry without delay
                    return userResponse.request();
                }

                return null;

            default:
                return null;
        }
    }

    private int retryAfter(Response userResponse, int defaultDelay) {
        String header = userResponse.header("Retry-After");

        if (header == null) {
            return defaultDelay;
        }

        // https://tools.ietf.org/html/rfc7231#section-7.1.3
        // currently ignores a HTTP-date, and assumes any non int 0 is a delay
        if (header.matches("\\d+")) {
            return Integer.valueOf(header);
        }

        return Integer.MAX_VALUE;
    }
}
