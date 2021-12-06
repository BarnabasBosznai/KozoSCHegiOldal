package hu.bme.aut.android.kozoschegioldal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Group(
    val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val type: Int = 0,
    val createdAt: Date = Date(),
    val createdBy: String = ""
) : Parcelable
