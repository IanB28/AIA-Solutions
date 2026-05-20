package data.firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Business(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val currentTurn: Long = 0, // Contador para el control de la fila
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp? = null
)