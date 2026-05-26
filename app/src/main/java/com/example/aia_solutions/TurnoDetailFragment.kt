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

    // Variables visuales
    private lateinit var txtWaitCount: TextView
    private lateinit var txtMyTurnStatus: TextView
    private lateinit var btnTakeTurn: Button
    private lateinit var btnCancelTurn: Button

    // Variables de estado para cruzar los datos
    private var listaTurnosActuales: List<Long> = emptyList()
    private var miTurnoActual: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtBusinessName = view.findViewById<TextView>(R.id.txtBusinessName)
        txtWaitCount = view.findViewById(R.id.txtWaitCount)
        txtMyTurnStatus = view.findViewById(R.id.txtMyTurnStatus)
        btnTakeTurn = view.findViewById(R.id.btnTakeTurn)
        btnCancelTurn = view.findViewById(R.id.btnCancelTurn)

        val businessName = arguments?.getString("businessName")
        val businessId = arguments?.getString("businessId")

        if (businessName != null) {
            txtBusinessName.text = businessName
        }

        if (businessId != null) {

            // 1. Escuchar la fila general y extraer solo los números
            service.escucharTurnos(businessId) { lista ->
                activity?.runOnUiThread {
                    // Mapeamos los documentos para tener una lista pura de números [15, 16, 17...]
                    listaTurnosActuales = lista.mapNotNull { (it["number"] as? Number)?.toLong() }
                    actualizarPantallaInteligente()
                }
            }

            // 2. Escuchar mi turno en tiempo real
            service.escucharMiTurno(businessId) { miNumero ->
                activity?.runOnUiThread {
                    miTurnoActual = miNumero
                    actualizarPantallaInteligente()
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

    // función que cruza los datos y decide qué mostrar
    private fun actualizarPantallaInteligente() {
        // Siempre mostramos cuántos hay en total esperando
        txtWaitCount.text = listaTurnosActuales.size.toString()

        if (miTurnoActual != null && miTurnoActual!! > 0) {

            // Revisamos quién es el primero en la fila general
            val elPrimeroDeLaFila = listaTurnosActuales.firstOrNull()

            if (elPrimeroDeLaFila == miTurnoActual) {
                // ¡YA ES TU TURNO! Eres el #1 de la lista
                txtMyTurnStatus.text = "¡ES TU TURNO! Pasa al mostrador."
                txtMyTurnStatus.setTextColor(Color.parseColor("#FF9800")) // Color Naranja llamativo

                btnTakeTurn.text = "ES TU TURNO"
                btnTakeTurn.isEnabled = false
                btnTakeTurn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
                btnCancelTurn.visibility = View.VISIBLE
                btnCancelTurn.isEnabled = true
                btnCancelTurn.text = "Cancelar mi turno"

            } else {
                // AÚN NO ES TU TURNO: Calculamos cuántos hay antes de ti
                val posicionEnFila = listaTurnosActuales.indexOf(miTurnoActual)
                val personasAdelante = if (posicionEnFila > 0) posicionEnFila else 0

                txtMyTurnStatus.text = "Tu turno: #$miTurnoActual\nFaltan $personasAdelante personas antes de ti"
                txtMyTurnStatus.setTextColor(Color.parseColor("#4CAF50")) // Verde

                btnTakeTurn.text = "YA TIENES UN TURNO"
                btnTakeTurn.isEnabled = false
                btnTakeTurn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
                btnCancelTurn.visibility = View.VISIBLE
                btnCancelTurn.isEnabled = true
                btnCancelTurn.text = "Cancelar mi turno"
            }

        } else {
            // ESTADO NORMAL: Sin turno
            txtMyTurnStatus.text = "Aún no solicitas turno aquí"
            txtMyTurnStatus.setTextColor(Color.parseColor("#424242")) // Gris oscuro

            btnTakeTurn.text = "SOLICITAR MI TURNO"
            btnTakeTurn.isEnabled = true
            btnTakeTurn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2196F3")) // Azul
            btnCancelTurn.visibility = View.GONE
        }
    }
}