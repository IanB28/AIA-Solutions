package com.example.aia_solutions

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AdminPerfilFragment : Fragment(R.layout.fragment_admin_perfil) {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtPerfilIniciales = view.findViewById<TextView>(R.id.txtPerfilIniciales)
        val txtPerfilNombre    = view.findViewById<TextView>(R.id.txtPerfilNombre)
        val txtPerfilEmail     = view.findViewById<TextView>(R.id.txtPerfilEmail)
        val txtInfoNombre      = view.findViewById<TextView>(R.id.txtInfoNombre)
        val txtInfoEmail       = view.findViewById<TextView>(R.id.txtInfoEmail)
        val txtInfoFecha       = view.findViewById<TextView>(R.id.txtInfoFecha)
        val btnCerrarSesion    = view.findViewById<TextView>(R.id.btnCerrarSesion)

        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("name") ?: ""
                val email  = doc.getString("email") ?: ""
                val fecha  = doc.getTimestamp("createdAt")?.toDate()

                val fechaFormateada = if (fecha != null) {
                    SimpleDateFormat("dd MMM yyyy", Locale("es", "MX")).format(fecha)
                } else "--"

                txtPerfilIniciales.text = nombre.take(2).uppercase()
                txtPerfilNombre.text    = nombre
                txtPerfilEmail.text     = email
                txtInfoNombre.text      = nombre
                txtInfoEmail.text       = email
                txtInfoFecha.text       = fechaFormateada
            }

        btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    auth.signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}