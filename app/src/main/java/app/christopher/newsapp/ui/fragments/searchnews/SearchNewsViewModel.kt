package app.christopher.newsapp.ui.fragments.searchnews

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.christopher.newsapp.NewsApplication
import app.christopher.newsapp.models.NewsResponse
import app.christopher.newsapp.repository.NewsRepository
import app.christopher.newsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    newsApplication: Application,
) : AndroidViewModel(newsApplication) {


    //LiveData object for Searching News
    val searchNews: MutableLiveData<Resource<NewsResponse>> by lazy { MutableLiveData() }
    var searchNewsPage = 1
    var search_text: String? = null
    private var searchNewsResponse: NewsResponse? = null

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
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

    //Make safe breaking news call and safe search news call.
    private suspend fun safeSearchNewsCall(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery,
                    searchNewsPage) //We are making our network response
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("Well, this is awkward. Check your internet connection"))
                Toast.makeText(getApplication(),
                    "Well, this is awkward. Check your internet connection",
                    Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network failure!"))
                //Here we want to catch any Retrofit's conversion exception from JSON to our Kotlin objects.
                else -> searchNews.postValue(Resource.Error("Conversion error!"))
            }
        } catch (ex: HttpException) {
            Log.e("VM: SearchNews", "Error fetching articles: ${ex.localizedMessage}")
        }
    }
    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService( //getSystemService() returns an object of Any, so we need to cast it to a ConnectivityManager
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
                ?: return false //Return false if there is no active network
            //Now we want to get the Network capabilities of the activeNetwork variable
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                ?: return false //Return false if no internet connection.
            //Check different kinds of network capabilities.
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else { //Here we want to check if the device SDK is below Marshmallow
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}