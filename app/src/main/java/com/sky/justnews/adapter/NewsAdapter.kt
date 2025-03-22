package com.sky.justnews.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sky.justnews.R
import com.sky.justnews.models.Article
import com.sky.justnews.models.Source

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    private lateinit var articleTitle: TextView
    private lateinit var articleSource: TextView
    private lateinit var articleDateTime: TextView
    private lateinit var articleDescription: TextView
    private lateinit var layoutNews: LinearLayout

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    private var onItemClickListener: ((Article) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {

        val article = differ.currentList[position]

        articleTitle = holder.itemView.findViewById(R.id.textArticleTitle)
        articleSource = holder.itemView.findViewById(R.id.textArticleSource)
        articleDateTime = holder.itemView.findViewById(R.id.textArticleDateTime)
        articleDescription = holder.itemView.findViewById(R.id.textArticleDescription)
        layoutNews = holder.itemView.findViewById(R.id.layoutNews)

        holder.itemView.apply {
            articleTitle.text = article.title
            articleSource.text = article.source?.name
            articleDateTime.text = article.publishedAt
            articleDescription.text = article.description

            setOnClickListener {
                onItemClickListener?.let {
                    it(article)
                }
            }
        }
    }

    fun setOnClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }
}