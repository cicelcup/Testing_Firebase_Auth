package com.example.testing_firebase_auth

import android.view.View
import androidx.databinding.BindingAdapter
import com.google.firebase.auth.FirebaseUser

@BindingAdapter(
    value = ["app:disableIf", "app:buttonType", "app:userValidated"],
    requireAll = false
)
fun disableIf(view: View, currentUser: FirebaseUser?, buttonType: Int, userValidated: Boolean?) {
    when (buttonType) {
        //User is not auth (only can sign up or sign in)
        1 -> {
            view.isEnabled = currentUser == null
        }
        //If user is auth, It always can sign out
        2 -> {
            if (userValidated != null) {
                view.isEnabled = currentUser != null
            } else {
                view.isEnabled = false
            }
        }
        //Check if the user is created and if the email is verified
        3 -> {
            if (currentUser != null) {
                view.isEnabled = currentUser.isEmailVerified == false
            } else {
                view.isEnabled = false
            }
        }
        //Check the user, if the user validated is not null and if it's validated is false
        4 -> {
            if (currentUser != null) {
                if (currentUser.isEmailVerified) {
                    if (userValidated != null) {
                        view.isEnabled = userValidated == false
                    } else {
                        view.isEnabled = false
                    }
                } else {
                    view.isEnabled = false
                }
            } else {
                view.isEnabled = false
            }
        }
        //Check the user, if the user validated is not null and if it's validated is true
        5 -> {
            if (currentUser != null) {
                if (currentUser.isEmailVerified) {
                    if (userValidated != null) {
                        view.isEnabled = userValidated == true
                    } else {
                        view.isEnabled = false
                    }
                } else {
                    view.isEnabled = false
                }
            } else {
                view.isEnabled = false
            }
        }
    }
}