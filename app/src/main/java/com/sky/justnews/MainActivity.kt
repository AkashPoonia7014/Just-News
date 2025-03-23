package com.sky.justnews

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.sky.justnews.database.ArticleDatabase
import com.sky.justnews.databinding.ActivityMainBinding
import com.sky.justnews.repository.NewsRepository
import com.sky.justnews.ui.NewsViewModel
import com.sky.justnews.ui.NewsViewModelProviderFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var newsViewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val newsRepository = NewsRepository(ArticleDatabase(this))
//        val newsViewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
//        newsViewModel = ViewModelProvider(this, newsViewModelProviderFactory).get(NewsViewModel::class.java)


        val navView: BottomNavigationView = binding.navView
        navView.itemIconTintList = null
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_bookmarks
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.articleFragment) { // Replace with your fragment ID
                navView.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
            }
        }


//        val rootView = binding.container
//        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.setPadding(
//                0,  // Left
//                systemBars.top,  // Status bar height (push content down)
//                0,  // Right
//                0   // Bottom (allow content under nav bar)
//            )
//            insets
//        }
    }
}
