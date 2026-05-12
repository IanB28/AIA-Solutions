package data.firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Business(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    @get:PropertyName("createdAT")
    @set:PropertyName("createdAT")
    var createdAt: Timestamp? = null
)