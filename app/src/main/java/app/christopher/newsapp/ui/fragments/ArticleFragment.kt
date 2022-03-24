package app.christopher.newsapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import app.christopher.newsapp.R
import app.christopher.newsapp.databinding.FragmentArticleBinding
import app.christopher.newsapp.ui.NewsActivity
import app.christopher.newsapp.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class ArticleFragment : Fragment(R.layout.fragment_article) {

    private var viewModel: NewsViewModel? = null
    private val args: ArticleFragmentArgs by navArgs()
    private lateinit var binding: FragmentArticleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArticleBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            (activity as NewsActivity).viewModel

        val article = args.article
        binding.apply {

            webView.webViewClient = WebViewClient()
            article.url?.let {
                webView.loadUrl(it)
            }
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.settings.javaScriptEnabled = true
            webView.settings.setGeolocationEnabled(true)
            webView.settings.setSupportZoom(true)
            webView.settings.setSupportMultipleWindows(true)
        }

        handleBackPressed()
        binding.fab.setOnClickListener {
            viewModel?.saveArticle(article)
            Snackbar.make(view, "Article saved to favorites", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun handleBackPressed() = binding.apply {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else if (webView.canGoForward()) {
                        webView.goForward()
                    }
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }
}