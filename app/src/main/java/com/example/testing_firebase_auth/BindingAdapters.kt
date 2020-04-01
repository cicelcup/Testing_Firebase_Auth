package com.example.testing_firebase_auth

import android.view.View
import androidx.databinding.BindingAdapter
import com.google.firebase.auth.FirebaseUser

@BindingAdapter(value = ["app:disableIf", "app:buttonType"], requireAll = true)
fun disableIf(view: View, currentUser: FirebaseUser?, buttonType: Int) {
    when (buttonType) {
        1 -> view.isEnabled = currentUser == null
        2 -> view.isEnabled = currentUser != null
        3 -> view.isEnabled = currentUser != null && currentUser.isEmailVerified != true
        4 -> view.isEnabled = currentUser?.isEmailVerified == true
    }
}