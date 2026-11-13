package ir.bimeh.bimeh_web

import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(engine: FlutterEngine) {
        super.configureFlutterEngine(engine)
        engine.platformViewsController.registry
            .registerViewFactory("bimeh/webview", BimehWebViewFactory())

        MethodChannel(engine.dartExecutor.binaryMessenger, "bimeh/web")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "setProxy" -> {
                        val hostPort = call.argument<String>("hostPort") ?: ""
                        result.success(setProxy(hostPort))
                    }
                    "load" -> {
                        BimehWebView.current?.load(call.argument<String>("url") ?: "")
                        result.success(null)
                    }
                    "clear" -> {
                        BimehWebView.current?.clearAll()
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
    }

    private fun setProxy(hostPort: String): Boolean {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) return false
        val cfg = ProxyConfig.Builder().addProxyRule(hostPort).build()
        ProxyController.getInstance().setProxyOverride(cfg, ContextCompat.getMainExecutor(this)) {}
        return true
    }
}
