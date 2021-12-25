package app.christopher.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.christopher.newsapp.R
import app.christopher.newsapp.adapters.NewsAdapter
import app.christopher.newsapp.ui.NewsActivity
import app.christopher.newsapp.ui.NewsViewModel
import app.christopher.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import app.christopher.newsapp.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_breaking_news.*

private const val TAG = "BreakingNewsFragment"

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        breakingNewsRL.apply {
            setOnRefreshListener {
                swipeRefreshLayout()
            }
        }

        viewModel =
            (activity as NewsActivity).viewModel //Cast to NewsActivity so we have access to the ViewModel created in it by calling ".viewModel"
        setUpRecyclerView()


        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_breakingNewsFragment_to_articleFragment,
                bundle)
        }

        //So here, we are subscribing to all changes that will be made in the LiveData observable
        viewModel.breakingNews.observe(viewLifecycleOwner, { response ->
            //Depending on the state of the response, we want to handle each differently (success or error)
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList()) //The "newsResponse.article" here did not load more items into the list when scrolling. Calling ".toList()" fixed this.
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2 //totalResults is an integer in the JSON that tells us how many results we have in our response.
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if (isLastPage) {
                            rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressbar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                        //Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_SHORT).show()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Well, this is awkward:")
                            .setMessage(message)
                            .setPositiveButton("TRY AGAIN") { _, _ ->
                               swipeRefreshLayout()
                            }.setNegativeButton("CANCEL") { cancel, _ ->
                                cancel.dismiss()
                            }
                            .show()
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
        isLoading = false
    }

    private fun showProgressbar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }

    var isLoading = false
    var isLastPage = false

    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            //If we are scrolling
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            //Check when we've scrolled to the bottom or not.
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotAtLastPage = !isLoading and !isLastPage
            val isAtLastItem =
                firstVisibleItemPosition + visibleItemCount >= totalItemCount //Then we know our last item is visible.
            val isNotAtBeginning =
                firstVisibleItemPosition >= 0 //Determine if we have scrolled down such that the 1st item is not visible.
            val isTotalMoreThanVisible =
                totalItemCount >= QUERY_PAGE_SIZE //Determine each page of our request is 20 items

            //Now, we set pagination to our recyclerview of items.
            val shouldPaginate =
                isNotLoadingAndNotAtLastPage and isAtLastItem and isNotAtBeginning and isTotalMoreThanVisible and isScrolling
            if (shouldPaginate) {
                viewModel.getBreakingNews("uk")
                isScrolling = false
            }
        }
    }

    private fun swipeRefreshLayout() {
        findNavController().navigate(R.id.action_breakingNewsFragment_self)
        Toast.makeText(activity, "Refreshed", Toast.LENGTH_SHORT).show()
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}