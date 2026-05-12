package com.example.aia_solutions

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import data.firebase.firebase.FirestoreService


class InicioFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val btnBuscarNegocios = view.findViewById<Button>(R.id.btnBuscarNegocios)

        Log.d("DEBUG", "Entré a InicioFragment")
        val firestoreService = FirestoreService()
        firestoreService.tomarTurno("testQueue") { turno ->
            if (turno != null) {
                Log.d("UI", "Tu turno es: $turno")
            } else {
                Log.d("UI", "Error al obtener turno")
            }
        }


        btnBuscarNegocios.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragmentos, BusinessFragment())
                .addToBackStack(null)
                .commit()
        }


    }


}
