package hu.bme.aut.android.kozoschegioldal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import hu.bme.aut.android.kozoschegioldal.R
import hu.bme.aut.android.kozoschegioldal.databinding.ChatMessageReceiveBinding
import hu.bme.aut.android.kozoschegioldal.databinding.ChatMessageSendBinding
import hu.bme.aut.android.kozoschegioldal.model.Message
import hu.bme.aut.android.kozoschegioldal.model.User

class ChatMessageListAdapter(private var messageList: List<Message>, private val user: User) : RecyclerView.Adapter<BaseMessageViewHolder>() {
    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].sentBy == user.uid)
            R.layout.chat_message_send
        else
            R.layout.chat_message_receive
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMessageViewHolder {
        return when (viewType) {
            R.layout.chat_message_send -> BaseMessageViewHolder.MessageSendViewHolder(
                ChatMessageSendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            R.layout.chat_message_receive -> BaseMessageViewHolder.MessageReceiveViewHolder(
                ChatMessageReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalAccessException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseMessageViewHolder, position: Int) {
        when (holder) {
            is BaseMessageViewHolder.MessageSendViewHolder -> holder.bind(messageList[position])
            is BaseMessageViewHolder.MessageReceiveViewHolder -> holder.bind(messageList[position])
        }
    }
}

sealed class BaseMessageViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    class MessageReceiveViewHolder(private val binding: ChatMessageReceiveBinding) : BaseMessageViewHolder(binding) {
        fun bind(message: Message) {
            binding.tvMessageReceive.text = message.text
        }
    }

    class MessageSendViewHolder(private val binding: ChatMessageSendBinding) : BaseMessageViewHolder(binding) {
        fun bind(message: Message) {
            binding.tvMessageSend.text = message.text
        }
    }
}