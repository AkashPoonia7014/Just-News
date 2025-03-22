package com.sky.justnews.ui.bookmarks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sky.justnews.R
import com.sky.justnews.adapter.NewsAdapter
import com.sky.justnews.database.ArticleDatabase
import com.sky.justnews.databinding.FragmentBookmarksBinding
import com.sky.justnews.repository.NewsRepository
import com.sky.justnews.ui.NewsViewModel
import com.sky.justnews.ui.NewsViewModelProviderFactory
import com.sky.justnews.util.Constants
import com.sky.justnews.util.Resource


class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    private lateinit var binding: FragmentBookmarksBinding
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookmarksBinding.bind(view)


        val newsRepository = NewsRepository(ArticleDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(requireActivity().application, newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setupBookmarksRecycler()

        val itemTouchHelperCallback = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                newsViewModel.deleteArticle(article)

                Snackbar.make(view, "Removed from your Bookmarks", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        Toast.makeText(activity,"Added to your Bookmarks", Toast.LENGTH_SHORT).show()
                        newsViewModel.addToFavourite(article)
                    }
                    setActionTextColor(resources.getColor(R.color.colorPink))
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.bookmarksRecyclerView)
        }

        newsViewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer { articles ->
            newsAdapter.differ.submitList(articles)
        })
    }

    private fun setupBookmarksRecycler() {
        newsAdapter = NewsAdapter().apply {
            setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable("article", it)
                }
                findNavController().navigate(R.id.action_navigation_bookmarks_to_articleFragment, bundle)
            }
        }
        binding.bookmarksRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }
}