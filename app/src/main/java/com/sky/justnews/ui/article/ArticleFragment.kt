package com.sky.justnews.ui.article

import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.sky.justnews.MainActivity
import com.sky.justnews.R
import com.sky.justnews.database.ArticleDatabase
import com.sky.justnews.databinding.FragmentArticleBinding
import com.sky.justnews.databinding.FragmentBookmarksBinding
import com.sky.justnews.repository.NewsRepository
import com.sky.justnews.ui.NewsViewModel
import com.sky.justnews.ui.NewsViewModelProviderFactory
import com.sky.justnews.ui.home.HomeFragment

class ArticleFragment : Fragment(R.layout.fragment_article) {

    private lateinit var newsViewModel: NewsViewModel
    val args: ArticleFragmentArgs by navArgs()
    private lateinit var binding: FragmentArticleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)

        val newsRepository = NewsRepository(ArticleDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(requireActivity().application, newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        val article = args.article

        binding.webView.apply {
            webViewClient = WebViewClient()

            article.url?.let { nonNullUrl ->
                loadUrl(nonNullUrl)
            } ?: run {
                Toast.makeText(context, "Article URL is invalid", Toast.LENGTH_SHORT).show()
            }
        }



        /*
        // this is a alternative of the above web view which handles null url, anyways the below code also works fine now
        // after i have made all the variables of the article and source classes nullable

        binding.webView.apply {
            webViewClient = WebViewClient()
            article.url?.let {
                loadUrl(it)
            }
        }

        */


        // can use this to solve the problem number 4 which is a progress bar or something when i click on the article and it loads in a web view
        // so this can be used to achieve this
        binding.webView.setBackgroundColor(Color.parseColor("#111111"))


        binding.fabBookmark.setOnClickListener {
            newsViewModel.addToFavourite(article)

            Snackbar.make(view, "Added to Your Bookmarks", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    Toast.makeText(activity,"Removed from Your Bookmarks", Toast.LENGTH_SHORT).show()
                    newsViewModel.deleteArticle(article)
                }
                setActionTextColor(resources.getColor(R.color.colorPink))
                show()
            }

        }

//        val rootView = binding.mainLayout
//        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.setPadding(
//                0,  // Left
//                0,  // Status bar height (push content down)
//                0,  // Right
//                0   // Bottom (allow content under nav bar)
//            )
//            insets
//        }


        // RANDOM TEST OF GETTING BACKGROUND COLOR

        /*
        // NOT WORKING AS DESIRED AS OF NOW

        binding.webView.settings.javaScriptEnabled = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Run JavaScript after the page has fully loaded
                view?.evaluateJavascript(
                    "(function() { return window.getComputedStyle(document.body).backgroundColor; })();"
                ) { color ->
                    Toast.makeText(view.context, "Color: $color", Toast.LENGTH_SHORT).show()
                }
            }
        }
        */

    }

}