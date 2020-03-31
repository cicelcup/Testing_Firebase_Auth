package com.example.testing_firebase_auth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.testing_firebase_auth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Message MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        auth = FirebaseAuth.getInstance()
        email = "cicelcup@gmail.com"
        password = "123456"
        name = "Augusto"

        Log.i(TAG, "User: ${auth.currentUser?.isEmailVerified}")

        with(binding) {

            updateLabel()

            signUpButton.setOnClickListener {
                signUp(email, password)
            }

            signInButton.setOnClickListener {
                signIn(email, password)
            }

            emailValidationButton.setOnClickListener {
                sendEmail()
            }

            accountValidationButton.setOnClickListener {

            }

            resetPasswordButton.setOnClickListener {

            }

            updateAccountButton.setOnClickListener {
                updateProfile(name)
            }

            signOutButton.setOnClickListener {
                signOut()
            }

            deleteAccountButton.setOnClickListener {

            }
        }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayLogAndToast("User created")
                    updateLabel()
                } else {
                    displayLogAndToast("Failure user creation ${task.exception}")
                }
            }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayLogAndToast("Sign in successful")
                    updateLabel()
                } else {
                    displayLogAndToast("Failure Sign In ${task.exception}")
                }
            }
    }

    private fun sendEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayLogAndToast("Email validation sent")
                    updateLabel()
                } else {
                    displayLogAndToast("Failure email validation ${task.exception}")
                }
            }
    }

    private fun updateProfile(name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayLogAndToast("Profile Updated")
                    updateLabel()
                } else {
                    displayLogAndToast("Failure update profile ${task.exception}")
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        displayLogAndToast("User signed out")
        updateLabel()
    }

    private fun displayLogAndToast(message: String) {
        Log.i(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateLabel() {
        binding.information = "User: ${auth.currentUser ?: "Not user"} " +
                "/ Name: ${auth.currentUser?.displayName ?: "Not name"} " +
                "/ EmailValidate: ${auth.currentUser?.isEmailVerified ?: "Not email"}"
    }
}
