package com.example.aia_solutions

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import data.firebase.firebase.FirestoreService

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private val service = FirestoreService()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNombreNegocio     = view.findViewById<TextView>(R.id.txtNombreNegocio)
        val txtEnEspera          = view.findViewById<TextView>(R.id.txtEnEspera)
        val txtAtendidos         = view.findViewById<TextView>(R.id.txtAtendidos)
        val txtNombreTurnoActual = view.findViewById<TextView>(R.id.txtNombreTurnoActual)
        val txtInicialesTurnoActual = view.findViewById<TextView>(R.id.txtInicialesTurnoActual)
        val txtNumeroTurnoActual = view.findViewById<TextView>(R.id.txtNumeroTurnoActual)
        val txtTiempoTurnoActual = view.findViewById<TextView>(R.id.txtTiempoTurnoActual)
        val txtTotalEspera       = view.findViewById<TextView>(R.id.txtTotalEspera)
        val btnSiguiente         = view.findViewById<View>(R.id.btnSiguiente)
        val recyclerTurnos       = view.findViewById<RecyclerView>(R.id.recyclerTurnos)

        recyclerTurnos.layoutManager = LinearLayoutManager(requireContext())

        // Por ahora datos de prueba — luego los conectamos a Firebase
        txtNombreNegocio.text = "Mi negocio"
        txtEnEspera.text = "0"
        txtAtendidos.text = "0"
        txtTotalEspera.text = "0 turnos"

        btnSiguiente.setOnClickListener {
            // lógica para pasar al siguiente turno
        }
    }
}