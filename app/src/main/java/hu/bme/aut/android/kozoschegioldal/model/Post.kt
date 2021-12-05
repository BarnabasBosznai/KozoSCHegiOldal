package hu.bme.aut.android.kozoschegioldal.model

import java.util.*

data class Post(
    val id: String = "",
    val author: String = "",
    val date: Date = Date(),
    val authorImageUrl: String? = null,
    val text: String? = null,
    var imageUrl: String? = null
    )
