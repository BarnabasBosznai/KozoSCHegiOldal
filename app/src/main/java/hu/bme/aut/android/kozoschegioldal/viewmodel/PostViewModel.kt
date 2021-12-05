package hu.bme.aut.android.kozoschegioldal.viewmodel

import androidx.lifecycle.*
import hu.bme.aut.android.kozoschegioldal.interfaces.Callback
import hu.bme.aut.android.kozoschegioldal.repository.PostRepository

class PostViewModel : ViewModel() {
    private val postRepository: PostRepository = PostRepository()

    fun getPostsAdapterOptions(lifecycleOwner: LifecycleOwner) = postRepository.getPostsAdapterOptions(lifecycleOwner)

    fun createPost(callback: Callback, postText: String? = null, postImageInBytes: ByteArray? = null)
        = postRepository.createPost(callback, postText, postImageInBytes)
}