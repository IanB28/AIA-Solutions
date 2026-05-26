package com.example.aia_solutions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomNavAdmin)

        // Fragment inicial
        if (savedInstanceState == null) {
            cargarFragment(AdminInicioFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio  -> cargarFragment(AdminInicioFragment())
                R.id.nav_negocio -> cargarFragment(MiNegocioFragment())
                R.id.nav_perfil  -> cargarFragment(AdminPerfilFragment())
            }
            true
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.contenedorAdmin, fragment)
            .commit()
    }
}