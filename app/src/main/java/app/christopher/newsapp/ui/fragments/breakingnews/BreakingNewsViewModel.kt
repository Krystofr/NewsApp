package app.christopher.newsapp.ui.fragments.breakingnews

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
class BreakingNewsViewModel @Inject constructor(private val newsRepository: NewsRepository, newsApplication: Application)
    : AndroidViewModel(newsApplication) {

    //LiveData object for Breaking News
    val breakingNews: MutableLiveData<Resource<NewsResponse>> by lazy { MutableLiveData() }
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null

    init {
        getBreakingNews("gb")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)
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


    //Make safe breaking news call and safe search news call.
    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode,
                    breakingNewsPage) //We are making our network response
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("Well, this is awkward. Check your internet connection"))
                Toast.makeText(getApplication(),
                    "Well, this is awkward. Check your internet connection",
                    Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network failure!"))
                //Here we want to catch any Retrofit's conversion exception from JSON to our Kotlin objects.
                else -> breakingNews.postValue(Resource.Error("Conversion error!"))
            }
        } catch (e: HttpException) {
            Log.e("BreakingNews: ", e.localizedMessage!!)
        }
    }

    /* We need the ConnectivityManager here (A SYSTEM SERVICE) which requires the context of an Activity which we cannot call inside the viewModel here
      But again we cannot implement this function inside the Activity but rather in this viewModel class
      One SUPER BAD PRACTICE of solving this is passing the Activity context into the constructor of this ViewModel class
      Because that is the exact reason why we use the ViewModel => to separate the activity data logic from the UI.
      Also, using the activity context in the ViewModel and the when the activity gets destroyed, then you cannot simply use that context anymore

      SOLUTION:
      What we can do is use the applicationContext() because we know that it stays alive as long as our application does.
      We solve this by letting NewsViewModel inherit from AndroidViewModel(), rather than the original ViewModel()
      Then we need to create an Application class and pass it as parameter to the AndroidViewModel().
      */
    //This checks if user is connected to the internet or not
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