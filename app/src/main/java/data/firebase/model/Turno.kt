package data.firebase.model

import com.google.firebase.Timestamp

data class Turno(
    val id: String = "",
    val number: Long = 0,
    val userId: String = "",
    val status: String = "waiting",
    val createdAt: Timestamp? = null
)

