package data.firebase.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import data.firebase.model.Business

class FirestoreService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 🔥 Crear turno con número automático usando subcolecciones internas
    fun tomarTurno(businessId: String, onResult: (Long?) -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            Log.e("Firestore", "Usuario no autenticado")
            onResult(null)
            return
        }

        val businessRef = db.collection("businesses").document(businessId)
        // Usamos el UID del usuario como ID del documento para validar por ID directo en la transacción
        val turnRef = businessRef.collection("turns").document(user.uid)

        db.runTransaction { transaction ->
            val businessSnapshot = transaction.get(businessRef)
            val turnSnapshot = transaction.get(turnRef)

            // 1. Verificar si el usuario ya tiene un turno activo en este negocio
            if (turnSnapshot.exists() && turnSnapshot.getString("status") == "waiting") {
                return@runTransaction turnSnapshot.getLong("number")
            }

            // 2. Obtener el turno actual desde el documento del negocio
            val currentTurn = businessSnapshot.getLong("currentTurn") ?: 0
            val nextTurn = currentTurn + 1

            // 3. Actualizar el contador en el negocio
            transaction.update(businessRef, "currentTurn", nextTurn)

            // 4. Crear el turno en la subcolección
            val turno = hashMapOf(
                "number" to nextTurn,
                "userId" to user.uid,
                "status" to "waiting",
                "createdAt" to FieldValue.serverTimestamp()
            )
            transaction.set(turnRef, turno)

            nextTurn
        }.addOnSuccessListener { turnNumber ->
            Log.d("Firestore", "Turno asignado con éxito: $turnNumber")
            onResult(turnNumber as Long?)
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al tomar turno en la transacción", e)
            onResult(null)
        }
    }

    // 🔍 Obtener turnos en espera (ordenados por número)
    fun escucharTurnos(businessId: String, onUpdate: (List<Map<String, Any>>) -> Unit) {
        db.collection("businesses")
            .document(businessId)
            .collection("turns")
            .whereEqualTo("status", "waiting")
            .orderBy("number")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error escuchando turnos", e)
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.map { it.data ?: emptyMap() } ?: emptyList()
                onUpdate(lista)
            }
    }

    // 👤 Obtener turno del usuario actual de manera directa
    fun obtenerMiTurno(businessId: String, onResult: (Long?) -> Unit) {
        val user = auth.currentUser ?: return onResult(null)

        db.collection("businesses")
            .document(businessId)
            .collection("turns")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getString("status") == "waiting") {
                    val number = document.getLong("number")
                    onResult(number)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // ▶️ Avanzar turno (Lógica para la sección de Administración)
    fun siguienteTurno(businessId: String) {
        db.collection("businesses")
            .document(businessId)
            .collection("turns")
            .whereEqualTo("status", "waiting")
            .orderBy("number")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0].reference
                    doc.update("status", "done")
                }
            }
    }

    // 🔥 Escuchar negocios activos en tiempo real
    fun escucharNegocios(onResult: (List<Business>) -> Unit) {
        db.collection("businesses")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error al obtener negocios", e)
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    val business = doc.toObject(Business::class.java)
                    business?.copy(id = doc.id)
                } ?: emptyList()

                onResult(lista)
            }
    }
}