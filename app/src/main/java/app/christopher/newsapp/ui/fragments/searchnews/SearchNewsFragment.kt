package app.christopher.newsapp.ui.fragments.searchnews

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.christopher.newsapp.adapters.NewsAdapter
import app.christopher.newsapp.databinding.FragmentSearchNewsBinding
import app.christopher.newsapp.util.Constants
import app.christopher.newsapp.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import app.christopher.newsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

private const val TAG = "SearchNewsFragment"
class SearchNewsFragment : Fragment() {

   private val viewModel: SearchNewsViewModel by activityViewModels()
   private lateinit var newsAdapter: NewsAdapter
   private var binding : FragmentSearchNewsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchNewsBinding.inflate(layoutInflater, container,false)
        return binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchArticle()
        setUpRecyclerView()

        binding!!.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val job : Job? = null
                MainScope().launch {
                    delay(SEARCH_NEWS_TIME_DELAY)
                    if (text.toString().isNotEmpty()) { //We only want to search for news if there is text in the search bar
                        viewModel.searchNews(text.toString())
                        job!!.cancel()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun searchArticle() = binding!!.apply {
        //So here, we are subscribing to all changes that will be made in the LiveData observable
        viewModel.searchNews.observe(viewLifecycleOwner) { response ->
            //Depending on the state of the response, we want to handle each differently (success or error)
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    response.data?.let { newsResponse ->
                        if (newsResponse.articles.isNotEmpty()){
                            newsAdapter.differ.submitList(newsResponse.articles.toList()) //The "newsResponse.article" here did not load more items into the list when scrolling. Calling ".toList()" fixed this.
                            val totalPages =
                                newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2 //totalResults is an integer in the JSON that tells us how many results we have in our response.
                            isLastPage = viewModel.searchNewsPage == totalPages
                        } else Toast.makeText(requireContext(), "Oops! Something went wrong", Toast.LENGTH_SHORT).show()

                        if (isLastPage) {
                            rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressbar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                        view.let {
                            Snackbar.make(it!!, "Well, this is awkward: $message", Snackbar.LENGTH_LONG)
                            .apply {
                                setAction("OKAY") {
                                }
                                show()
                            } }
                    }
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }

        }
    }

    private fun hideProgressbar() = binding!!.apply{
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }
    private fun showProgressbar()= binding!!.apply{
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setUpRecyclerView() = binding!!.apply{
        newsAdapter = NewsAdapter(requireContext())
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
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
                totalItemCount >= Constants.QUERY_PAGE_SIZE //Determine each page of our request is 20 items

            //Now, we set pagination to our recyclerview of items.
            val shouldPaginate =
                isNotLoadingAndNotAtLastPage and isAtLastItem and isNotAtBeginning and isTotalMoreThanVisible and isScrolling
            if (shouldPaginate) {
                viewModel.searchNews(binding!!.etSearch.textAlignment.toString())
                isScrolling = false
            }
        }
    }
}