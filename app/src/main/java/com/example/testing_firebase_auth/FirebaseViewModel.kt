package com.example.testing_firebase_auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser

class FirebaseViewModel(application: Application) : AndroidViewModel(application) {

    //Constant for Log
    companion object {
        const val TAG = "JAPM FirebaseViewModel"
    }

    //Firebase DB class access
    private val firebaseDB = FirebaseDB(application)

    //Variables for ui referenced in layout
    val information: LiveData<String> = firebaseDB.information

    val data: LiveData<String> = firebaseDB.data

    //Variables for controlling ui referenced in layout
    val userValidated: LiveData<Boolean> = firebaseDB.userValidated

    //Current User Variable
    val currentUser: LiveData<FirebaseUser?> = firebaseDB.currentUser

    //User Information Variables (just as example of this project)
    var email = "cicelcup@gmail.com"
    var password = "123456"
    var name = "Jorge Augusto"

    //Sign up with the correspond email and password
    fun signUp(email: String, password: String) {
        firebaseDB.signUp(email, password)
    }

    //Sign in with the correspond email and password
    fun signIn(email: String, password: String) {
        firebaseDB.signIn(email, password)
    }

    //Sign out function
    fun signOut() {
        firebaseDB.signOut()
    }

    //Send the email for validation
    fun sendEmail() {
        firebaseDB.sendEmail()
    }

    // Validate User Function
    fun validateUser(dataToSend: Boolean) {
        firebaseDB.validateUser(dataToSend)
    }

    //Reset Password Function
    fun resetPassword(email: String) {
        firebaseDB.resetPassword(email)
    }

    //Update Password Function
    fun updatePassword(newPassword: String) {
        firebaseDB.updatePassword(newPassword)
    }

    //update profile function. It creates a profile update with the correspond information
    fun updateAccount(name: String) {
        firebaseDB.updateAccount(name)
    }

    //Delete user function
    fun deleteAccount() {
        firebaseDB.deleteAccount()
    }

    //Send Data function
    fun sendData() {
        firebaseDB.sendData()
    }

    //Listening Data
    fun readData() {
        firebaseDB.readData()
    }
}