package app.christopher.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.christopher.newsapp.R
import app.christopher.newsapp.adapters.NewsAdapter
import app.christopher.newsapp.databinding.FragmentBreakingNewsBinding
import app.christopher.newsapp.ui.NewsActivity
import app.christopher.newsapp.viewmodel.NewsViewModel
import app.christopher.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import app.christopher.newsapp.util.Resource
import com.google.android.material.snackbar.Snackbar

private const val TAG = "BreakingNewsFragment"

class BreakingNewsFragment : Fragment() {

    private var binding: FragmentBreakingNewsBinding? = null
    private var viewModel: NewsViewModel? = null
    private lateinit var newsAdapter: NewsAdapter
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBreakingNewsBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            (activity as NewsActivity).viewModel
        setUpRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_breakingNewsFragment_to_articleFragment, bundle)
        }

        getBreakingNews()
    }

    private fun hideProgressbar() = binding!!.apply {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressbar() = binding!!.apply {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setUpRecyclerView() = binding!!.apply {
        newsAdapter = NewsAdapter(requireActivity())
        rvBreakingNews.apply {
            setHasFixedSize(true)
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }

    //Handle pagination in recyclerview on scrolling
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotAtLastPage = !isLoading and !isLastPage
            val isAtLastItem =
                firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning =
                firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible =
                totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate =
                isNotLoadingAndNotAtLastPage and isAtLastItem and isNotAtBeginning and isTotalMoreThanVisible and isScrolling
            if (shouldPaginate) {
                viewModel?.getBreakingNews("gb")
                isScrolling = false
            }
        }
    }

    //Listen for incoming news requests: Success, Error or Loading
    private fun getBreakingNews() = binding!!.apply{
        viewModel?.breakingNews?.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages =
                            newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel?.breakingNewsPage == totalPages
                        if (isLastPage) {
                            rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressbar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
                    }
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        getBreakingNews()
    }
}