package com.sky.justnews.ui.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sky.justnews.R
import com.sky.justnews.adapter.NewsAdapter
import com.sky.justnews.database.ArticleDatabase
import com.sky.justnews.databinding.FragmentHomeBinding
import com.sky.justnews.databinding.FragmentSearchBinding
import com.sky.justnews.repository.NewsRepository
import com.sky.justnews.ui.NewsViewModel
import com.sky.justnews.ui.NewsViewModelProviderFactory
import com.sky.justnews.util.Constants
import com.sky.justnews.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.sky.justnews.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var searchErrorLayout: LinearLayout
    private lateinit var tryAgainButton:Button
//    private var adapterEmpty:Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)


        searchErrorLayout = view.findViewById(R.id.searchErrorLayout)
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView: View = inflater.inflate(R.layout.layout_error, null)
        tryAgainButton = newView.findViewById(R.id.tryAgainButton)


        val newsRepository = NewsRepository(ArticleDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(requireActivity().application, newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setupSearchRecyclerView()

        var job: Job?= null
        binding.inputSearch.addTextChangedListener() { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable.let {
                    if(editable.toString().isNotEmpty()) {
                        newsViewModel.searchNews(editable.toString())
                    }
                }
            }
        }

        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
//                        {
//                            Toast.makeText(activity, "Updated Count125: ${newsAdapter.itemCount}", Toast.LENGTH_SHORT).show()
//                            if(newsAdapter.itemCount > 0)  adapterEmpty = false
//                            Log.d("count", "adapter.itemCount: ${newsAdapter.itemCount}")
//                            Log.d("count", "adapter Empty: $adapterEmpty")
//                        }

                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE +2
                        isLastPage = newsViewModel.searchNewsPage == totalPages
                        if (isLastPage) {
                            binding.searchRecyclerView.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity,"Error: $message", Toast.LENGTH_SHORT).show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }
        })


        // NOT WORKING
        tryAgainButton.setOnClickListener {
            if (binding.inputSearch.text.toString().isNotEmpty()) {
                newsViewModel.searchNews(binding.inputSearch.text.toString())

            } else {
                hideErrorMessage()
            }
        }

        binding.searchButton.setOnClickListener {
            hideKeyboardFrom(requireView())
        }

    }


    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private fun hideProgressBar() {
        binding.tryAgainProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.tryAgainProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        binding.searchErrorLayout.root.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        binding.searchErrorLayout.root.visibility = View.VISIBLE
//        if (isAdded) {
//            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
//        }
        isError = true
    }

    val scrollListener = object: RecyclerView.OnScrollListener() {

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
                newsViewModel.searchNews(binding.inputSearch.text.toString())
                isScrolling = false
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
                hideKeyboardFrom(requireView())
            }
        }
    }

    private fun setupSearchRecyclerView() {
        newsAdapter = NewsAdapter().apply {
            setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable("article", it)
                }
                findNavController().navigate(R.id.action_navigation_search_to_articleFragment, bundle)
            }
        }
        binding.searchRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireActivity())
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }

    private fun showKeyboardTo(view: View) {
        binding.inputSearch.requestFocus()
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboardFrom(view: View) {

        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        binding.inputSearch.clearFocus()

    }

    /*

    // the above method is more better so i am using that one instead of this
    private fun hideKeyboard() {
        requireActivity().currentFocus?.let {
            (requireContext().getSystemService(InputMethodManager::class.java))?.hideSoftInputFromWindow(it.windowToken, 0)
        }
        binding.inputSearch.clearFocus()
    }




    //  The above and the below code both are same the belo one more conventional and java and the above one is shorter and more kotlin

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        binding.inputSearch.clearFocus()
    }
    */


    // Do something so that keyboard does not open automatically when i am switching between fragments
    // Handled

    override fun onResume() {
        super.onResume()
        if (binding.inputSearch.text.toString().trim().isEmpty()) {
            showKeyboardTo(binding.inputSearch)
        } else {
            hideKeyboardFrom(requireView())
        }
    }

}