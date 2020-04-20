package com.example.testing_firebase_auth

import com.google.firebase.database.PropertyName

data class FirebaseData(
    @get:PropertyName("V")
    @set:PropertyName("V")
    var data: String = ""
) {
}