package com.example.aia_solutions

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import data.firebase.adapter.BusinessAdapter
import data.firebase.firebase.FirestoreService
import data.firebase.model.Business
import com.example.aia_solutions.TurnoDetailFragment

class BusinessFragment : Fragment(R.layout.fragment_business) {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: BusinessAdapter
    private lateinit var etSearch: EditText
    private lateinit var chipGroup: LinearLayout
    private val service = FirestoreService()

    private var listaCompleta: List<Business> = emptyList()
    private var categoriaSeleccionada: String = "Todos"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler  = view.findViewById(R.id.recyclerBusinesses)
        etSearch  = view.findViewById(R.id.etSearch)
        chipGroup = view.findViewById(R.id.chipGroup)

        adapter = BusinessAdapter(emptyList()) { business ->
            // 1. Creamos el fragmento de detalle y le pasamos el ID del negocio
            val detailFragment = TurnoDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("businessId", business.id)
                    putString("businessName", business.name)
                }
            }

            // 2. Realizamos la transacción manual usando el contenedor de tu MainActivity
            parentFragmentManager.beginTransaction()
                .replace(R.id.contenedorFragmentos, detailFragment)
                .addToBackStack(null) // Esto permite que el botón "Atrás" del celular regrese a la lista
                .commit()
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        setupSearch()
        cargarNegocios()
    }

    private fun cargarNegocios() {
        service.escucharNegocios { lista ->
            listaCompleta = lista
            setupChips(lista)
            filtrar()
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filtrar() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupChips(lista: List<Business>) {
        // Solo construye los chips una vez
        if (chipGroup.childCount > 0) return

        val categorias = listOf("Todos") + lista.map { it.category }.distinct()

        categorias.forEach { categoria ->
            val chip = TextView(requireContext()).apply {
                text = categoria
                textSize = 12f
                setPadding(32, 16, 32, 16)
                setBackgroundResource(
                    if (categoria == categoriaSeleccionada) R.drawable.bg_badge_active
                    else R.drawable.bg_chip_category
                )
                setTextColor(
                    if (categoria == categoriaSeleccionada)
                        ContextCompat.getColor(context, R.color.green_text)
                    else 0xFF5F5E5A.toInt()
                )
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 8
                layoutParams = params

                setOnClickListener {
                    categoriaSeleccionada = categoria
                    actualizarChips()
                    filtrar()
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun actualizarChips() {
        val categorias = listOf("Todos") + listaCompleta.map { it.category }.distinct()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as TextView
            val esSeleccionado = chip.text == categoriaSeleccionada
            chip.setBackgroundResource(
                if (esSeleccionado) R.drawable.bg_badge_active
                else R.drawable.bg_chip_category
            )
            chip.setTextColor(
                if (esSeleccionado)
                    ContextCompat.getColor(requireContext(), R.color.green_text)
                else 0xFF5F5E5A.toInt()
            )
        }
    }

    private fun filtrar() {
        val query = etSearch.text.toString().trim().lowercase()

        val filtrada = listaCompleta.filter { business ->
            val coincideCategoria = categoriaSeleccionada == "Todos" ||
                    business.category.equals(categoriaSeleccionada, ignoreCase = true)

            val coincideBusqueda = query.isEmpty() ||
                    business.name.lowercase().contains(query) ||
                    business.description.lowercase().contains(query) ||
                    business.category.lowercase().contains(query)

            coincideCategoria && coincideBusqueda
        }

        adapter.actualizarLista(filtrada)
    }
}