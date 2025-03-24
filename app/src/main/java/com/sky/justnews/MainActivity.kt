package com.sky.justnews

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
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
            if (destination.id == R.id.articleFragment) {
                navView.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.container) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if(isGestureNavigationEnabled(this)) {
                view.setPadding(
                    0,  // Left
                    systemBars.top,  // Status bar height (push content down)
                    0,  // Right
                    0    // Bottom (allow content under nav bar)
                )
                insets
            } else {
                view.setPadding(
                    0,  // Left
                    systemBars.top,  // Status bar height (push content down)
                    0,  // Right
                    systemBars.bottom    // Bottom (allow content under nav bar)
                )
                insets
            }

        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.navView) { view, insets ->
            // val existingPaddingStart = view.paddingStart  // padding set in the xml

            // another approach for this is instead of getting padding from the xml, dynamically set padding
            val paddingStart = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -24.5f, view.resources.displayMetrics).toInt()

            view.setPadding(
                // existingPaddingStart,  // Left (first approach)
                paddingStart,  // second approach (more recommended)
                0,  // Status bar height (push content down)
                0,  // Right
                // -insets.systemGestureInsets.bottom   // Bottom (allow content under nav bar) (but this is deprecated) so alternative is
                - insets.getInsets(WindowInsetsCompat.Type.systemGestures()).bottom
            )
            insets
        }
    }

    private fun isGestureNavigationEnabled(context: Context): Boolean {
        return try {
            val mode = Settings.Secure.getInt(context.contentResolver, "navigation_mode")
            mode == 2  // 2 = Gesture Navigation  (id mode == 2 then the fun returns true)
        } catch (e: Settings.SettingNotFoundException) {
            false  // Default to false if setting is unavailable
        }
    }
}
