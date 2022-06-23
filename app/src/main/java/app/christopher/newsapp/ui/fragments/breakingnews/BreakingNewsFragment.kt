package app.christopher.newsapp.ui.fragments.breakingnews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.christopher.newsapp.adapters.NewsAdapter
import app.christopher.newsapp.databinding.FragmentBreakingNewsBinding
import app.christopher.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import app.christopher.newsapp.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "BreakingNewsFragment"

class BreakingNewsFragment : Fragment() {

    private val viewModel: BreakingNewsViewModel by activityViewModels()
    private lateinit var binding : FragmentBreakingNewsBinding
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBreakingNewsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBreakingNews()
        setUpSwipeRefresh()
        setUpRecyclerView()

    }

    private fun getBreakingNews() = binding.apply{
        //So here, we are subscribing to all changes that will be made in the LiveData observable
        viewModel.breakingNews.observe(viewLifecycleOwner) { response ->
            //Depending on the state of the response, we want to handle each differently (success or error)
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    swipeContainer.isRefreshing = false
                    response.data?.let { newsResponse ->
                        if (newsResponse.articles.isNotEmpty()) {
                            newsAdapter.differ.submitList(newsResponse.articles.toList()) //The "newsResponse.article" here did not load more items into the list when scrolling. Calling ".toList()" fixed this.
                            val totalPages =
                                newsResponse.totalResults / QUERY_PAGE_SIZE + 2 //totalResults is an integer in the JSON that tells us how many results we have in our response.
                            isLastPage = viewModel.breakingNewsPage == totalPages
                        } else Toast.makeText(activity, "An error occurred", Toast.LENGTH_SHORT).show()
                        if (isLastPage) {
                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressbar()
                    swipeContainer.isRefreshing = false
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                        //Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_SHORT).show()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error!")
                            .setMessage(message)
                            .setPositiveButton("okay") { _, _ ->
                            }.setNegativeButton("CANCEL") { cancel, _ ->
                                cancel.dismiss()
                            }
                            .show()
                    }
                }
                is Resource.Loading -> {
                    showProgressbar()
                    swipeContainer.isRefreshing = true
                }
            }

        }
    }

    private fun hideProgressbar() = binding.apply {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressbar() = binding.apply {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setUpRecyclerView() = binding.apply{
        newsAdapter = NewsAdapter(requireContext())
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }

    var isLoading = false
    var isLastPage = false

    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
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

            //Now, we set pagination to our recyclerview items.
            val shouldPaginate =
                isNotLoadingAndNotAtLastPage and isAtLastItem and isNotAtBeginning and isTotalMoreThanVisible and isScrolling
            if (shouldPaginate) {
                viewModel.getBreakingNews("gb")
                isScrolling = false
            }
        }
    }

    private fun setUpSwipeRefresh() = binding.apply {
        swipeContainer.apply {
            setOnRefreshListener {
                isRefreshing = true
                getBreakingNews()
            }
            setColorSchemeResources(
                android.R.color.holo_purple,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_green_light)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.swipeContainer.isRefreshing = true
    }

    override fun onResume() {
        super.onResume()
        binding.swipeContainer.isRefreshing = false
    }
}