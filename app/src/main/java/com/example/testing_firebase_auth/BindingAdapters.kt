package com.example.testing_firebase_auth

import android.view.View
import androidx.databinding.BindingAdapter
import com.google.firebase.auth.FirebaseUser

@BindingAdapter(
    value = ["app:disableIf", "app:buttonType", "app:userValidated"],
    requireAll = false
)
fun disableIf(view: View, currentUser: FirebaseUser?, buttonType: Int, userValidated: Boolean) {
    when (buttonType) {
        //User is not auth
        1 -> view.isEnabled = currentUser == null
        //User is auth and it doesn't matter is the email is verified or not
        2 -> view.isEnabled = currentUser != null
        //User is auth and the email is not verified
        3 -> view.isEnabled = currentUser != null && currentUser.isEmailVerified != true
        //User is auth, the email is verified, but it's not validated
        4 -> view.isEnabled = currentUser?.isEmailVerified == true && userValidated == false
        //User is auth, the email is verified and it's validated
        5 -> view.isEnabled = currentUser?.isEmailVerified == true && userValidated == true
    }
}