package app.christopher.newsapp.api

import app.christopher.newsapp.models.NewsResponse
import app.christopher.newsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    //Get the response data from the API
    @GET("v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String = "gb",
        @Query("page") pageNumber: Int = 1,
        @Query("apiKey") apiKey : String = API_KEY) : Response<NewsResponse>

    //Search response by query parameter
    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q") searchQuery: String,
        @Query("page") pageNumber: Int = 1,
        @Query("apiKey") apiKey : String = API_KEY) : Response<NewsResponse>
}