package com.sky.justnews.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sky.justnews.MainActivity
import com.sky.justnews.R
import com.sky.justnews.adapter.NewsAdapter
import com.sky.justnews.database.ArticleDatabase
import com.sky.justnews.databinding.FragmentArticleBinding
import com.sky.justnews.databinding.FragmentHomeBinding
import com.sky.justnews.repository.NewsRepository
import com.sky.justnews.ui.NewsViewModel
import com.sky.justnews.ui.NewsViewModelProviderFactory
import com.sky.justnews.util.Constants
import com.sky.justnews.util.Resource

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var headlinesErrorLayout: LinearLayout
    private lateinit var tryAgainButton:Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        headlinesErrorLayout = view.findViewById(R.id.headlinesErrorLayout)
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView: View = inflater.inflate(R.layout.layout_error, null)
        tryAgainButton = newView.findViewById(R.id.tryAgainButton)


        val newsRepository = NewsRepository(ArticleDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(requireActivity().application, newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setupHeadlinesRecyclerView()
        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE +2
                        isLastPage = newsViewModel.headlinesPage == totalPages
                        if (isLastPage) {
                            binding.headlinesRecyclerView.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity,"Error: $message",Toast.LENGTH_SHORT).show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })

        tryAgainButton.setOnClickListener {
            newsViewModel.getHeadlines()
        }


    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

//    var mSqueezebarAnimator: ValueAnimator? = null

    private fun hideProgressBar() {
        binding.tryAgainProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.tryAgainProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        binding.headlinesErrorLayout.root.visibility = View.INVISIBLE
        isError = false
    }

    /*

    private fun showErrorMessage(message: String) {
        itemHeadlinesError.visibility = View.VISIBLE
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
        isError = true
    }
*/

    private fun showErrorMessage(message: String) {
        binding.headlinesErrorLayout.root.visibility = View.VISIBLE
//        if (isAdded) {
//            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
//        }
        isError = true
    }

    val scrollListener = object:RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoError = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoError && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                newsViewModel.getHeadlines()
                isScrolling = false
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    /*

    private fun setupHeadlinesRecyclerView() {
        newsAdapter = NewsAdapter()

        //the below two codes both do the same thing, 1 st one being the traditional java approach while the second one is kotlin way of doing it

//        val recyclerView: RecyclerView = binding.headlinesRecyclerView
//        recyclerView.adapter = newsAdapter
//        recyclerView.layoutManager = LinearLayoutManager(activity)
//        recyclerView.addOnScrollListener(this@HomeFragment.scrollListener)


        binding.headlinesRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireActivity())
            addOnScrollListener(this@HomeFragment.scrollListener)
        }

    }
*/

    private fun setupHeadlinesRecyclerView() {
        newsAdapter = NewsAdapter().apply {
            setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable("article", it)
                }
                findNavController().navigate(R.id.action_navigation_home_to_articleFragment, bundle)
            }
        }
        binding.headlinesRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireActivity())
            addOnScrollListener(this@HomeFragment.scrollListener)
        }
    }

}