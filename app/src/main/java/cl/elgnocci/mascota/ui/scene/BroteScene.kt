package cl.elgnocci.mascota.ui.scene

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import cl.elgnocci.mascota.data.TodayStatus

/** Recuerda si la página ya cargó y cuántas hojas tenía Brote, para animar la caída. */
private class SceneBridge {
    var ready = false
    var lastLeaves: Int? = null
    var pendingJson: String? = null
}

/** El balcón de Brote en 3D: la escena three.js de tools/scene dentro de una WebView, sin internet. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BroteScene(status: TodayStatus, modifier: Modifier = Modifier) {
    val bridge = remember { SceneBridge() }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                        Log.d("BroteScene", "${message.message()} (${message.sourceId()}:${message.lineNumber()})")
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        bridge.ready = true
                        bridge.pendingJson?.let { view.evaluateJavascript("Brote.setState($it)", null) }
                    }
                }
                loadUrl("file:///android_asset/app.html")
            }
        },
        update = { view ->
            val json = status.toSceneJson()
            val previous = bridge.lastLeaves
            bridge.lastLeaves = status.leaves
            if (!bridge.ready) {
                bridge.pendingJson = json
                return@AndroidView
            }
            if (previous != null && status.leaves < previous) view.evaluateJavascript("Brote.dropLeaf()", null)
            view.evaluateJavascript("Brote.setState($json)", null)
        },
    )
}
