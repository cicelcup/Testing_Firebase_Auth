package com.example.testing_firebase_auth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.testing_firebase_auth.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
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

        with(binding) {

            updateLabel()

            signUpButton.setOnClickListener { signUp(email, password) }

            signInButton.setOnClickListener { signIn(email, password) }

            emailValidationButton.setOnClickListener { sendEmail() }

            accountValidationButton.setOnClickListener {

            }

            resetPasswordButton.setOnClickListener {

            }

            updateAccountButton.setOnClickListener { updateProfile(name) }

            signOutButton.setOnClickListener { signOut() }

            deleteAccountButton.setOnClickListener { deleteUser() }
        }
    }

    //Sign up with the correspond email and password
    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "User created",
                    "Failure user creation ${task.exception}"
                )
            }
    }

    //Sign in with the correspond email and password
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "Sign in successful",
                    "Failure Sign In ${task.exception}"
                )
            }
    }

    //Send the email for validation
    private fun sendEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "Email validation sent",
                    "Failure email validation ${task.exception}"
                )
            }
    }

    //update profile function. It creates a profile update with the correspond information
    private fun updateProfile(name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "Profile Updated",
                    "Failure update profile ${task.exception}"
                )
            }
    }

    //Sign out function
    private fun signOut() {
        auth.signOut()
        displayLogAndToast("User signed out")
        updateLabel()
    }

    private fun deleteUser() {
        auth.currentUser?.delete()
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "User Deleted",
                    "Failure delete user ${task.exception}"
                )
            }
    }

    //Generic function receiving any kind of Task for displaying and log the result
    private fun <T : Any?> taskListener(
        task: Task<T>,
        successMessage: String,
        failureMessage: String
    ) {
        if (task.isSuccessful) {
            displayLogAndToast(successMessage)
            updateLabel()
        } else {
            displayLogAndToast(failureMessage)
        }
    }

    //Log the message parameter
    private fun displayLogAndToast(message: String) {
        Log.i(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    //Update the label in the UI to show the current information
    private fun updateLabel() {
        binding.information = "User: ${auth.currentUser ?: "Not user"} " +
                "/ Name: ${auth.currentUser?.displayName ?: "Not name"} " +
                "/ EmailValidate: ${auth.currentUser?.isEmailVerified ?: "Not email"}"
    }
}
