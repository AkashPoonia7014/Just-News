package com.sky.justnews.ui.article

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    }

}