package app.christopher.newsapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.christopher.newsapp.NewsApplication
import app.christopher.newsapp.models.Article
import app.christopher.newsapp.models.NewsResponse
import app.christopher.newsapp.repository.NewsRepository
import app.christopher.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

/**
 * The ViewModel is responsible for calling the functions of our NewsRepository, and also handle the responses of our requests
 * We will have LiveData objects that will notify all Fragments about changes regarding these requests.
 */
class NewsViewModel(private val newsRepository: NewsRepository, newsApplication: Application) :
    AndroidViewModel(newsApplication) {

    //LiveData object for Breaking News
    val breakingNews: MutableLiveData<Resource<NewsResponse>> by lazy { MutableLiveData() }
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null

    //LiveData object for Search News
    val searchNews: MutableLiveData<Resource<NewsResponse>> by lazy { MutableLiveData() }
    var searchNewsPage = 1
    private var searchNewsResponse: NewsResponse? = null

    init {
        getBreakingNews("gb")
    }


    //viewModelScope ensures that the coroutine stays alive as long as our viewModel is alive^
    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++ //Increase current page number so that we can load the next page after that
                if (breakingNewsResponse == null) {//Since this is null initially, we want to set it when we get the first response
                    breakingNewsResponse = resultResponse
                } else { //In case it's not the first page
                    val oldArticles = breakingNewsResponse?.articles

                    /**
                     * Because this article List is not a Mutable List, we cannot add old articles to new articles
                     * So therefore we change the List to a MutableList in NewsResponse.kt
                     */
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
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
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

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                //Here, we are making our network response
                val response = newsRepository.searchNews(searchQuery,
                    searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("Well, this is awkward. Check your internet connection and try again"))
            }
        } catch (t: Throwable) {
            //Here we want to catch any Retrofit's conversion exception from JSON to our Kotlin objects.
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network failure!"))
                else -> searchNews.postValue(Resource.Error("Conversion error!"))
            }
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode,
                    breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("Well, this is awkward. Check your internet connection and restart"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network failure!"))
                else -> breakingNews.postValue(Resource.Error("Conversion error!"))
            }
        }
    }

    /* We need the ConnectivityManager here (A SYSTEM SERVICE) which requires the context of an Activity and we cannot call that inside the viewModel here
      But again we cannot implement this fun inside the Activity but rather in this viewModel class
      One SUPER BAD PRACTICE of solving this is passing the Activity context into the constructor of this ViewModel class
      Because that is the exact reason why we use the ViewModel => (Separation of Concerns) to separate the activity business logic from the UI.
      Also, using the activity context in the ViewModel and when the activity gets destroyed, then you cannot simply use that context anymore

      SOLUTION:
      What we can do is use the applicationContext() because we know that it stays alive as long as our application does.
      We solve this by letting NewsViewModel inherit from AndroidViewModel() class, rather than the original ViewModel() class
      Then we need to create an Application class and pass it as parameter to the AndroidViewModel().
      */
    //This method handles Network Connectivity.
    private fun hasInternetConnection(): Boolean {
        //getSystemService() returns an object of Any, so we need to cast it to a ConnectivityManager
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//Here we want to check if the device SDK is below Marshmallow -> Call requires this as minimum
            val activeNetwork = connectivityManager.activeNetwork
                ?: return false //Return false if there is no active network
            //Now we want to get the Network capabilities of the activeNetwork variable
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                ?: return false //Return false if no internet connection.

            //Check different kinds of network capabilities.
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        return false
    }
}