package org.depparse.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.InflateException
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler

/**
 * Web activity
 *
 * @author Bernard Bou
 */
class WebActivity : AppCompatActivity() {

    private var url: String? = null
    private var usesJavaScript = false
    private var local = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        try {
            setContentView(R.layout.activity_web)
        } catch (e: InflateException) {
            Toast.makeText(this, "Needs android WebView", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }

        // unmarshal args
        val params = intent.extras!!
        url = params.getString(ARG_URL)
        usesJavaScript = params.getBoolean(ARG_USES_JAVASCRIPT)
        local = params.getBoolean(ARG_LOCAL)

        // web view
        val webview = findViewById<WebView>(R.id.webView)
        webview.clearCache(true)
        webview.clearHistory()
        if (usesJavaScript) {
            webview.settings.javaScriptEnabled = true
            webview.settings.javaScriptCanOpenWindowsAutomatically = true
        }
        val assetLoader = if (local) WebViewAssetLoader.Builder().addPathHandler("/assets/", AssetsPathHandler(this)).build() else null
        //if (local)
        //{
        //webview.getSettings().setAllowFileAccessFromFileURLs(true);
        //webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        //}
        webview.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated")
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Log.e(TAG, "$failingUrl:$description,$errorCode")
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                Log.e(TAG, error.description.toString() + ',' + error.errorCode)
            }

            @Deprecated("Deprecated")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return false
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                val uriStr = uri.toString()
                view.loadUrl(uriStr)
                return false
            }

            @Deprecated("Deprecated in Java")
            override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
                return if (assetLoader != null) {
                    assetLoader.shouldInterceptRequest(url.toUri())
                } else {
                    @Suppress("DEPRECATION")
                    super.shouldInterceptRequest(view, url)
                }
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return if (assetLoader != null) {
                    assetLoader.shouldInterceptRequest(request.url)
                } else super.shouldInterceptRequest(view, request)
            }
        }
        if (url != null) {
            webview.loadUrl(url!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.web, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }

            R.id.help -> {
                tryStart(this, "file:///android_asset/help/en/index.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.reference -> {
                tryStart(this, "file:///android_asset/reference/index.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.reference_uds -> {
                tryStart(this, "file:///android_asset/reference/uds.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.reference_online -> {
                tryStart(this, "https://universaldependencies.org/u/dep/all.html", usesJavaScript = true, local = false)
                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {

        private const val TAG = "Web activity"
        const val ARG_URL = "url"
        const val ARG_USES_JAVASCRIPT = "uses_javascript"
        const val ARG_LOCAL = "local"

        // S T A R T A C T I V I T Y

        fun tryStart(activity: Activity, url: String, usesJavaScript: Boolean, local: Boolean) {
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra(ARG_URL, url)
            intent.putExtra(ARG_USES_JAVASCRIPT, usesJavaScript)
            intent.putExtra(ARG_LOCAL, local)
            activity.startActivity(intent)
        }
    }
}
