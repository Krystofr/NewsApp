package app.christopher.newsapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import app.christopher.newsapp.models.Article
import app.christopher.newsapp.models.NewsResponse
import app.christopher.newsapp.repository.NewsRepository
import app.christopher.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

//Here, we will call the functions of our NewsRepository, and also handle the responses of our requests
//We will have LiveData objects that will notify all Fragments about changes regarding these requests.
class NewsViewModel(
    val newsRepository: NewsRepository,
) : ViewModel() {

    //LiveData object for Breaking News
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse : NewsResponse? = null

    //LiveData object for Searching News
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse : NewsResponse? = null

    init {
        getBreakingNews("uk")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        //^viewModelScope ensures that the coroutine stays alive as long as our viewModel is alive^
        breakingNews.postValue(Resource.Loading())
        val response = newsRepository.getBreakingNews(countryCode,
            breakingNewsPage) //We are making our network response
        breakingNews.postValue(handleBreakingNewsResponse(response))
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        //Here we want to post to our search news LiveData
        searchNews.postValue(Resource.Loading())
        val response = newsRepository.searchNews(searchQuery, searchNewsPage)
        searchNews.postValue(handleSearchNewsResponse(response))
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                //Increase current page number so that we can load the next page after that
                breakingNewsPage++
                //Since this is null initially, we want to set it when we get the first response
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse //resultResponse -> response from our API.
                } else { //In case it's not the first page
                    val oldArticles = breakingNewsResponse?.articles
                    //Because this article List is not a Mutable List, we cannot add old articles to new articles
                    //So therefore we change the List to a MutableList in NewsResponse.kt
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                //Increase current page number so that we can load the next page after that
                searchNewsPage++
                //Since this is null initially, we want to set it when we get the first response
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse //resultResponse -> response from our API.
                } else { //In case it's not the first page
                    val oldArticles = searchNewsResponse?.articles
                    //Because this article List is not a Mutable List, we cannot add old articles to new articles
                    //So therefore we change the List to a MutableList in NewsResponse.kt
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
}