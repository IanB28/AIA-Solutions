package com.example.aia_solutions

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.aia_solutions.InicioFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var bottomNavigation: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        if (savedInstanceState == null) {
            cargarFragment(InicioFragment())
        }
        bottomNavigation.setOnItemSelectedListener { item ->


            val fragment: Fragment = when (item.itemId) {
                R.id.nav_inicio -> InicioFragment()
                R.id.nav_perfil -> PerfilFragment()
                R.id.nav_configuracion -> ConfigFragment()
                else -> InicioFragment()

            }
            cargarFragment(fragment)
            true
        }

    }


    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedorFragmentos, fragment)
            .commit()
    }

}
