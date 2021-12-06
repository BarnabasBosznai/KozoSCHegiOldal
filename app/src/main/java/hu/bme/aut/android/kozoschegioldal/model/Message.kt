package hu.bme.aut.android.kozoschegioldal.model

import java.util.*

data class Message(
    val text: String = "",
    val sentAt: Date = Date(),
    val sentBy: String = ""
)
