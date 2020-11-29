package io.horizontalsystems.bankwallet.modules.webview


import android.net.http.SslError
import android.os.Bundle
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import io.horizontalsystems.bankwallet.R
import kotlinx.android.synthetic.main.activity_web_view.*


class WebViewActivity : AppCompatActivity(){

    companion object {
        const val guideWebUrlKey = "urlKey"
        const val guideTitleKey = "titleKey"
        const val guideAddressKey = "addressKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_web_view)

        val guideUrl = intent.extras?.getString(guideWebUrlKey)
        val titleStr = intent.extras?.getString(guideTitleKey)



        val address =intent.extras?.getString(guideAddressKey)?:""

        //val viewModel by viewModels<GuideWebViewModel> { GuideWebModule.Factory(guideUrl) }

        initView()
        websiteView.loadUrl(guideUrl)

        setWebView()




    }

    override fun onBackPressed() {
        if (websiteView.canGoBack()){
            websiteView.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        websiteView?.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        websiteView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        websiteView?.onResume()
        super.onResume()
    }

    private fun initView(){
        val settings = websiteView.settings
        settings.javaScriptEnabled=true
        settings.domStorageEnabled = true

        //websiteView.settings.javaScriptEnabled = true
        websiteView.addJavascriptInterface(JsToJava(), "Android")
    }

    private fun setWebView(){
        websiteView.webViewClient= MyWebView()
        websiteView.webChromeClient=MyWebViewSec()
    }

    /**
     *js可以调用该类的方法
     */
    internal inner class JsToJava {
        //必须有该注解
        @JavascriptInterface
        fun show() {
            //toast("我被js调用了啦")
            println("我被js调用了啦")
        }
    }

    private inner class MyWebView : WebViewClient(){

        override fun onPageFinished(view: WebView?, url: String?) {
            //super.onPageFinished(view, url)
            //kotlin调用js
            //JS定义String变量的时候用单引号，而JAVA是使用双引号。
            //var json = "kotlin调用js"
            //mWebView.loadUrl("javascript:showMessage('$json')")
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
            view.loadUrl(null)
            return true//super.shouldOverrideUrlLoading(view, request)
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError?) {
            //super.onReceivedSslError(view, handler, error)
            handler.proceed()
        }
    }

    private class MyWebViewSec : WebChromeClient(){
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }
    }


}