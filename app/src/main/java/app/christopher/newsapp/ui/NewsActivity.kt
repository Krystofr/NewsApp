package app.christopher.newsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import app.christopher.newsapp.R
import app.christopher.newsapp.databinding.ActivityNewsBinding
import app.christopher.newsapp.db.ArticleDatabase
import app.christopher.newsapp.repository.NewsRepository
import app.christopher.newsapp.viewmodel.NewsViewModel
import app.christopher.newsapp.viewmodel.NewsViewModelProviderFactory

class NewsActivity : AppCompatActivity() {

    var viewModel: NewsViewModel? = null
    private lateinit var binding: ActivityNewsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Instantiate our NewsRepository
        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]

        //Set up NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        NavigationUI.setupActionBarWithNavController(this, navHostFragment.findNavController())
    }
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.newsNavHostFragment), null)
    }

}