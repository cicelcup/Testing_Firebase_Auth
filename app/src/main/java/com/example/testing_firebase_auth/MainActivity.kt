package com.example.testing_firebase_auth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.testing_firebase_auth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Message MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        auth = FirebaseAuth.getInstance()

        Log.i(TAG, "User: ${auth.currentUser?.isEmailVerified}")

        with(binding) {
            signUpButton.setOnClickListener {
                email = "cicelcup@gmail.com"
                password = "123456"
                signUp(email, password)
            }

            signInButton.setOnClickListener {

            }

            emailValidationButton.setOnClickListener {
                sendEmail()
            }

            accountValidationButton.setOnClickListener {

            }

            updateAccountButton.setOnClickListener {

            }

            signOutButton.setOnClickListener {

            }

            deleteAccountButton.setOnClickListener {

            }
        }
    }

    private fun sendEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Email Validation sent")
                } else {
                    Log.i(TAG, "Failure Email Validation ${task.exception}")
                }
            }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "User Created")
                } else {
                    Log.i(TAG, "Failure User Creation ${task.exception}")
                }
            }
    }
}
