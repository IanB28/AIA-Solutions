package com.example.aia_solutions

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import data.firebase.adapter.TurnoAdapter
import data.firebase.model.Turno

class AdminInicioFragment : Fragment(R.layout.fragment_admin_inicio) {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private var businessId: String?  = null
    private var turnoActualId: String? = null
    private var turnoActualNumero: Long = 0
    private var turnoActualNombre: String = ""

    private lateinit var turnoAdapter: TurnoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNombreNegocio        = view.findViewById<TextView>(R.id.txtNombreNegocio)
        val txtEnEspera             = view.findViewById<TextView>(R.id.txtEnEspera)
        val txtAtendidos            = view.findViewById<TextView>(R.id.txtAtendidos)
        val txtNumeroTurnoActual    = view.findViewById<TextView>(R.id.txtNumeroTurnoActual)
        val txtNombreTurnoActual    = view.findViewById<TextView>(R.id.txtNombreTurnoActual)
        val txtInicialesTurnoActual = view.findViewById<TextView>(R.id.txtInicialesTurnoActual)
        val txtTiempoTurnoActual    = view.findViewById<TextView>(R.id.txtTiempoTurnoActual)
        val txtTotalEspera          = view.findViewById<TextView>(R.id.txtTotalEspera)
        val txtSinEspera            = view.findViewById<TextView>(R.id.txtSinEspera)
        val btnSiguiente            = view.findViewById<View>(R.id.btnSiguiente)
        val recyclerTurnos          = view.findViewById<RecyclerView>(R.id.recyclerTurnos)
        val switchEstado            = view.findViewById<Switch>(R.id.switchEstado)
        val layoutTurnoActivo       = view.findViewById<LinearLayout>(R.id.layoutTurnoActivo)
        val layoutSinTurno          = view.findViewById<LinearLayout>(R.id.layoutSinTurno)

        turnoAdapter = TurnoAdapter(emptyList())
        recyclerTurnos.layoutManager = LinearLayoutManager(requireContext())
        recyclerTurnos.adapter = turnoAdapter

        // Click turno actual y botón siguiente abren el bottom sheet
        layoutTurnoActivo.setOnClickListener {
            val bid = businessId ?: return@setOnClickListener
            val tid = turnoActualId ?: return@setOnClickListener
            mostrarBottomSheet(turnoActualNumero, turnoActualNombre, tid, bid)
        }

        btnSiguiente.setOnClickListener {
            val bid = businessId ?: return@setOnClickListener
            val tid = turnoActualId ?: return@setOnClickListener
            mostrarBottomSheet(turnoActualNumero, turnoActualNombre, tid, bid)
        }

        val uid = auth.currentUser?.uid ?: return

        db.collection("businesses")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull() ?: return@addOnSuccessListener
                businessId = doc.id
                txtNombreNegocio.text = doc.getString("name") ?: "Mi negocio"

                val isActive = doc.getBoolean("isActive") ?: true
                switchEstado.isChecked = isActive
                switchEstado.text = if (isActive) "Abierto" else "Cerrado"

