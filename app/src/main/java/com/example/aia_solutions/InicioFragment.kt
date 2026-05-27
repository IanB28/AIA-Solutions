package com.example.aia_solutions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.card.MaterialCardView
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
        val cardEscanear = view.findViewById<MaterialCardView>(R.id.cardEscanear)

        btnBuscarNegocios.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragmentos, BusinessFragment())
                .addToBackStack(null)
                .commit()
        }

        cardEscanear.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragmentos, QRScannerFragment())
                .addToBackStack(null)
                .commit()
        }


    }


}
