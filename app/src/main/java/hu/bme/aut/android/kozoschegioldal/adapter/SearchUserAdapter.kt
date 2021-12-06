package hu.bme.aut.android.kozoschegioldal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.kozoschegioldal.databinding.GroupSearchResultBinding
import hu.bme.aut.android.kozoschegioldal.model.User

class SearchUserAdapter(val addedUsers: List<User>, val onItemClicked: OnItemClickedListener) : ListAdapter<User, SearchUserAdapter.ViewHolder>(Companion) {

    companion object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(val binding: GroupSearchResultBinding) : RecyclerView.ViewHolder(binding.root)
    {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(GroupSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)

        holder.binding.root.setOnClickListener {
            onItemClicked.onClick(user)
            if (holder.binding.viewAdded.visibility == View.VISIBLE)
                holder.binding.viewAdded.visibility = View.GONE
            else
                holder.binding.viewAdded.visibility = View.VISIBLE
        }

        holder.binding.tvChatUserName.text = user.displayName
        val idx = addedUsers.indexOfFirst { it.uid == user.uid }
        if (idx != -1)
            holder.binding.viewAdded.visibility = View.VISIBLE
        else
            holder.binding.viewAdded.visibility = View.GONE
    }

    interface OnItemClickedListener {
        fun onClick(user: User)
    }

}