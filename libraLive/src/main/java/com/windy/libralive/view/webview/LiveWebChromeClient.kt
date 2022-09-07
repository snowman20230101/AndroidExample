package com.windy.libralive.view.webview

import android.webkit.WebChromeClient
import android.webkit.WebView

class LiveWebChromeClient : WebChromeClient() {
    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
    }
}