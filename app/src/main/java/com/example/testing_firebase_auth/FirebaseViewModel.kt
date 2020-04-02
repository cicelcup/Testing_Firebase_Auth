package com.example.testing_firebase_auth

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class FirebaseViewModel(application: Application) : AndroidViewModel(application) {

    //Constant for Log
    companion object {
        const val TAG = "JAPM FirebaseViewModel"
    }

    //Variables for ui referenced in layout
    lateinit var information: String
    lateinit var data: String

    //Variables for controlling ui referenced in layout
    var userValidated: Boolean = false

    /* Firebase variables */

    //Auth Variable
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    //Current User Variable
    var currentUser: FirebaseUser? = auth.currentUser

    //Firebase Instance
    private val firebaseDB = FirebaseDatabase.getInstance()

    //Data Reference
    private val dataTestReference = firebaseDB.getReference("dataTest")

    //User Reference
    private val userValidationReference = firebaseDB.getReference("users")

    //Firebase Listener
    private var firebaseListener: ValueEventListener? = null

    //User Information Variables (just as example of this project)
    var email = "cicelcup@gmail.com"
    var password = "123456"
    var name = "Jorge Augusto"
    private var cont = Random.nextInt(0, 100)
    private var dataToSend: String = "Information sent it $cont"

    init {
        updateUI()
        updateData("No data")
    }

    //Sign up with the correspond email and password
    fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "User created",
                    "Fails creating user ${task.exception}"
                )
            }
    }

    //Sign in with the correspond email and password
    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "Sign in successful",
                    "Fails signing in ${task.exception}"
                )
            }
    }

    //Sign out function
    fun signOut() {
        if (currentUser != null) {
            auth.signOut()
            displayLogAndToast("User signed out")
            userValidated = false
            updateUI()
            updateData("No data")
        } else {
            displayLogAndToast("Fails signing out. User not auth")
        }
    }

    //Send the email for validation
    fun sendEmail() {
        if (currentUser != null) {
            auth.setLanguageCode("es")
            currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    taskListener(
                        task, "Email validation sent",
                        "Fails sending email validation ${task.exception}"
                    )
                }
        } else {
            displayLogAndToast("Fails sending email validation. User not auth")
        }
    }

    // Validate User Function
    fun validateUser(dataToSend: Boolean) {
        sendDataToFirebase(FirebaseUsers(active = dataToSend), userValidationReference)
        readDataFromFirebase<FirebaseUsers>(userValidationReference, "active")
    }

    //Reset Password Function
    fun resetPassword() {
        if (currentUser != null) {
            auth.setLanguageCode("es")
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    taskListener(
                        task, "Recovery email sent successful",
                        "Fails sending recovery email ${task.exception}"
                    )
                }
            auth.signOut()
        } else {
            displayLogAndToast("It's not possible to reset password. User not auth")
        }
    }

    //Update Password Function
    fun updatePassword(newPassword: String) {
        auth.currentUser?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "Password Updated",
                    "Fails updating password ${task.exception}"
                )
            }
    }

    //update profile function. It creates a profile update with the correspond information
    fun updateAccount(name: String) {
        if (currentUser != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            currentUser?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    taskListener(
                        task, "Profile Updated",
                        "Fails updating profile ${task.exception}"
                    )
                }
        } else {
            displayLogAndToast("It is not possible update profile. User not auth")
        }
    }

    //Delete user function
    fun deleteAccount() {
        if (currentUser != null) {
            currentUser?.delete()
                ?.addOnCompleteListener { task ->
                    taskListener(
                        task, "User Deleted",
                        "Fails deleting user ${task.exception}"
                    )
                }
        } else {
            displayLogAndToast("It is not possible to delete user. User not auth")
        }
    }

    //Send Data function
    fun sendData() {
        sendDataToFirebase(FirebaseData(data = dataToSend), dataTestReference)
        cont = Random.nextInt(0, 100); dataToSend = "Information sent it $cont"
    }

    //Listening Data
    fun readData() {
        readDataFromFirebase<FirebaseData>(dataTestReference, "data")
    }

    //Send any data to firebase to any reference
    private fun sendDataToFirebase(firebaseData: Any, reference: DatabaseReference) {
        reference.child(currentUser?.uid.toString()).setValue(firebaseData)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "Data sent it",
                    "Fails sending data ${task.exception}"
                )
            }
    }

    //Read data from firebase
    private inline fun <reified T> readDataFromFirebase(
        reference: DatabaseReference,
        propertyName: String
    ) {
        if (firebaseListener == null) {
            firebaseListener = object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    displayLogAndToast("Fails reading data $databaseError")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val dataReceived = dataSnapshot
                        .getValue(T::class.java) ?: "No data received"
                    val fieldRequested = readUnknownProperty(dataReceived, propertyName)
                    //Temporary check until active mode observation is implemented
                    if (propertyName == "active") {
                        userValidated = if (fieldRequested == null) false
                        else fieldRequested as Boolean
                    } else {
                        updateData(fieldRequested.toString())
                        displayLogAndToast(fieldRequested.toString())
                    }

                    reference.child(currentUser?.uid.toString())
                        .removeEventListener(firebaseListener!!)
                    firebaseListener = null
                }

            }
        }
        reference.child(currentUser?.uid.toString())
            .addListenerForSingleValueEvent(firebaseListener!!)
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

    //Update the data received
    private fun updateData(dataReceived: String) {
        data = dataReceived
    }

    //Generic function receiving any kind of Task for displaying and log the result
    private fun <T : Any?> taskListener(
        task: Task<T>,
        successMessage: String,
        failureMessage: String
    ) {
        if (task.isSuccessful) {
            //updating the current user variable globally
            currentUser = auth.currentUser
            displayLogAndToast(successMessage)
            updateUI()
        } else {
            displayLogAndToast(failureMessage)
        }
    }

    //Log the message parameter
    private fun displayLogAndToast(message: String) {
        Log.i(TAG, message)
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    //Update the label in the UI to show the current information
    private fun updateUI() {
        information = "User: ${currentUser?.uid ?: "Not user"} \n" +
                "Name: ${currentUser?.displayName ?: "Not name"} " +
                "/ EmailValidate: ${currentUser?.isEmailVerified ?: "Not email"}"
    }
}