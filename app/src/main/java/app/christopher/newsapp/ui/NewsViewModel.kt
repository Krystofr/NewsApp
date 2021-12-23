package app.christopher.newsapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Query
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

    //LiveData object for Searching News
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1

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
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


}