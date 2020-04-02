package com.example.testing_firebase_auth

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class FirebaseDB(application: Application) {
    //Constant for Log
    companion object {
        const val TAG = "JAPM FirebaseDB"
    }

    //Variables for ui referenced in layout
    val information = MutableLiveData<String>()

    val data = MutableLiveData<String>()

    //Variables for controlling ui referenced in layout
    val userValidated = MutableLiveData(false)

    /* Firebase variables */

    //Auth Variable
    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser = MutableLiveData(auth.currentUser)

    //Firebase Instance
    private val firebaseDB = FirebaseDatabase.getInstance()

    //Data Reference
    private val dataTestReference = firebaseDB.getReference("dataTest")

    //User Reference
    private val userValidationReference = firebaseDB.getReference("users")

    //Firebase Listener
    private var firebaseListener: ValueEventListener? = null

    private var cont = Random.nextInt(0, 100)
    private var dataToSend: String = "Information sent it $cont"

    private val applicationReceived = application

    init {
        updateUI()
        updateData("No data received")
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
        //sign out the user
        auth.signOut()
        //update the current value
        currentUser.value = auth.currentUser
        //Display the message
        displayLogAndToast("User signed out")
        //Disable the user validated button
        userValidated.value = false
        //update the UI
        updateUI()
        //update the data received
        updateData("No data received")
    }

    //Send the email for validation
    fun sendEmail() {
        auth.setLanguageCode("es")
        currentUser.value?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "Email validation sent",
                    "Fails sending email validation ${task.exception}"
                )
            }
        signOut()
    }

    // Validate User Function
    fun validateUser(dataToSend: Boolean) {
        sendDataToFirebase(FirebaseUsers(active = dataToSend), userValidationReference)
        readDataFromFirebase<FirebaseUsers>(userValidationReference, "active")
    }

    //Reset Password Function
    fun resetPassword(email: String) {
        auth.setLanguageCode("es")
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "Recovery email sent successful",
                    "Fails sending recovery email ${task.exception}"
                )
            }
        signOut()
    }

    //Update Password Function
    fun updatePassword(newPassword: String) {
        currentUser.value?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "Password Updated",
                    "Fails updating password ${task.exception}"
                )
            }
    }

    //update profile function. It creates a profile update with the correspond information
    fun updateAccount(name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        currentUser.value?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "Profile Updated",
                    "Fails updating profile ${task.exception}"
                )
            }
    }

    //Delete user function
    fun deleteAccount() {
        currentUser.value?.delete()
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "User Deleted",
                    "Fails deleting user ${task.exception}"
                )
            }
        updateData("No data received")
    }

    //Send Data function
    fun sendData() {
        sendDataToFirebase(FirebaseData(data = dataToSend), dataTestReference)
        cont = Random.nextInt(0, 100); dataToSend = "Information sent it $cont"
    }

    fun readData() {
        readDataFromFirebase<FirebaseData>(dataTestReference, "data")
    }

    //Send any data to firebase to any reference
    private fun sendDataToFirebase(firebaseData: Any, reference: DatabaseReference) {
        reference.child(currentUser.value?.uid.toString()).setValue(firebaseData)
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
                        userValidated.value = if (fieldRequested == null) false
                        else fieldRequested as Boolean
                    } else {
                        updateData(fieldRequested.toString())
                        displayLogAndToast(fieldRequested.toString())
                    }

                    reference.child(currentUser.value?.uid.toString())
                        .removeEventListener(firebaseListener!!)
                    firebaseListener = null
                }

            }
        }
        reference.child(currentUser.value?.uid.toString())
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

    //Generic function receiving any kind of Task for displaying and log the result
    private fun <T : Any?> taskListener(
        task: Task<T>,
        successMessage: String,
        failureMessage: String
    ) {
        if (task.isSuccessful) {
            //updating the current user variable globally
            currentUser.value = auth.currentUser
            displayLogAndToast(successMessage)
            updateUI()
        } else {
            displayLogAndToast(failureMessage)
        }
    }

    //Update the data received
    private fun updateData(dataReceived: String) {
        data.value = dataReceived
    }

    //Update the label in the UI to show the current information
    private fun updateUI() {
        information.value = "User: ${currentUser.value?.uid ?: "Not user"} \n" +
                "Name: ${currentUser.value?.displayName ?: "Not name"} " +
                "/ EmailValidate: ${currentUser.value?.isEmailVerified ?: "Not email"}"
    }

    //Log the message parameter
    private fun displayLogAndToast(message: String) {
        Log.i(FirebaseViewModel.TAG, message)
        Toast.makeText(applicationReceived, message, Toast.LENGTH_SHORT).show()
    }
}