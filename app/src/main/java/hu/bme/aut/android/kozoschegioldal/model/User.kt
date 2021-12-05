package hu.bme.aut.android.kozoschegioldal.model

data class User(
    val uid: String,
    val fcmToken: String,
    val photoUrl: String? = null
)