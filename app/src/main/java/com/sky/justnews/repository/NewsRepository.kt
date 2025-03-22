package com.sky.justnews.repository

import com.sky.justnews.api.RetrofitInstance
import com.sky.justnews.database.ArticleDatabase
import com.sky.justnews.models.Article

class NewsRepository (val db: ArticleDatabase) {

    suspend fun getHeadlines(countryCode: String = "us", pageNumber: Int)  =
        RetrofitInstance.api.getHeadlines(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getFavouriteNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteNews(article: Article) = db.getArticleDao().deleteArticles(article)




}