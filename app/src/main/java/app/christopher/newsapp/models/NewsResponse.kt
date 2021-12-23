package app.christopher.newsapp.models

import app.christopher.newsapp.models.Article


data class NewsResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)