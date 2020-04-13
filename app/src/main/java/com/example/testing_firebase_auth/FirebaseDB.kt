package com.example.testing_firebase_auth

import android.app.Application
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.impl.resolve.constants.NullValue

class FirebaseDB(application: Application) {
    //Constant for Log
    companion object {
        const val TAG = "JAPM FirebaseDB"
    }

    //Variables for ui referenced in layout
    val information = MutableLiveData<SpannableStringBuilder>()

    val data = MutableLiveData<String>()

    //Variables for controlling ui referenced in layout
    val userValidated = MutableLiveData<Boolean>()

    /* Firebase variables */

    //Auth Variable
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser = MutableLiveData(auth.currentUser)

    //Firebase Instance
    private val firebaseDB = FirebaseDatabase.getInstance()

    //Data Reference
    private val dataTestReference = firebaseDB.getReference("dataTest")

    //User Reference
    private val userValidationReference = firebaseDB.getReference("users")

    //Firebase Listeners
    private var firebaseListener: ValueEventListener? = null
    private var validUserListener: ValueEventListener? = null

    private var cont = Random.nextInt(0, 100)
    private var dataToSend: String = "Information sent it $cont"

    private val applicationReceived = application

    init {
        if (currentUser.value?.uid != null) {
            Log.i(TAG, "Prueba ${currentUser.value?.uid}")
            signOut()
        } else {
            updateUI()
            updateData("No data received")
        }
    }

    //Sign up with the correspond email and password
    fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "User created",
                    "Fails creating user ${task.exception}",
                    "signUp"
                )
            }
    }

    //Sign in with the correspond email and password
    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "Sign in successful",
                    "Fails signing in ${task.exception}",
                    "signIn"
                )
            }
    }

    //Sign out function
    fun signOut() {
        removeUserValidListener()
        //sign out the user
        auth.signOut()
        //update the current value
        currentUser.value = auth.currentUser
        //Display the message
        displayLogAndToast("User signed out successfully")
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
                    "Fails sending email validation ${task.exception}",
                    "sendEmail"
                )
            }
        signOut()
    }

    // Validate User Function
    fun validateUser(dataToSend: Boolean) {
        sendDataToFirebase(FirebaseUsers(active = dataToSend), userValidationReference)
    }

    //Reset Password Function
    fun resetPassword(email: String) {
        auth.setLanguageCode("es")
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                taskListener(
                    task, "Recovery email sent successful",
                    "Fails sending recovery email ${task.exception}",
                    "resetPassword"
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
                    "Fails updating password ${task.exception}",
                    "updatePassWord"
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
                    "Fails updating profile ${task.exception}",
                    "updateAccount"
                )
            }
    }

    //Delete user function
    fun deleteAccount() {
        currentUser.value?.delete()
            ?.addOnCompleteListener { task ->
                taskListener(
                    task, "User Deleted",
                    "Fails deleting user ${task.exception}",
                    "deleteAccount"
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
                    "Fails sending data ${task.exception}",
                    "sendDataToFirebase"
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
                        .getValue(T::class.java) ?: NullValue()
                    val fieldRequested =
                        readUnknownProperty(dataReceived, propertyName) ?: "No data received"
                    updateData(fieldRequested.toString())
                    displayLogAndToast(fieldRequested.toString())

                    reference.child(currentUser.value?.uid.toString())
                        .removeEventListener(firebaseListener!!)
                    firebaseListener = null
                }

            }
        }
        reference.child(currentUser.value?.uid.toString())
            .addListenerForSingleValueEvent(firebaseListener!!)
    }

    //Check valid user
    fun addUserValidListener() {
        if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {
            if (validUserListener == null) {
                validUserListener = object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                        displayLogAndToast("Fails validating user $databaseError")
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val dataReceived = dataSnapshot
                            .getValue(FirebaseUsers::class.java)?.active
                        userValidated.value = dataReceived
                        displayLogAndToast("User checked")
                        updateUI()
                    }
                }
            }

            userValidationReference.child(currentUser.value?.uid.toString())
                .addValueEventListener(validUserListener!!)
        }
    }

    fun removeUserValidListener() {
        if (validUserListener != null) {
            userValidationReference.child(currentUser.value?.uid.toString())
                .removeEventListener(validUserListener!!)
            validUserListener = null
            userValidated.value = null
        }
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
        failureMessage: String,
        whoCalls: String
    ) {
        if (task.isSuccessful) {
            //updating the current user variable globally
            currentUser.value = auth.currentUser
            displayLogAndToast(successMessage)
            updateUI()
            if (whoCalls == "signIn") addUserValidListener()
            if (whoCalls == "signUp") validateUser(false)
        } else {
            displayLogAndToast(failureMessage)
        }
    }

    //Log the message parameter
    private fun displayLogAndToast(message: String) {
        Log.i(TAG, message)
        Toast.makeText(applicationReceived, message, Toast.LENGTH_SHORT).show()
    }

    //Update the data received
    private fun updateData(dataReceived: String) {
        data.value = dataReceived
    }

    //Update the label in the UI to show the current information
    private fun updateUI() {
        // Variable to format the string in different ways
        val sp = formatAllText()

        //Update the ui
        information.value = sp
    }

    //Format all the UI with different colors
    private fun formatAllText(): SpannableStringBuilder {
        val sp = SpannableStringBuilder()

        var text = "User: ${currentUser.value?.uid ?: "Not user"}\n"
        sp.append(formatText(text, 5))

        text = "Name: ${currentUser.value?.displayName ?: "Not name"} "
        sp.append(formatText(text, 5))

        text = "/ EmailValidate: ${currentUser.value?.isEmailVerified ?: "Not email"}\n"
        sp.append(formatText(text, 16))

        text = "User Validated: ${userValidated.value ?: "Not Validated"}"
        sp.append(formatText(text, 15))

        return sp
    }

    //Format text with different colors
    private fun formatText(text: String, length: Int): SpannableStringBuilder {

        val smallSp = SpannableStringBuilder()
        smallSp.append(text)

        val colorText = ForegroundColorSpan(getColor(applicationReceived, R.color.colorPrimary))
        smallSp.setSpan(colorText, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return smallSp
    }
}