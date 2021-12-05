package hu.bme.aut.android.kozoschegioldal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.DocumentSnapshot
import hu.bme.aut.android.kozoschegioldal.R
import hu.bme.aut.android.kozoschegioldal.databinding.PostItem2Binding
import hu.bme.aut.android.kozoschegioldal.model.Post
import java.util.*
import java.util.concurrent.TimeUnit

class CustomFirestorePagingAdapter(private val fragment: Fragment, options: FirestorePagingOptions<Post>) : FirestorePagingAdapter<Post, CustomFirestorePagingAdapter.PostViewHolder>(options) {

    class PostViewHolder(val binding : PostItem2Binding) : RecyclerView.ViewHolder(binding.root)
    {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(PostItem2Binding.inflate(LayoutInflater.from(fragment.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        val post = docToPost(this.getItem(position)!!)

        if (post.imageUrl != null) {
            holder.binding.ivPostImage.layout(0,0,0,0)
            val circularProgressDrawable = CircularProgressDrawable(fragment.requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            holder.binding.ivPostImage.visibility = View.VISIBLE
            Glide.with(fragment).asBitmap().load(post.imageUrl).placeholder(circularProgressDrawable).into(holder.binding.ivPostImage)
        } else {
            holder.binding.ivPostImage.visibility = View.GONE
        }

        holder.binding.tvPostAuthor.text = post.author
        if (post.authorImageUrl != null) {
            holder.binding.ivPostAuthorImage.layout(0,0,0,0)
            Glide.with(fragment).load(post.authorImageUrl).into(holder.binding.ivPostAuthorImage)
        } else {
            holder.binding.ivPostAuthorImage.setImageResource(R.mipmap.ic_launcher)
        }

        if (post.text != null) {
            holder.binding.tvPostText.text = post.text
        } else {
            holder.binding.tvPostText.visibility = View.GONE
        }

        holder.binding.tvPostDate.text = dateFormat(post.date)

    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    private fun docToPost(snapshot: DocumentSnapshot): Post {
        return Post(
            id = snapshot.id,
            author = snapshot.getString("author")!!,
            date = snapshot.getDate("date")!!,
            authorImageUrl = snapshot.getString("authorImageUrl"),
            text = snapshot.getString("text"),
            imageUrl = snapshot.getString("imageUrl")
        )
    }

    private fun dateFormat(date: Date): String {
        val timeDiff = Date().time - date.time
        val seconds = (timeDiff / 1000).toInt()
        val minutes = (seconds / 60).toInt()
        val hours = (minutes / 60).toInt()
        val days = (hours / 24).toInt()

        return when {
            days >= 1 -> fragment.requireContext().resources.getQuantityString(R.plurals.days, days, days)
            hours >= 1 -> fragment.requireContext().resources.getQuantityString(R.plurals.hours, hours, hours)
            minutes >= 1 -> fragment.requireContext().resources.getQuantityString(R.plurals.minutes, minutes, minutes)
            else -> fragment.requireContext().resources.getQuantityString(R.plurals.seconds, seconds, seconds)
        }
    }
}