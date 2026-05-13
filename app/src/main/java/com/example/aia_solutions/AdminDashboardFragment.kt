package com.example.aia_solutions

import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private var businessId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNombreNegocio        = view.findViewById<TextView>(R.id.txtNombreNegocio)
        val txtEnEspera             = view.findViewById<TextView>(R.id.txtEnEspera)
        val txtAtendidos            = view.findViewById<TextView>(R.id.txtAtendidos)
        val txtNombreTurnoActual    = view.findViewById<TextView>(R.id.txtNombreTurnoActual)
        val txtInicialesTurnoActual = view.findViewById<TextView>(R.id.txtInicialesTurnoActual)
        val txtNumeroTurnoActual    = view.findViewById<TextView>(R.id.txtNumeroTurnoActual)
        val txtTiempoTurnoActual    = view.findViewById<TextView>(R.id.txtTiempoTurnoActual)
        val txtTotalEspera          = view.findViewById<TextView>(R.id.txtTotalEspera)
        val btnSiguiente            = view.findViewById<View>(R.id.btnSiguiente)
        val recyclerTurnos          = view.findViewById<RecyclerView>(R.id.recyclerTurnos)
        val switchEstado            = view.findViewById<Switch>(R.id.switchEstado)

        recyclerTurnos.layoutManager = LinearLayoutManager(requireContext())

        txtEnEspera.text    = "0"
        txtAtendidos.text   = "0"
        txtTotalEspera.text = "0 turnos"

        val uid = auth.currentUser?.uid ?: return

        // Cargar negocio y estado inicial del switch
        db.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    businessId = doc.id
                    txtNombreNegocio.text = doc.getString("name") ?: "Mi negocio"

                    val isActive = doc.getBoolean("isActive") ?: true
                    switchEstado.isChecked = isActive
                    switchEstado.text = if (isActive) "Abierto" else "Cerrado"
                } else {
                    txtNombreNegocio.text = "Sin negocio asignado"
                }
            }
            .addOnFailureListener {
                txtNombreNegocio.text = "Error al cargar"
            }

        // Switch abierto/cerrado
        switchEstado.setOnCheckedChangeListener { _, isChecked ->
            val bid = businessId ?: return@setOnCheckedChangeListener

            switchEstado.text = if (isChecked) "Abierto" else "Cerrado"
            switchEstado.thumbTintList = android.content.res.ColorStateList.valueOf(
                if (isChecked) 0xFF97C459.toInt() else 0xFF888780.toInt()
            )

            db.collection("businesses").document(bid)
                .update("isActive", isChecked)
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                    // Revertir si falla
                    switchEstado.isChecked = !isChecked
                }
        }

        btnSiguiente.setOnClickListener {
            // lógica para pasar al siguiente turno
        }


        view.findViewById<View>(R.id.cardMiNegocio).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragmentos, MiNegocioFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}