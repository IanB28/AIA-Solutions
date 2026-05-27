package com.example.aia_solutions

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class ConfigFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Perfil Section
        val tvProfileEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val currentUser = auth.currentUser
        tvProfileEmail.text = currentUser?.email ?: "Usuario"

        // Settings Section
        val switchNotifications = view.findViewById<SwitchMaterial>(R.id.switchNotifications)
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val llLanguage = view.findViewById<LinearLayout>(R.id.llLanguage)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "activadas" else "desactivadas"
            Toast.makeText(requireContext(), "Notificaciones $status", Toast.LENGTH_SHORT).show()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "activado" else "desactivated"
            Toast.makeText(requireContext(), "Modo oscuro $status", Toast.LENGTH_SHORT).show()
        }

        llLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Configuración de idioma", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