                escucharTurnos(
                    doc.id,
                    txtEnEspera, txtAtendidos, txtTotalEspera, txtSinEspera,
                    txtNumeroTurnoActual, txtNombreTurnoActual,
                    txtInicialesTurnoActual, txtTiempoTurnoActual,
                    layoutTurnoActivo, layoutSinTurno, recyclerTurnos
                )
            }
            .addOnFailureListener {
                txtNombreNegocio.text = "Error al cargar"
            }

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
                    switchEstado.isChecked = !isChecked
                }
        }
    }

    private fun escucharTurnos(
        businessId: String,
        txtEnEspera: TextView, txtAtendidos: TextView,
        txtTotalEspera: TextView, txtSinEspera: TextView,
        txtNumeroTurnoActual: TextView, txtNombreTurnoActual: TextView,
        txtInicialesTurnoActual: TextView, txtTiempoTurnoActual: TextView,
        layoutTurnoActivo: LinearLayout, layoutSinTurno: LinearLayout,
        recyclerTurnos: RecyclerView
    ) {
        db.collection("businesses").document(businessId)
            .collection("turns")
            .orderBy("number", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val todos     = snapshot.documents
                val enEspera  = todos.filter { it.getString("status") == "waiting" }
                val atendidos = todos.filter { it.getString("status") == "done" }

                txtEnEspera.text    = enEspera.size.toString()
                txtAtendidos.text   = atendidos.size.toString()
                txtTotalEspera.text = "${enEspera.size} turnos"

                val turnoActual = enEspera.firstOrNull()
                if (turnoActual != null) {
                    layoutTurnoActivo.visibility = View.VISIBLE
                    layoutSinTurno.visibility    = View.GONE

                    val numero  = turnoActual.getLong("number") ?: 0
                    val userId  = turnoActual.getString("userId") ?: ""
                    val turnoId = turnoActual.id

                    txtNumeroTurnoActual.text = "#$numero"
                    turnoActualId     = turnoId
                    turnoActualNumero = numero

                    if (userId.isNotEmpty()) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                val nombre    = userDoc.getString("name") ?: "Cliente"
                                val iniciales = nombre.take(2).uppercase()
                                txtNombreTurnoActual.text    = nombre
                                txtInicialesTurnoActual.text = iniciales
                                turnoActualNombre = nombre
                            }
                    } else {
                        txtNombreTurnoActual.text    = "Cliente #$numero"
                        txtInicialesTurnoActual.text = "#$numero"
                        turnoActualNombre = "Cliente #$numero"
                    }

                    txtTiempoTurnoActual.text = "En espera"
                } else {
                    layoutTurnoActivo.visibility = View.GONE
                    layoutSinTurno.visibility    = View.VISIBLE
                    txtNumeroTurnoActual.text    = "--"
                    turnoActualId     = null
                    turnoActualNumero = 0
                    turnoActualNombre = ""
                }

                val cola = enEspera.drop(1)
                if (cola.isEmpty()) {
                    txtSinEspera.visibility   = View.VISIBLE
                    recyclerTurnos.visibility = View.GONE
                } else {
                    txtSinEspera.visibility   = View.GONE
                    recyclerTurnos.visibility = View.VISIBLE
                    val items = cola.map { doc ->
                        Turno(
                            id     = doc.id,
                            number = doc.getLong("number") ?: 0,
                            userId = doc.getString("userId") ?: "",
                            status = doc.getString("status") ?: ""
                        )
                    }
                    turnoAdapter.actualizarLista(items)
                }
            }
    }

    private fun mostrarBottomSheet(
        numero: Long,
        nombre: String,
        turnoId: String,
        businessId: String
    ) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val sheetView   = layoutInflater.inflate(R.layout.bottom_sheet_turno, null)
        bottomSheet.setContentView(sheetView)

        sheetView.findViewById<TextView>(R.id.txtBottomNombre).text    = nombre
        sheetView.findViewById<TextView>(R.id.txtBottomNumero).text    = "Turno #$numero"
        sheetView.findViewById<TextView>(R.id.txtBottomIniciales).text = nombre.take(2).uppercase()

        sheetView.findViewById<TextView>(R.id.btnAtendido).setOnClickListener {
            db.collection("businesses").document(businessId)
                .collection("turns").document(turnoId)
                .update("status", "done")
                .addOnSuccessListener { bottomSheet.dismiss() }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }

        sheetView.findViewById<TextView>(R.id.btnCancelarTurno).setOnClickListener {
            db.collection("businesses").document(businessId)
                .collection("turns").document(turnoId)
                .update("status", "cancelled")
                .addOnSuccessListener { bottomSheet.dismiss() }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al cancelar", Toast.LENGTH_SHORT).show()
                }
        }

        sheetView.findViewById<TextView>(R.id.btnCerrarSheet).setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }
}