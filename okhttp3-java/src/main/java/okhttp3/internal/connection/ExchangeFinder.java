/*
 * Copyright (C) 2015 Square, Inc.
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
package okhttp3.internal.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import okhttp3.Address;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.http.ExchangeCodec;

import static okhttp3.internal.Util.closeQuietly;

import android.util.Log;

/**
 * Attempts to find the connections for a sequence of exchanges. This uses the following strategies:
 *
 * <ol>
 *   <li>If the current call already has a connection that can satisfy the request it is used.
 *       Using the same connection for an initial exchange and its follow-ups may improve locality.
 *
 *   <li>If there is a connection in the pool that can satisfy the request it is used. Note that
 *       it is possible for shared exchanges to make requests to different host names! See {@link
 *       RealConnection#isEligible} for details.
 *
 *   <li>If there's no existing connection, make a list of routes (which may require blocking DNS
 *       lookups) and attempt a new connection them. When failures occur, retries iterate the list
 *       of available routes.
 * </ol>
 *
 * <p>If the pool gains an eligible connection while DNS, TCP, or TLS work is in flight, this finder
 * will prefer pooled connections. Only pooled HTTP/2 connections are used for such de-duplication.
 *
 * <p>It is possible to cancel the finding process.
 */
final class ExchangeFinder {
    private final Transmitter transmitter;
    private final Address address;
    private final RealConnectionPool connectionPool;
    private final Call call;
    private final EventListener eventListener;

    private RouteSelector.Selection routeSelection;

    // State guarded by connectionPool.
    private final RouteSelector routeSelector;
    private RealConnection connectingConnection;
    private boolean hasStreamFailure;
    private Route nextRouteToTry;

    ExchangeFinder(Transmitter transmitter, RealConnectionPool connectionPool, Address address, Call call, EventListener eventListener) {
        Log.d(OkHttpClient.TAG, "ExchangeFinder created....");

        this.transmitter = transmitter;
        this.connectionPool = connectionPool;
        this.address = address;
        this.call = call;
        this.eventListener = eventListener;
        this.routeSelector = new RouteSelector(address, connectionPool.routeDatabase, call, eventListener);
    }

