package com.example.aia_solutions

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EstadisticasFragment : Fragment(R.layout.fragment_estadisticas) {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtFechaHoy        = view.findViewById<TextView>(R.id.txtFechaHoy)
        val txtAtendidosHoy    = view.findViewById<TextView>(R.id.txtAtendidosHoy)
        val txtCanceladosHoy   = view.findViewById<TextView>(R.id.txtCanceladosHoy)
        val txtTiempoMedioHoy  = view.findViewById<TextView>(R.id.txtTiempoMedioHoy)
        val txtTasaAtencion    = view.findViewById<TextView>(R.id.txtTasaAtencion)
        val progressAtencion   = view.findViewById<ProgressBar>(R.id.progressAtencion)
        val txtTotalAtendidos  = view.findViewById<TextView>(R.id.txtTotalAtendidos)
        val txtTotalCancelados = view.findViewById<TextView>(R.id.txtTotalCancelados)
        val txtTiempoMedioTotal = view.findViewById<TextView>(R.id.txtTiempoMedioTotal)
        val txtHoraPico        = view.findViewById<TextView>(R.id.txtHoraPico)
        val txtHoraPicoDesc    = view.findViewById<TextView>(R.id.txtHoraPicoDesc)

        // Fecha de hoy
        val sdf = SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "MX"))
        txtFechaHoy.text = sdf.format(Date())

        val uid = auth.currentUser?.uid ?: return

        db.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val businessId = snapshot.documents.firstOrNull()?.id ?: return@addOnSuccessListener
                cargarEstadisticas(
                    businessId,
                    txtAtendidosHoy, txtCanceladosHoy, txtTiempoMedioHoy,
                    txtTasaAtencion, progressAtencion,
                    txtTotalAtendidos, txtTotalCancelados, txtTiempoMedioTotal,
                    txtHoraPico, txtHoraPicoDesc
                )
            }
    }

    private fun cargarEstadisticas(
        businessId: String,
        txtAtendidosHoy: TextView, txtCanceladosHoy: TextView, txtTiempoMedioHoy: TextView,
        txtTasaAtencion: TextView, progressAtencion: ProgressBar,
        txtTotalAtendidos: TextView, txtTotalCancelados: TextView, txtTiempoMedioTotal: TextView,
        txtHoraPico: TextView, txtHoraPicoDesc: TextView
    ) {
        db.collection("businesses").document(businessId)
            .collection("turns")
            .get()
            .addOnSuccessListener { snapshot ->
                val todos = snapshot.documents

                // Inicio y fin de hoy
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val inicioDia = cal.time
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val finDia = cal.time

                // Filtrar por hoy
                val hoy = todos.filter { doc ->
                    val fecha = doc.getTimestamp("createdAt")?.toDate()
                    fecha != null && fecha.after(inicioDia) && fecha.before(finDia)
                }

                val atendidosHoy   = hoy.filter { it.getString("status") == "done" }
                val canceladosHoy  = hoy.filter { it.getString("status") == "cancelled" }
                val atendidosTotal = todos.filter { it.getString("status") == "done" }
                val canceladosTotal = todos.filter { it.getString("status") == "cancelled" }

                // Métricas de hoy
                txtAtendidosHoy.text  = atendidosHoy.size.toString()
                txtCanceladosHoy.text = canceladosHoy.size.toString()
                txtTotalAtendidos.text  = atendidosTotal.size.toString()
                txtTotalCancelados.text = canceladosTotal.size.toString()

                // Tiempo medio hoy
                val tiemposHoy = atendidosHoy.mapNotNull { it.getLong("waitSeconds") }
                if (tiemposHoy.isNotEmpty()) {
                    val mediaHoy = tiemposHoy.average().toLong()
                    txtTiempoMedioHoy.text = formatTiempo(mediaHoy)
                }

                // Tiempo medio total
                val tiemposTotal = atendidosTotal.mapNotNull { it.getLong("waitSeconds") }
                if (tiemposTotal.isNotEmpty()) {
                    val mediaTotal = tiemposTotal.average().toLong()
                    txtTiempoMedioTotal.text = formatTiempo(mediaTotal)
                }

                // Tasa de atención hoy
                val totalHoy = hoy.size
                if (totalHoy > 0) {
                    val tasa = (atendidosHoy.size * 100) / totalHoy
                    txtTasaAtencion.text = "$tasa%"
                    progressAtencion.progress = tasa
                }

                // Hora pico (histórico)
                val horasPico = atendidosTotal.mapNotNull { doc ->
                    doc.getTimestamp("createdAt")?.toDate()?.let {
                        Calendar.getInstance().apply { time = it }.get(Calendar.HOUR_OF_DAY)
                    }
                }
                if (horasPico.isNotEmpty()) {
                    val horaMasComun = horasPico.groupBy { it }
                        .maxByOrNull { it.value.size }?.key ?: 0
                    val amPm = if (horaMasComun < 12) "AM" else "PM"
                    val hora12 = if (horaMasComun % 12 == 0) 12 else horaMasComun % 12
                    txtHoraPico.text     = "$hora12:00 $amPm"
                    txtHoraPicoDesc.text = "es cuando más clientes llegan"
                }
            }
    }

    private fun formatTiempo(segundos: Long): String {
        return when {
            segundos < 60   -> "${segundos}s"
            segundos < 3600 -> "${segundos / 60}m"
            else            -> "${segundos / 3600}h ${(segundos % 3600) / 60}m"
        }
    }
}