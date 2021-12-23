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

    //This fun will return all available articles in our database
    @Query("SELECT * FROM articles")
    //Not a Suspend fun because it will return a LiveData object, which doesn't work with suspend functions
    //LiveData will notify all its observers that subscribed to changes from it, whenever an article inside the List changes.
    fun getAllArticles() : LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}