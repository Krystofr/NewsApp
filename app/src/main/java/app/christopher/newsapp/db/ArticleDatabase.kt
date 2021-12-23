package app.christopher.newsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.christopher.newsapp.models.Article

@Database(
    entities = [Article::class],
    version = 1
)

@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun getArticleDao(): ArticleDao

    //Create our actual Database
    companion object {
        //Create an instance of the Article Database
        @Volatile //Other Threads can immediately see when a thread changes 'instance' below.
        private var instance : ArticleDatabase? = null

        private val LOCK = Any() // makes sure there is only a single instance of our database at once
        //Called whenever we create an instance of our database
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            //No other Threads can access this block of code at the same time.
            //In other words, we make sure no other Threads alter this 'instance' while we already set it.
            instance ?: createDatabase(context).also { instance = it }

        }
        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "articles_db.db"
            ).build()
    }
}