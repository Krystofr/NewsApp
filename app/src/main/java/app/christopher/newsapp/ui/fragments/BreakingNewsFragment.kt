package app.christopher.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.christopher.newsapp.R
import app.christopher.newsapp.adapters.NewsAdapter
import app.christopher.newsapp.ui.NewsActivity
import app.christopher.newsapp.ui.NewsViewModel
import app.christopher.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*

private const val TAG = "BreakingNewsFragment"
class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            (activity as NewsActivity).viewModel //Cast to NewsActivity so we have access to the ViewModel created in it by calling ".viewModel"

        setUpRecyclerView()

        //So here, we are subscribing to all changes that will be made in the LiveData observable
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            //Depending on the state of the response, we want to handle each differently (success or error)
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)
                    }
                }
                is Resource.Error -> {
                    hideProgressbar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }

        })
    }

    private fun hideProgressbar() {
        paginationProgressBar.visibility = View.INVISIBLE
    }
    private fun showProgressbar(){
        paginationProgressBar.visibility = View.VISIBLE
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        }
    }
}