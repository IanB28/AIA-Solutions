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
import com.google.firebase.firestore.FirebaseFirestore

class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var signUpButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backToLoginText: TextView

    private lateinit var nameCompleteEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        emailLayout           = findViewById(R.id.emailRegisterLayout)
        passwordLayout        = findViewById(R.id.passwordRegisterLayout)
        confirmPasswordLayout = findViewById(R.id.passwordConfirmLayout)
        emailEditText         = findViewById(R.id.etEmailRegister)
        passwordEditText      = findViewById(R.id.etPassRegister)
        confirmPasswordEditText = findViewById(R.id.etPassRegisterConfirm)
        signUpButton          = findViewById(R.id.btnSignUp)
        progressBar           = findViewById(R.id.progressBarSignIn)
        backToLoginText       = findViewById(R.id.tvBackToLogin)
        nameCompleteEditText  = findViewById(R.id.nameComplete)
        phoneNumberEditText   = findViewById(R.id.phoneNumberRegister)


        signUpButton.setOnClickListener {
            if (validateForm()) performSignUp()
        }

        backToLoginText.setOnClickListener { finish() }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val email           = emailEditText.text.toString().trim()
        val password        = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val nameComplete    = nameCompleteEditText.text.toString().trim()
        val phoneNumber     = phoneNumberEditText.text.toString().trim()


        emailLayout.error           = null
        passwordLayout.error        = null
        confirmPasswordLayout.error = null
        nameCompleteEditText.error  = null
        phoneNumberEditText.error   = null

        if (email.isEmpty()) {
            emailLayout.error = "Ingresa tu correo"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Ingresa un formato de correo válido"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Ingresa una contraseña"
            isValid = false
        } else if (password.length < 8) {
            passwordLayout.error = "Debe tener al menos 8 caracteres"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Repite tu contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Las contraseñas no coinciden"
            isValid = false
        }
        if (nameComplete.isEmpty()) {
            nameCompleteEditText.error = "Ingresa tu nombre completo"
            isValid = false
        }
        if (phoneNumber.isEmpty()) {
            phoneNumberEditText.error = "Ingresa tu número de teléfono"
            isValid = false
        }


        return isValid
    }

    private fun performSignUp() {
        val email    = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val nameComplete    = nameCompleteEditText.text.toString().trim()
        val phoneNumber     = phoneNumberEditText.text.toString().trim()



        progressBar.visibility  = View.VISIBLE
        signUpButton.isEnabled  = false
        emailEditText.isEnabled = false
        passwordEditText.isEnabled        = false
        confirmPasswordEditText.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid

                    // Guardar usuario en Firestore
                    val usuario = hashMapOf(
                        "email"     to email,
                        "role"      to "client",
                        "name" to nameComplete,
                        "phoneNumber" to phoneNumber,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("users").document(uid)
                        .set(usuario)
                        .addOnSuccessListener {
                            progressBar.visibility  = View.GONE
                            signUpButton.isEnabled  = true
                            emailEditText.isEnabled = true
                            passwordEditText.isEnabled        = true
                            confirmPasswordEditText.isEnabled = true

                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility  = View.GONE
                            signUpButton.isEnabled  = true
                            emailEditText.isEnabled = true
                            passwordEditText.isEnabled        = true
                            confirmPasswordEditText.isEnabled = true

                            Toast.makeText(this, "Error al guardar usuario: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    progressBar.visibility  = View.GONE
                    signUpButton.isEnabled  = true
                    emailEditText.isEnabled = true
                    passwordEditText.isEnabled        = true
                    confirmPasswordEditText.isEnabled = true

                    Toast.makeText(baseContext, "Fallo en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}