package com.windy.libralive.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.windy.libralive.base.view.BaseActivity

class WebViewActivity : BaseActivity() {

    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(applicationContext)


        webView?.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // KEYCODE_SOFT_LEFT KEYCODE_SOFT_RIGHT
        if (keyCode == KeyEvent.KEYCODE_BACK && webView?.canGoBack() == true) {
            webView?.goBack()
            return true
        }

        return super.onKeyDown(keyCode, event)

    }


    override fun onDestroy() {
        webView?.loadDataWithBaseURL(null, "", "text/html", " utf -8", null)
        webView?.clearHistory()
        (webView?.parent as ViewGroup).removeView(webView)
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

}