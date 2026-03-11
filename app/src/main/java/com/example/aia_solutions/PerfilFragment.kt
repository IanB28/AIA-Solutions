
package com.example.aia_solutions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        auth = FirebaseAuth.getInstance()

        // Referencias de la UI
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)
        val btnEditProfile = view.findViewById<LinearLayout>(R.id.btnEditProfile)

        // Mostrar email del usuario actual
        val currentUser = auth.currentUser
        tvEmail.text = currentUser?.email ?: "Usuario"

        // Configurar botón de cerrar sesión
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // Limpiar el stack de actividades para que no pueda volver atrás al perfil
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // El botón de editar perfil puede quedar como placeholder por ahora
        btnEditProfile.setOnClickListener {
            // Lógica futura para editar perfil
        }

        return view
    }
}
