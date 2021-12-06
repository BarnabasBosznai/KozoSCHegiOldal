package hu.bme.aut.android.kozoschegioldal.viewmodel

import androidx.lifecycle.ViewModel
import hu.bme.aut.android.kozoschegioldal.model.Group
import hu.bme.aut.android.kozoschegioldal.model.Message
import hu.bme.aut.android.kozoschegioldal.model.User
import hu.bme.aut.android.kozoschegioldal.repository.ChatMessageListLiveData
import hu.bme.aut.android.kozoschegioldal.repository.ChatRepository

class ChatViewModel : ViewModel() {
    private val chatListRepository = ChatRepository()

    fun getChatListLiveData(groupId: String): ChatMessageListLiveData? {
        return chatListRepository.getChatListLiveData(groupId)
    }

    fun sendMessage(message: Message, group: Group) = chatListRepository.sendMessage(message, group)

    fun resetChatList() = chatListRepository.resetChatListQuery()

    fun getGroupsLiveData(user: User) = chatListRepository.getUserGroupsLiveData(user)

    fun getUsersTask() = chatListRepository.getUsers()

    fun createGroup(creator: String, groupName: String, users: MutableList<String>) = chatListRepository.createGroup(creator, groupName, users)

    interface ChatListRepository {
        fun getChatListLiveData(groupId: String): ChatMessageListLiveData?
    }
}