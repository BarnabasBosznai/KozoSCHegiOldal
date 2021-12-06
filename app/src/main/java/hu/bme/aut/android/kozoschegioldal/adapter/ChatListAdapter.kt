package hu.bme.aut.android.kozoschegioldal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.kozoschegioldal.databinding.ChatItemBinding
import hu.bme.aut.android.kozoschegioldal.model.Group

class ChatListAdapter(private var groupList: List<Group>, private val selectListener: ChatGroupSelectListener) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groupList[position]
        holder.binding.tvChatUserName.text = group.name
        holder.binding.root.setOnClickListener {
            selectListener.onSelect(group)
        }
    }

    override fun getItemCount(): Int = groupList.size

    class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)
    {}

    interface ChatGroupSelectListener {
        fun onSelect(group: Group)
    }
}