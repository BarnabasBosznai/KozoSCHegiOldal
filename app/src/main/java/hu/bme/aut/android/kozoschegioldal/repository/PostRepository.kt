package hu.bme.aut.android.kozoschegioldal.repository

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingConfig
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import hu.bme.aut.android.kozoschegioldal.interfaces.Callback
import hu.bme.aut.android.kozoschegioldal.model.Post
import java.net.URLEncoder
import java.util.*

class PostRepository {
    private val storageRef = Firebase.storage.reference

    private val baseQuery = Firebase.firestore.collection("posts").orderBy("date", Query.Direction.DESCENDING)
    private val config = PagingConfig(5,1, false)

    fun getPostsAdapterOptions(lifecycleOwner: LifecycleOwner): FirestorePagingOptions<Post> {
        return FirestorePagingOptions.Builder<Post>()
            .setLifecycleOwner(lifecycleOwner)
            .setQuery(baseQuery, config, Post::class.java)
            .build()
    }

    fun createPost(callback: Callback, postText: String? = null, postImageInBytes: ByteArray? = null): Boolean {
        var success = false
        val postRef = Firebase.firestore.collection("posts").document()
        val post = Post(postRef.id, Firebase.auth.currentUser?.displayName!!, Date(), null, postText)

        if (postImageInBytes != null) {
            val imageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
            val imageRef = storageRef.child("images/$imageName")

            imageRef.putBytes(postImageInBytes)
                .addOnFailureListener {
                    success = false
                }
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { uri ->
                    success = true
                    post.imageUrl = uri.toString()

                    postRef.set(post)
                    //postsRef.add(post)
                        .addOnSuccessListener {
                            success = true
                            callback.onSuccess()
                        }
                        .addOnFailureListener {
                            success = false
                            callback.onFailure(it)
                            imageRef.delete()
                            postRef.delete()
                        }
                }
        } else {
            postRef.set(post)
                .addOnSuccessListener {
                    success = true
                    callback.onSuccess()
                }
                .addOnFailureListener {
                    success = false
                    callback.onFailure(it)
                    Log.d("POSTERROR", it.message ?: "no message")
                    postRef.delete()
                }
        }

        return success
    }
}