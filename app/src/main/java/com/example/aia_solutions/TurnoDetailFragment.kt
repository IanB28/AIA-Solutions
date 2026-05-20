package com.example.aia_solutions

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import data.firebase.firebase.FirestoreService

class TurnoDetailFragment : Fragment(R.layout.fragment_turno_detail) {

    private val service = FirestoreService()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtBusinessName = view.findViewById<TextView>(R.id.txtBusinessName)
        val txtWaitCount = view.findViewById<TextView>(R.id.txtWaitCount)
        val txtMyTurnStatus = view.findViewById<TextView>(R.id.txtMyTurnStatus)
        val btnTakeTurn = view.findViewById<Button>(R.id.btnTakeTurn)
        val btnCancelTurn = view.findViewById<Button>(R.id.btnCancelTurn)

        val businessName = arguments?.getString("businessName")
        val businessId = arguments?.getString("businessId")

        if (businessName != null) {
            txtBusinessName.text = businessName
        }

        if (businessId != null) {

            // 1. Escuchar la fila general en tiempo real
            service.escucharTurnos(businessId) { lista ->
                activity?.runOnUiThread {
                    txtWaitCount.text = lista.size.toString()
                }
            }

            // 2. 🔥 Aquí está la corrección: Usamos la función NUEVA (escucharMiTurno)
            service.escucharMiTurno(businessId) { miNumero ->
                activity?.runOnUiThread {
                    actualizarEstadoBoton(miNumero, txtMyTurnStatus, btnTakeTurn, btnCancelTurn)
                }
            }
        }

        // --- ACCIÓN: SOLICITAR TURNO ---
        btnTakeTurn.setOnClickListener {
            if (businessId != null) {
                btnTakeTurn.isEnabled = false
                btnTakeTurn.text = "SOLICITANDO..."

                service.tomarTurno(businessId) { numeroAsignado ->
                    activity?.runOnUiThread {
                        if (numeroAsignado != null && numeroAsignado > 0) {
                            Toast.makeText(context, "¡Turno #$numeroAsignado solicitado!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al solicitar turno", Toast.LENGTH_SHORT).show()
                            btnTakeTurn.isEnabled = true
                            btnTakeTurn.text = "SOLICITAR MI TURNO"
                        }
                    }
                }
            }
        }

        // --- ACCIÓN: CANCELAR TURNO ---
        btnCancelTurn.setOnClickListener {
            if (businessId != null) {
                btnCancelTurn.isEnabled = false
                btnCancelTurn.text = "Cancelando..."

                service.cancelarTurno(businessId) { exito ->
                    activity?.runOnUiThread {
                        if (exito) {
                            Toast.makeText(context, "Turno cancelado exitosamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al cancelar", Toast.LENGTH_SHORT).show()
                            btnCancelTurn.isEnabled = true
                            btnCancelTurn.text = "Cancelar mi turno"
                        }
                    }
                }
            }
        }
    }

    // Control preciso de los estados visuales del Ticket Digital
    private fun actualizarEstadoBoton(miNumero: Long?, txtMyTurnStatus: TextView, btnTakeTurn: Button, btnCancelTurn: Button) {
        if (miNumero != null && miNumero > 0) {
            // ESTADO: Con turno activo (Verde y bloqueado)
            txtMyTurnStatus.text = "Tu turno actual es el: #$miNumero"
            txtMyTurnStatus.setTextColor(Color.parseColor("#4CAF50"))

            btnTakeTurn.text = "YA TIENES UN TURNO"
            btnTakeTurn.isEnabled = false
            btnTakeTurn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))

            btnCancelTurn.visibility = View.VISIBLE
            btnCancelTurn.isEnabled = true
            btnCancelTurn.text = "Cancelar mi turno"
        } else {
            // ESTADO: Sin turno / Cancelado (Azul y libre)
            txtMyTurnStatus.text = "Aún no solicitas turno aquí"
            txtMyTurnStatus.setTextColor(Color.parseColor("#424242"))

            btnTakeTurn.text = "SOLICITAR MI TURNO"
            btnTakeTurn.isEnabled = true
            btnTakeTurn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))

            btnCancelTurn.visibility = View.GONE
        }
    }
}