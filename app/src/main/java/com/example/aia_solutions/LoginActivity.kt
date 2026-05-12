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
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var signInButton: Button
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var adminLoginCheckbox: CheckBox
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        setContentView(R.layout.activity_login)

        emailLayout       = findViewById(R.id.emailLayout)
        passwordLayout    = findViewById(R.id.passwordLayout)
        emailEditText     = findViewById(R.id.email)
        passwordEditText  = findViewById(R.id.password)
        loginButton       = findViewById(R.id.loginButton)
        signInButton      = findViewById(R.id.signInButton)
        rememberMeCheckbox   = findViewById(R.id.rememberMe)
        adminLoginCheckbox   = findViewById(R.id.adminLogin)
        progressBar       = findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            if (validateForm()) performFirebaseLogin()
        }

        signInButton.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
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
        val email    = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        emailLayout.error    = null
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
        val email    = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        progressBar.visibility = View.VISIBLE
        toggleUiState(false)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid

                    if (adminLoginCheckbox.isChecked) {
                        // Verificar rol en Firestore
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                progressBar.visibility = View.GONE
                                toggleUiState(true)

                                val role = doc.getString("role")
                                if (role == "admin") {
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("destination", "admin_dashboard")
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    auth.signOut()
                                    Toast.makeText(this, "No tienes permisos de administrador", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                toggleUiState(true)
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        progressBar.visibility = View.GONE
                        toggleUiState(true)
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    progressBar.visibility = View.GONE
                    toggleUiState(true)
                    Toast.makeText(baseContext, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun toggleUiState(enabled: Boolean) {
        loginButton.isEnabled      = enabled
        emailEditText.isEnabled    = enabled
        passwordEditText.isEnabled = enabled
    }
}