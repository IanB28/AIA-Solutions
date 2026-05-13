package com.example.aia_solutions

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MiNegocioFragment : Fragment(R.layout.fragment_mi_negocio) {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private var businessId: String? = null

    private val categorias = listOf("salud", "restaurante", "tienda", "servicios", "educación", "otro")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack            = view.findViewById<ImageView>(R.id.btnBack)
        val btnGuardar         = view.findViewById<TextView>(R.id.btnGuardar)
        val etNombre           = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion      = view.findViewById<EditText>(R.id.etDescripcion)
        val spinnerCategoria   = view.findViewById<Spinner>(R.id.spinnerCategoria)
        val btnEliminarNegocio = view.findViewById<TextView>(R.id.btnEliminarNegocio)

        // Setup spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categorias
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = spinnerAdapter

        val uid = auth.currentUser?.uid ?: return

        // Cargar datos actuales
        db.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull() ?: return@addOnSuccessListener
                businessId = doc.id

                etNombre.setText(doc.getString("name") ?: "")
                etDescripcion.setText(doc.getString("description") ?: "")

                val categoria = doc.getString("category") ?: ""
                val index = categorias.indexOf(categoria)
                if (index >= 0) spinnerCategoria.setSelection(index)
            }

        // Guardar cambios
        btnGuardar.setOnClickListener {
            val bid = businessId ?: return@setOnClickListener

            val nombre      = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val categoria   = spinnerCategoria.selectedItem.toString()

            if (nombre.isEmpty()) {
                etNombre.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false

            db.collection("businesses").document(bid)
                .update(
                    mapOf(
                        "name"        to nombre,
                        "description" to descripcion,
                        "category"    to categoria
                    )
                )
                .addOnSuccessListener {
                    btnGuardar.isEnabled = true
                    Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    btnGuardar.isEnabled = true
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }

        // Eliminar negocio
        btnEliminarNegocio.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar negocio")
                .setMessage("¿Estás seguro? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    val bid = businessId ?: return@setPositiveButton
                    db.collection("businesses").document(bid)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Negocio eliminado", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Regresar
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}