package app.christopher.newsapp.repository

import app.christopher.newsapp.api.RetrofitInstance
import app.christopher.newsapp.db.ArticleDatabase

//This class is to get the data from our database and  our remote data source (Retrofit/API)
class NewsRepository(val db: ArticleDatabase) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    //Call the API search function
    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

}