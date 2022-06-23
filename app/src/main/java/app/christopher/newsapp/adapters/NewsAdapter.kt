package app.christopher.newsapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.christopher.newsapp.databinding.ItemArticlePreviewBinding
import app.christopher.newsapp.models.Article
import app.christopher.newsapp.ui.fragments.ArticleActivity
import com.bumptech.glide.Glide

class NewsAdapter(val context: Context) : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticlePreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffUtilCallback by lazy {
        object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                // return oldItem.id == newItem.id
                //But instead we want to compare the url because we know each url is unique to each article
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }

        }
    }

    val differ = AsyncListDiffer(this, diffUtilCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(ItemArticlePreviewBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]

        holder.binding.apply {
            Glide.with(context).load(article.urlToImage).into(ivArticleImage)

            tvSource.text = article.source?.name
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt

            holder.itemView.setOnClickListener {
                val intent = Intent(context, ArticleActivity::class.java)
                intent.putExtra("url", differ.currentList[position].url)
                context.startActivity(intent)
                onItemClickListener?.let { it(article) }
            }
        }
    }

    private var onItemClickListener: ((Article) -> Unit)? = null

    override fun getItemCount() = differ.currentList.size

}