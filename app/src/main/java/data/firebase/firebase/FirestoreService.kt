package data.firebase.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import data.firebase.model.Business

class FirestoreService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 🔥 Crear turno con número automático (SIN DUPLICADOS)
    fun tomarTurno(queueId: String, onResult: (Long?) -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            Log.e("Firestore", "Usuario no autenticado")
            onResult(null)
            return
        }

        val queueRef = db.collection("queues").document(queueId)

        db.runTransaction { transaction ->

            val queueSnapshot = transaction.get(queueRef)

            // Obtener turno actual
            val currentTurn = queueSnapshot.getLong("currentTurn") ?: 0

            // Verificar si el usuario ya tiene turno activo
            val existingTurns = queueRef.collection("turns")
                .whereEqualTo("userId", user.uid)
                .whereEqualTo("status", "waiting")

            val existingSnapshot = existingTurns.get().result

            if (existingSnapshot != null && !existingSnapshot.isEmpty) {
                // Ya tiene turno
                val existingNumber = existingSnapshot.documents[0].getLong("number")
                return@runTransaction existingNumber
            }

            val nextTurn = currentTurn + 1

            // Actualizar contador
            transaction.update(queueRef, "currentTurn", nextTurn)

            // Crear turno
            val turno = hashMapOf(
                "number" to nextTurn,
                "userId" to user.uid,
                "status" to "waiting",
                "createdAt" to FieldValue.serverTimestamp()
            )

            val turnRef = queueRef.collection("turns").document()
            transaction.set(turnRef, turno)

            nextTurn

        }.addOnSuccessListener { turnNumber ->
            Log.d("Firestore", "Turno asignado: $turnNumber")
            onResult(turnNumber)
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al tomar turno", e)
            onResult(null)
        }
    }

    // 🔍 Obtener turnos en espera (ordenados)
    fun escucharTurnos(queueId: String, onUpdate: (List<Map<String, Any>>) -> Unit) {
        db.collection("queues")
            .document(queueId)
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

    // 👤 Obtener turno del usuario actual
    fun obtenerMiTurno(queueId: String, onResult: (Long?) -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            onResult(null)
            return
        }

        db.collection("queues")
            .document(queueId)
            .collection("turns")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("status", "waiting")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val number = snapshot.documents[0].getLong("number")
                    onResult(number)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // ▶️ Avanzar turno (para admin)
    fun siguienteTurno(queueId: String) {
        val queueRef = db.collection("queues").document(queueId)

        queueRef.collection("turns")
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

    // 🔥 Escuchar negocios en tiempo real
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