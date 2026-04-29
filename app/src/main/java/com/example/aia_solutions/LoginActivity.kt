package com.example.aia_solutions

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Uso de TextInputEditText para compatibilidad con Material Design
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText

    // Layouts para manejo de errores visuales
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    private lateinit var loginButton: Button
    private lateinit var signInButton: Button
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializar Splash Screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Redirigir si ya hay una sesión activa (opcional)
        /*
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        */

        setContentView(R.layout.activity_login)

        // Inicialización de vistas
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        signInButton = findViewById(R.id.signInButton)
        rememberMeCheckbox = findViewById(R.id.rememberMe)
        progressBar = findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            if (validateForm()) {
                performFirebaseLogin()
            }
        }

        signInButton.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.forgotPassword).setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                emailLayout.error = "Ingresa tu correo para recuperar la contraseña"
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        emailLayout.error = null
        passwordLayout.error = null

        if (email.isEmpty()) {
            emailLayout.error = "Ingresa tu correo"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Formato de correo inválido"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Ingresa tu contraseña"
            isValid = false
        }

        return isValid
    }

    private fun performFirebaseLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        progressBar.visibility = View.VISIBLE
        toggleUiState(false)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                toggleUiState(true)

                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(baseContext, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun toggleUiState(enabled: Boolean) {
        loginButton.isEnabled = enabled
        emailEditText.isEnabled = enabled
        passwordEditText.isEnabled = enabled
    }
}