    public ExchangeCodec find(OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        int connectTimeout = chain.connectTimeoutMillis();
        int readTimeout = chain.readTimeoutMillis();
        int writeTimeout = chain.writeTimeoutMillis();
        int pingIntervalMillis = client.pingIntervalMillis();
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();

        try {
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);

            // TODO 这里是关键
            return resultConnection.newCodec(client, chain);
        } catch (RouteException e) {
            trackFailure();
            throw e;
        } catch (IOException e) {
            trackFailure();
            throw new RouteException(e);
        }
    }

    /**
     * Finds a connection and returns it if it is healthy. If it is unhealthy the process is repeated
     * until a healthy connection is found.
     */
    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                                                 int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                                                 boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled);

            // If this is a brand new connection, we can skip the extensive health checks. 是新连接 且不是HTTP2.0 就不用体检
            synchronized (connectionPool) {
                if (candidate.successCount == 0 && !candidate.isMultiplexed()) {
                    return candidate;
                }
            }

            // Do a (potentially slow) check to confirm that the pooled connection is still good. If it
            // isn't, take it out of the pool and start again.
            if (!candidate.isHealthy(doExtensiveHealthChecks)) {
                candidate.noNewExchanges();
                continue;
            }

            return candidate;
        }
    }

    /**
     * Returns a connection to host a new stream. This prefers the existing connection if it exists,
     * then the pool, finally building a new connection.
     */
    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
        boolean foundPooledConnection = false;
        RealConnection result = null;
        Route selectedRoute = null;
        RealConnection releasedConnection;
        Socket toClose;
        synchronized (connectionPool) {
            if (transmitter.isCanceled()) throw new IOException("Canceled");
            hasStreamFailure = false; // This is a fresh attempt.

            // Attempt to use an already-allocated connection. We need to be careful here because our
            // already-allocated connection may have been restricted from creating new exchanges.
            releasedConnection = transmitter.connection;
            toClose = transmitter.connection != null && transmitter.connection.noNewExchanges
                    ? transmitter.releaseConnectionNoEvents()
                    : null;

            if (transmitter.connection != null) {
                // We had an already-allocated connection and it's good.
                result = transmitter.connection;
                releasedConnection = null;
            }

            if (result == null) {
                // Attempt to get a connection from the pool.
                // 1, 第一次尝试从缓冲池里面获取RealConnection(Socket的包装类)
                if (connectionPool.transmitterAcquirePooledConnection(address, transmitter, null, false)) {
                    foundPooledConnection = true;
                    result = transmitter.connection;
                } else if (nextRouteToTry != null) {
                    // 2, 如果缓冲池中没有，则看看有没有下一个Route可以尝试，这里只有重试的情况会走进来
                    selectedRoute = nextRouteToTry;
                    nextRouteToTry = null;
                } else if (retryCurrentRoute()) {
                    // 3, 如果已经设置了使用当前Route重试，那么会继续使用当前的Route
                    selectedRoute = transmitter.connection.route();
                }
            }
        }
        closeQuietly(toClose);

        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
        }
        if (result != null) {
            // If we found an already-allocated or pooled connection, we're done.
            // 4，如果前面发现ConnectionPool或者transmiter中有可以复用的Connection，这里就直接返回了
            return result;
        }

        // If we need a route selection, make one. This is a blocking operation.
        // 5, 如果前面没有获取到Connection，这里就需要通过routeSelector来获取到新的Route来进行Connection的建立
        boolean newRouteSelection = false;
        if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
            newRouteSelection = true;

            // 6，获取route的过程其实就是DNS获取到域名IP的过程，这是一个阻塞的过程，会等待DNS结果返回
            routeSelection = routeSelector.next();
        }

        List<Route> routes = null;
        synchronized (connectionPool) {
            if (transmitter.isCanceled()) throw new IOException("Canceled");

            if (newRouteSelection) {
                // Now that we have a set of IP addresses, make another attempt at getting a connection from
                // the pool. This could match due to connection coalescing.
                routes = routeSelection.getAll();

                // 7，前面如果通过routeSelector拿到新的Route，其实就是相当于拿到一批新的IP，这里会再次尝试从ConnectionPool
                // 中检查是否有可以复用的Connection
                if (connectionPool.transmitterAcquirePooledConnection(address, transmitter, routes, false)) {
                    foundPooledConnection = true;
                    result = transmitter.connection;
                }
            }

            if (!foundPooledConnection) {
                if (selectedRoute == null) {
                    // 8，前面我们拿到的是一批IP，这里通过routeSelection获取到其中一个IP，Route是proxy和InetAddress的包装类
                    selectedRoute = routeSelection.next();
                }

                // Create a connection and assign it to this allocation immediately. This makes it possible
                // for an asynchronous cancel() to interrupt the handshake we're about to do.
                Log.d(OkHttpClient.TAG, "findConnection: new RealConnection()");
                // 9，用新的route创建RealConnection，注意这里还没有尝试连接
                result = new RealConnection(connectionPool, selectedRoute);
                connectingConnection = result;
            }
        }

        // If we found a pooled connection on the 2nd time around, we're done.
        // 10，注释说得很清楚，如果第二次从connectionPool获取到Connection可以直接返回了
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
            return result;
        }

        // TODO 这里牛皮。。。进行TCP + TLS握手。这是一个阻塞操作。
        // 11，原文注释的很清楚，这里是进行TCP + TLS连接的地方
        // Do TCP + TLS handshakes. This is a blocking operation.
        result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled, call, eventListener);

        connectionPool.routeDatabase.connected(result.route());

        // 后面一段是将连接成功的RealConnection放到ConnectionPool里面
        Socket socket = null;
        synchronized (connectionPool) {
            connectingConnection = null;
            // Last attempt at connection coalescing, which only occurs if we attempted multiple
            // concurrent connections to the same host.
            if (connectionPool.transmitterAcquirePooledConnection(address, transmitter, routes, true)) {
                // We lost the race! Close the connection we created and return the pooled connection.
                result.noNewExchanges = true;
                socket = result.socket();
                result = transmitter.connection;

                // It's possible for us to obtain a coalesced connection that is immediately unhealthy. In
                // that case we will retry the route we just successfully connected with.
                nextRouteToTry = selectedRoute;
            } else {
                connectionPool.put(result);
                transmitter.acquireConnectionNoEvents(result);
            }
        }
        closeQuietly(socket);

        eventListener.connectionAcquired(call, result);
        return result;
    }

    RealConnection connectingConnection() {
        assert (Thread.holdsLock(connectionPool));
        return connectingConnection;
    }

    void trackFailure() {
        assert (!Thread.holdsLock(connectionPool));
        synchronized (connectionPool) {
            hasStreamFailure = true; // Permit retries.
        }
    }

    /**
     * Returns true if there is a failure that retrying might fix.
     */
    boolean hasStreamFailure() {
        synchronized (connectionPool) {
            return hasStreamFailure;
        }
    }

    /**
     * Returns true if a current route is still good or if there are routes we haven't tried yet.
     */
    boolean hasRouteToTry() {
        synchronized (connectionPool) {
            if (nextRouteToTry != null) {
                return true;
            }
            if (retryCurrentRoute()) {
                // Lock in the route because retryCurrentRoute() is racy and we don't want to call it twice.
                nextRouteToTry = transmitter.connection.route();
                return true;
            }
            return (routeSelection != null && routeSelection.hasNext())
                    || routeSelector.hasNext();
        }
    }

    /**
     * Return true if the route used for the current connection should be retried, even if the
     * connection itself is unhealthy. The biggest gotcha here is that we shouldn't reuse routes from
     * coalesced connections.
     */
    private boolean retryCurrentRoute() {
        return transmitter.connection != null
                && transmitter.connection.routeFailureCount == 0
                && Util.sameConnection(transmitter.connection.route().address().url(), address.url());
    }
}
