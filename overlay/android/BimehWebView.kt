package ir.bimeh.bimeh_web

import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.webkit.*
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

// فقط این دامنه/IPها گواهی نامعتبر را می‌پذیرند؛ بقیه رد می‌شوند
private val trustedHosts = setOf(
    "agents.iraninsurance.ir", "co.iraninsurance.ir", "fire.iic.local",
    "192.168.10.17", "192.168.10.20", "192.168.10.97", "192.168.11.50",
    "192.168.16.138", "192.168.21.31", "192.168.21.32", "192.168.18.104"
)

class BimehWebView(context: Context, params: Map<String, Any>?) : PlatformView {
    private val web = WebView(context)
    private val user = params?.get("user") as? String ?: ""
    private val pass = params?.get("pass") as? String ?: ""
    private var authTries = 0

    companion object {
        var current: BimehWebView? = null
    }

    init {
        current = this
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = true
        web.settings.setSupportZoom(true)
        web.settings.builtInZoomControls = true
        web.settings.displayZoomControls = false
        web.webViewClient = object : WebViewClient() {
            override fun onReceivedHttpAuthRequest(v: WebView, h: HttpAuthHandler, host: String, realm: String) {
                if (authTries++ < 3) h.proceed(user, pass) else h.cancel()
            }
            override fun onReceivedSslError(v: WebView, h: SslErrorHandler, e: SslError) {
                val host = Uri.parse(e.url).host ?: ""
                if (host in trustedHosts) h.proceed() else h.cancel()
            }
        }
        val url = params?.get("url") as? String
        if (!url.isNullOrEmpty()) load(url)
    }

    fun load(url: String) {
        authTries = 0
        web.loadUrl(url)
    }

    fun clearAll() {
        web.loadUrl("about:blank")
        web.clearCache(true)
        web.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
    }

    override fun getView() = web

    override fun dispose() {
        web.destroy()
        if (current === this) current = null
    }
}

class BimehWebViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    @Suppress("UNCHECKED_CAST")
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView =
        BimehWebView(context, args as? Map<String, Any>)
}
