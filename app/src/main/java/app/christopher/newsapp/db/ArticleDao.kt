package app.christopher.newsapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import app.christopher.newsapp.models.Article

@Dao
interface ArticleDao {

    //onConflict strategy determines if an article already exists in the Database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    //Inserts a new article in the database, and or updates or replaces it if already present.
    suspend fun upsert(article: Article): Long

    @Query("SELECT * FROM articles")
    fun getAllArticles() : LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}