package app.christopher.newsapp.repository

import app.christopher.newsapp.api.NewsApi
import app.christopher.newsapp.api.RetrofitInstance
import javax.inject.Inject

//This class is to get the data from our database and  our remote data source (Retrofit/API)
class NewsRepository @Inject constructor(private val api: NewsApi) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        api.getBreakingNews(countryCode, pageNumber)

    //Call the API search function
    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        api.searchForNews(searchQuery, pageNumber)

}