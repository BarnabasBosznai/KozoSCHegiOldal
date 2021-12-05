package hu.bme.aut.android.kozoschegioldal.model

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
data class User(
    val uid: String = "",
    val fcmToken: String = "",
    val photoUrl: String? = null
) : Parcelable