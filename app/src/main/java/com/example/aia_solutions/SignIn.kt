package com.example.aia_solutions

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Contenedores visuales para mostrar errores
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout

    // Campos de texto actualizados a TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText

    private lateinit var signUpButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backToLoginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        // Inicialización de contenedores
        emailLayout = findViewById(R.id.emailRegisterLayout)
        passwordLayout = findViewById(R.id.passwordRegisterLayout)
        confirmPasswordLayout = findViewById(R.id.passwordConfirmLayout)

        // Inicialización de campos de texto
        emailEditText = findViewById(R.id.etEmailRegister)
        passwordEditText = findViewById(R.id.etPassRegister)
        confirmPasswordEditText = findViewById(R.id.etPassRegisterConfirm)

        signUpButton = findViewById(R.id.btnSignUp)
        progressBar = findViewById(R.id.progressBarSignIn)
        backToLoginText = findViewById(R.id.tvBackToLogin)

        signUpButton.setOnClickListener {
            if (validateForm()) {
                performSignUp()
            }
        }

        // Navegación para regresar al Login
        backToLoginText.setOnClickListener {
            // Como SignIn normalmente se abre desde LoginActivity, finish() lo cierra y te regresa
            finish()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Limpiar errores previos
        emailLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

        // Validación de correo
        if (email.isEmpty()) {
            emailLayout.error = "Ingresa tu correo"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Ingresa un formato de correo válido"
            isValid = false
        }

        // Validación de contraseña
        if (password.isEmpty()) {
            passwordLayout.error = "Ingresa una contraseña"
            isValid = false
        } else if (password.length < 8) {
            passwordLayout.error = "Debe tener al menos 8 caracteres"
            isValid = false
        }

        // Validación de confirmación de contraseña
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Repite tu contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    private fun performSignUp() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Bloquear UI mientras carga
        progressBar.visibility = View.VISIBLE
        signUpButton.isEnabled = false
        emailEditText.isEnabled = false
        passwordEditText.isEnabled = false
        confirmPasswordEditText.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Restaurar UI
                progressBar.visibility = View.GONE
                signUpButton.isEnabled = true
                emailEditText.isEnabled = true
                passwordEditText.isEnabled = true
                confirmPasswordEditText.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    // Limpiar la pila de actividades para que el usuario no pueda volver al login con el botón de retroceso del sistema
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(baseContext, "Fallo en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}