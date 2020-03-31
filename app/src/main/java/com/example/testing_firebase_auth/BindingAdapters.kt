package com.example.testing_firebase_auth

import android.view.View
import androidx.databinding.BindingAdapter
import com.google.firebase.auth.FirebaseUser

@BindingAdapter(value = ["app:disableIf", "app:buttonType"], requireAll = true)
fun disableIf(view: View, currentUser: FirebaseUser?, buttonType: Int) {
    if (buttonType == 1) {
        view.isEnabled = currentUser == null
    } else if (buttonType == 2) {
        view.isEnabled = currentUser != null
    }
}