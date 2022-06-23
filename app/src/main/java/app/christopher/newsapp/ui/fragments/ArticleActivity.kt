package app.christopher.newsapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import app.christopher.newsapp.R
import app.christopher.newsapp.databinding.ActivityArticleBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ArticleActivity : AppCompatActivity() {

    private var binding: ActivityArticleBinding? = null
    private var isAlreadyCreated = true
    //private val viewModel: NewsViewModel by viewModels()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        val intent = intent
        val url = intent.getStringExtra("url")
        binding?.webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding?.webViewProgressbar?.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding?.webViewProgressbar?.visibility = View.GONE
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                binding?.webViewProgressbar?.visibility = View.GONE
                showErrorMessage(getString(R.string.error_title), getString(R.string.error_message))
            }
        }

        binding?.webView!!.settings.apply {
            javaScriptCanOpenWindowsAutomatically = true
            javaScriptEnabled = true
            setGeolocationEnabled(true)
            setSupportZoom(true)
            setSupportMultipleWindows(true)
            if (url != null) {
                binding?.webView?.loadUrl(url)
            }
        }

        binding!!.shareIcon.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url.toString())
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Sending article to...")
            startActivity(shareIntent)
        }

       /* binding!!.fab.setOnClickListener {
            viewModel.saveArticle(url)
            Snackbar.make(it, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
        }*/
    }

    private fun showErrorMessage(title: String, message: String) {
        MaterialAlertDialogBuilder(this@ArticleActivity).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(false)
            setPositiveButton("RETRY") { _, _ ->
                this@ArticleActivity.recreate()
            }.setNegativeButton("CANCEL") { _, _ ->
                this@ArticleActivity.finish()
            }.setNeutralButton("SETTINGS") { _, _ ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }.show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectionManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectionManager.activeNetworkInfo
        return networkInfo !== null && networkInfo.isConnectedOrConnecting
    }

    override fun onResume() {
        super.onResume()
        if (isAlreadyCreated && !isNetworkAvailable()) {
            isAlreadyCreated = false
            showErrorMessage(getString(R.string.error_title), getString(R.string.error_message))

        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding!!.webView.canGoBack()) {
            binding!!.webView.goBack()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

}

