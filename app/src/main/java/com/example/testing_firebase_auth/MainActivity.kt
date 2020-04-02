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
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class MainActivity : AppCompatActivity() {

    //Constant for Log
    companion object {
        const val TAG = "Message MainActivity"
    }

    //Binding Variable
    private lateinit var binding: ActivityMainBinding

    //Auth Variable
    private lateinit var auth: FirebaseAuth

    //User Information Variables
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name: String
    private lateinit var dataToSend: String
    private var cont = Random.nextInt(0, 100)

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

        //example information variables
        email = "cicelcup@gmail.com"
        password = "123456"
        name = "Augusto"
        dataToSend = "Information sent it $cont"

        with(binding) {

            updateUI()
            updateData("No data")

            signUpButton.setOnClickListener { signUp(email, password) }

            signInButton.setOnClickListener { signIn(email, password) }

            emailValidationButton.setOnClickListener { sendEmail() }

            accountValidationButton.setOnClickListener { validateUser(true) }

            accountInvalidationButton.setOnClickListener { validateUser(false) }

            resetPasswordButton.setOnClickListener { resetPassword() }

            updateAccountButton.setOnClickListener { updateProfile(name) }

            updatePassWordButton.setOnClickListener { updatePassword() }

            signOutButton.setOnClickListener { signOut() }

            deleteAccountButton.setOnClickListener { deleteUser() }

            sendDataButton.setOnClickListener { sendData(dataToSend) }

            readDataButton.setOnClickListener { listenData() }
        }
    }

    private fun updatePassword() {
        auth.currentUser?.updatePassword("123456")
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "Password Updated",
                    "Failure update password ${task.exception}"
                )
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
        readDataFromFirebase<FirebaseUsers>(userValidationReference, "active")
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
            binding.userValidated = false
            updateUI()
            updateData("No data")
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
    private fun sendData(data: String) {
        sendDataToFirebase(FirebaseData(data = data), dataTestReference)
        cont = Random.nextInt(0, 100); dataToSend = "Information sent it $cont"
    }

    //Listening Data
    private fun listenData() {
        readDataFromFirebase<FirebaseData>(dataTestReference, "data")
    }

    //Read data from firebase
    private inline fun <reified T> readDataFromFirebase(
        reference: DatabaseReference,
        propertyName: String
    ) {
        if (firebaseListener == null) {
            firebaseListener = object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    displayLogAndToast("Error reading data $databaseError")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val dataReceived = dataSnapshot
                        .getValue(T::class.java) ?: "No data received"
                    val fieldRequested = readUnknownProperty(dataReceived, propertyName)
                    //Temporary check until view model is implemented
                    if (propertyName == "active") {
                        if (fieldRequested == null) binding.userValidated = false
                        else binding.userValidated = fieldRequested as Boolean
                    } else {
                        updateData(fieldRequested.toString())
                        displayLogAndToast(fieldRequested.toString())
                    }

                    reference.child(auth.uid.toString())
                        .removeEventListener(firebaseListener!!)
                }

            }
        }
        reference.child(auth.uid.toString())
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
            information = "User: ${auth.currentUser?.uid ?: "Not user"} \n" +
                    "Name: ${auth.currentUser?.displayName ?: "Not name"} " +
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

    //Using reflection to check any field value from a class that is unknown
    private fun readUnknownProperty(dataObject: Any, propertyName: String): Any? {
        val property = dataObject::class.memberProperties.find {
            it.name == propertyName
        }
        return property?.getter?.call(dataObject)
    }

    //Using reflection to check any field value from a class that is unknown
    //It's not used in the current project but it could works in others projects
    private fun setUnknownProperty(dataObject: Any, propertyName: String, propertyValue: Any) {
        val property = dataObject::class.memberProperties.find {
            it.name == propertyName
        }
        if (property == null) {
            Log.i(TAG, "Not property found it")
            return
        }

        if (property is KMutableProperty<*>) {
            property.setter.call(dataObject, propertyValue)
        } else {
            Log.i(TAG, "Property not mutable")
        }
    }
}
