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
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Message MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name: String
    private lateinit var dataToSend: String

    //Firebase Instance
    private val firebaseDB = FirebaseDatabase.getInstance()

    //Data Reference
    private val dataTestReference = firebaseDB.getReference("dataTest")

    //User Reference
    private val userValidationReference = firebaseDB.getReference("users")

    //Firebase Listener
    private var firebaseListener: ValueEventListener? = null

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
        dataToSend = "Information sent it"

        with(binding) {

            updateUI()
            updateData("No data")

            signUpButton.setOnClickListener { signUp(email, password) }

            signInButton.setOnClickListener { signIn(email, password) }

            emailValidationButton.setOnClickListener { sendEmail() }

            accountValidationButton.setOnClickListener { validateUser(true) }

            resetPasswordButton.setOnClickListener { resetPassword() }

            updateAccountButton.setOnClickListener { updateProfile(name) }

            signOutButton.setOnClickListener { signOut() }

            deleteAccountButton.setOnClickListener { deleteUser() }

            sendDataButton.setOnClickListener { sendData(dataToSend) }

            readDataButton.setOnClickListener { listenData() }
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

    // Validate User Function
    private fun validateUser(dataToSend: Boolean) {
        sendDataToFirebase(FirebaseUsers(active = dataToSend), userValidationReference)
    }

    //Send the email for validation
    private fun sendEmail() {
        if (auth.currentUser != null) {
            auth.setLanguageCode("es")
            auth.currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    taskListener(
                        task, "Email validation sent",
                        "Failure email validation ${task.exception}"
                    )
                }
        } else {
            displayLogAndToast("Email validation is not possible. User not auth")
        }
    }

    private fun resetPassword() {
        if (auth.currentUser != null) {
            auth.setLanguageCode("es")
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    taskListener(
                        task, "Recovery email sent successful",
                        "Failure sending recovery email ${task.exception}"
                    )
                }
            auth.signOut()
        } else {
            displayLogAndToast("It's not possible to reset password. User not auth")
        }
    }

    //update profile function. It creates a profile update with the correspond information
    private fun updateProfile(name: String) {
        if (auth.currentUser != null) {
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
        } else {
            displayLogAndToast("It is not possible update profile. User not auth")
        }
    }

    //Sign out function
    private fun signOut() {
        if (auth.currentUser != null) {
            auth.signOut()
            displayLogAndToast("User signed out")
            updateUI()
        } else {
            displayLogAndToast("It is not possible to sign out. User not auth")
        }
    }

    //Delete user function
    private fun deleteUser() {
        if (auth.currentUser != null) {
            auth.currentUser?.delete()
                ?.addOnCompleteListener { task ->
                    taskListener(
                        task, "User Deleted",
                        "Failure delete user ${task.exception}"
                    )
                }
        } else {
            displayLogAndToast("It is not possible to delete user. User not auth")
        }
    }

    //Send Data function
    private fun sendData(dataToSend: String) {
        sendDataToFirebase(FirebaseData(data = dataToSend), dataTestReference)
    }

    //Listening Data
    private fun listenData() {
        if (firebaseListener == null) {
            firebaseListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    displayLogAndToast("Error reading data $p0")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val post = dataSnapshot
                        .getValue(FirebaseData::class.java)?.data ?: "No data received"
                    updateData(post)
                    displayLogAndToast(post)
                    dataTestReference.child(auth.uid.toString())
                        .removeEventListener(firebaseListener!!)
                }

            }
        }
        dataTestReference.child(auth.uid.toString())
            .addListenerForSingleValueEvent(firebaseListener!!)
    }

    //Generic function receiving any kind of Task for displaying and log the result
    private fun <T : Any?> taskListener(
        task: Task<T>,
        successMessage: String,
        failureMessage: String
    ) {
        if (task.isSuccessful) {
            displayLogAndToast(successMessage)
            updateUI()
        } else {
            displayLogAndToast(failureMessage)
        }
    }

    //Log the message parameter
    private fun displayLogAndToast(message: String) {
        Log.i(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //Update the label in the UI to show the current information
    private fun updateUI() {
        with(binding) {
            currentUser = auth.currentUser
            information = "User: ${auth.currentUser ?: "Not user"} " +
                    "/ Name: ${auth.currentUser?.displayName ?: "Not name"} " +
                    "/ EmailValidate: ${auth.currentUser?.isEmailVerified ?: "Not email"}"
        }
    }

    //Send any data to firebase to any reference
    private fun sendDataToFirebase(firebaseData: Any, reference: DatabaseReference) {
        reference.child(auth.uid.toString()).setValue(firebaseData)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "Data sent it",
                    "Error sending data ${task.exception}"
                )
            }
    }

    //Update the data received
    private fun updateData(dataReceived: String) {
        binding.data = dataReceived
    }
}
