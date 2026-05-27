package data.firebase.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import data.firebase.model.Business

class FirestoreService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 1. Crear turno de forma segura sin transacciones para evitar crasheos en el emulador
    fun tomarTurno(businessId: String, onResult: (Long?) -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            Log.e("Firestore", "Usuario no autenticado")
            onResult(null)
            return
        }

        val businessRef = db.collection("businesses").document(businessId)
        val turnRef = businessRef.collection("turns").document(user.uid)

        turnRef.get().addOnSuccessListener { turnSnapshot ->
            if (turnSnapshot.exists() && turnSnapshot.getString("status") == "waiting") {
                val numeroExistente = turnSnapshot.getLong("number")
                onResult(numeroExistente)
                return@addOnSuccessListener
            }

            businessRef.get().addOnSuccessListener { businessSnapshot ->
                val currentTurn = businessSnapshot.getLong("currentTurn") ?: 0L
                val nextTurn = currentTurn + 1L

                businessRef.update("currentTurn", nextTurn).addOnSuccessListener {
                    val turno = hashMapOf(
                        "number" to nextTurn,
                        "userId" to user.uid,
                        "status" to "waiting",
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    turnRef.set(turno).addOnSuccessListener {
                        Log.d("Firestore", "Turno asignado: $nextTurn")
                        onResult(nextTurn)
                    }.addOnFailureListener { e ->
                        Log.e("Firestore", "Error al crear turno", e)
                        onResult(null)
                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error al actualizar contador", e)
                    onResult(null)
                }
            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error al leer negocio", e)
                onResult(null)
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al verificar turno", e)
            onResult(null)
        }
    }

    // 2. Cancelar el turno activo cambiando su estado
    fun cancelarTurno(businessId: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onResult(false)

        db.collection("businesses")
            .document(businessId)
            .collection("turns")
            .document(user.uid)
            .update("status", "cancelled")
            .addOnSuccessListener {
                Log.d("Firestore", "Turno cancelado exitosamente")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cancelar turno", e)
                onResult(false)
            }
    }

    // 3. Escuchar la fila general de personas esperando (Contador gigante)
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

    // 🔥 NUEVO: Escuchar MI turno en tiempo real (Sustituye al obtenerMiTurno viejo)
    fun escucharMiTurno(businessId: String, onResult: (Long?) -> Unit) {
        val user = auth.currentUser ?: return onResult(null)

        db.collection("businesses")
            .document(businessId)
            .collection("turns")
            .document(user.uid)
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.e("Firestore", "Error escuchando mi turno", e)
                    onResult(null)
                    return@addSnapshotListener
                }

                // Solo devolvemos el número si el documento existe Y está en estado "waiting"
                if (document != null && document.exists() && document.getString("status") == "waiting") {
                    val number = document.getLong("number")
                    onResult(number)
                } else {
                    // Si no existe, o fue cancelado/terminado, devolvemos null
                    onResult(null)
                }
            }
    }

    // 5. Avanzar turno (Lógica de administración, intacta)
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

    // 6. Escuchar los negocios activos (Intacta)
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

    fun obtenerNegocioPorId(businessId: String, onResult: (Business?) -> Unit) {
        db.collection("businesses")
            .document(businessId)
            .get()
            .addOnSuccessListener { doc ->
                val business = if (doc.exists()) {
                    doc.toObject(Business::class.java)?.copy(id = doc.id)
                } else {
                    null
                }
                onResult(business)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener negocio por ID", e)
                onResult(null)
            }
    }
}