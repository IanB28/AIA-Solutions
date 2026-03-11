package com.example.aia_solutions

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class ConfigFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos la vista que definimos anteriormente
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a los elementos del XML
        val switchNotifications = view.findViewById<SwitchMaterial>(R.id.switchNotifications)
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val tvLanguage = view.findViewById<TextView>(R.id.tvLanguage)
        val tvPrivacy = view.findViewById<TextView>(R.id.tvPrivacy)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Manejo de Notificaciones
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "activadas" else "desactivadas"
            Toast.makeText(requireContext(), "Notificaciones $status", Toast.LENGTH_SHORT).show()
        }

        // Manejo de Modo Oscuro
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "activado" else "desactivado"
            Toast.makeText(requireContext(), "Modo oscuro $status", Toast.LENGTH_SHORT).show()
        }

        // Click en Idioma
        tvLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Configuración de idioma", Toast.LENGTH_SHORT).show()
        }

        // Click en Privacidad
        tvPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Ajustes de privacidad", Toast.LENGTH_SHORT).show()
        }

        // Cerrar Sesión con Firebase
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            
            // Redirigir al Login y limpiar la pila de actividades
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
