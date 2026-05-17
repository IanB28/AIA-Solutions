package com.example.aia_solutions

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import data.firebase.firebase.FirestoreService

class TurnoDetailFragment : Fragment(R.layout.fragment_turno_detail) {

    private val service = FirestoreService()
    private var businessId: String? = null
    private var businessName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar los datos del negocio seleccionado
        businessId = arguments?.getString("businessId")
        businessName = arguments?.getString("businessName")

        val txtName = view.findViewById<TextView>(R.id.txtBusinessName)
        val txtWaitCount = view.findViewById<TextView>(R.id.txtWaitCount)
        val btnTakeTurn = view.findViewById<Button>(R.id.btnTakeTurn)
        val txtMyTurn = view.findViewById<TextView>(R.id.txtMyTurn)

        txtName.text = businessName

        businessId?.let { id ->
            // 1. Escuchar la fila del negocio en tiempo real (Subcolección)
            service.escucharTurnos(id) { lista ->
                txtWaitCount.text = lista.size.toString()
            }

            // 2. Verificar si el usuario ya tiene un turno activo aquí
            service.obtenerMiTurno(id) { miNumero ->
                if (miNumero != null) {
                    txtMyTurn.text = "Tu turno actual es el: #$miNumero"
                    btnTakeTurn.isEnabled = false
                    btnTakeTurn.text = "Ya tienes un turno"
                }
            }
        }

        // 3. Botón para solicitar turno
        btnTakeTurn.setOnClickListener {
            businessId?.let { id ->
                btnTakeTurn.isEnabled = false
                service.tomarTurno(id) { numeroAsignado ->
                    if (numeroAsignado != null) {
                        txtMyTurn.text = "¡Turno asignado! Eres el: #$numeroAsignado"
                        Toast.makeText(context, "Turno solicitado con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        btnTakeTurn.isEnabled = true
                        Toast.makeText(context, "Error al solicitar turno", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}