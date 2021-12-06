package hu.bme.aut.android.kozoschegioldal.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import hu.bme.aut.android.kozoschegioldal.model.*
import hu.bme.aut.android.kozoschegioldal.notification.NotificationRetrofitInstance
import hu.bme.aut.android.kozoschegioldal.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class ChatRepository : ChatViewModel.ChatListRepository, ChatMessageListLiveData.OnLastMessageReachedCallback, ChatMessageListLiveData.OnLastVisibleMessageCallback {
    private var lastGroupId: String? = null
    private var lastVisibleMessage: DocumentSnapshot? = null
    private var isLastMessageReached: Boolean = false

    private var baseQuery: CollectionReference = Firebase.firestore.collection("messages")
    private var q: Query? = null

    override fun setLastVisibleMessage(lastVisibleMessage: DocumentSnapshot) {
        this.lastVisibleMessage = lastVisibleMessage
    }

    override fun setLastMessageReached(isLastMessage: Boolean) {
        this.isLastMessageReached = isLastMessage
    }

    override fun getChatListLiveData(groupId: String): ChatMessageListLiveData? {
        if (q == null) {
            lastGroupId = groupId
            q = baseQuery.document(groupId).collection("messages").orderBy("sentAt", Query.Direction.DESCENDING).limit(10)
        }

        if (isLastMessageReached)
            return null
        if (lastVisibleMessage != null)
            q = q!!.startAfter(lastVisibleMessage!!).limit(10)

        return ChatMessageListLiveData(q!!, this, this)
    }

    fun getUserGroupsLiveData(user: User): ChatGroupListLiveData {
        val query = Firebase.firestore.collection("groups").whereArrayContains("members", user.uid)
        return ChatGroupListLiveData(query)
    }

    fun sendMessage(message: Message, group: Group) {
        baseQuery.document(group.id).collection("messages").add(message)
        val data = NotificationData("Unread message", message.text)
        getUsers().addOnSuccessListener {
            it.toObjects(User::class.java).filter { u -> group.members.contains(u.uid) }.forEach { user ->
                if (user.uid != message.sentBy) {
                    Log.d("TOKEN", user.fcmToken)
                    sendNotification(PushNotification(data, user.fcmToken))
                }
            }
        }
    }

    fun resetChatListQuery() {
        lastVisibleMessage = null
        isLastMessageReached = false
        q = null
    }

    fun getUsers(): Task<QuerySnapshot> {
        return Firebase.firestore.collection("users").get()
    }

    fun createGroup(creator: String, groupName: String, users: MutableList<String>) {
        val newGroupDoc = Firebase.firestore.collection("groups").document()
        newGroupDoc.set(Group(newGroupDoc.id, groupName, users, 0, Date(), creator))
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = NotificationRetrofitInstance.api.postNotification(notification)
            /*if (response.isSuccessful) {
                Log.d("SendNotification", "Response: ${Gson().toJson(response.body())}")
            } else {
                Log.d("SendNotification", response.errorBody().toString())
            }*/
        } catch (e: Exception) {
            Log.d("SendNotification", e.toString())
        }
    }

}

data class Operation<T>(val message: T, val type: Int)

class ChatMessageListLiveData(
    private val query: Query,
    private val onLastVisibleMessageCallback: OnLastVisibleMessageCallback,
    private val onLastMessageReachedCallback: OnLastMessageReachedCallback)
    : LiveData<Operation<Message>>(), EventListener<QuerySnapshot> {

    private var listenerRegistration: ListenerRegistration? = null

    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null)
            return

        for (documentChange in value!!.documentChanges) {
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    val message = documentChange.document.toObject(Message::class.java)
                    val operation = Operation(message, 0)
                    setValue(operation)
                }
                DocumentChange.Type.MODIFIED -> {
                    val message = documentChange.document.toObject(Message::class.java)
                    val operation = Operation(message, 1)
                    setValue(operation)
                }
                DocumentChange.Type.REMOVED -> {
                    val message = documentChange.document.toObject(Message::class.java)
                    val operation = Operation(message, 2)
                    setValue(operation)
                }
            }
        }

        val querySnapshotSize = value.size()
        if (querySnapshotSize < 10) {
            onLastMessageReachedCallback.setLastMessageReached(true)
        } else {
            val lastVisibleMessage = value.documents.get(querySnapshotSize - 1)
            onLastVisibleMessageCallback.setLastVisibleMessage(lastVisibleMessage)
        }
    }

    override fun onActive() {
        listenerRegistration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        listenerRegistration!!.remove()
    }

    interface OnLastVisibleMessageCallback {
        fun setLastVisibleMessage(lastVisibleMessage: DocumentSnapshot)
    }

    interface OnLastMessageReachedCallback {
        fun setLastMessageReached(isLastMessage: Boolean)
    }
}

class ChatGroupListLiveData(private  val query: Query) : LiveData<Operation<Group>>(), EventListener<QuerySnapshot> {
    private var listenerRegistration: ListenerRegistration? = null

    override fun onEvent(snapshots: QuerySnapshot?, error: FirebaseFirestoreException?) {
        if (error != null)
            return

        for (documentChange in snapshots!!.documentChanges) {
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    setValue(Operation(documentChange.document.toObject(Group::class.java), 0))
                }
                DocumentChange.Type.MODIFIED -> {
                    setValue(Operation(documentChange.document.toObject(Group::class.java), 1))
                }
                DocumentChange.Type.REMOVED -> {
                    setValue(Operation(documentChange.document.toObject(Group::class.java), 2))
                }

            }
        }
    }

    override fun onActive() {
        super.onActive()
        listenerRegistration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()

        listenerRegistration?.also {
            it.remove()
            listenerRegistration = null
        }
    }
}