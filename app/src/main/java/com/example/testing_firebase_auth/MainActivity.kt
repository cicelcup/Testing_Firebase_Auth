package com.example.testing_firebase_auth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.testing_firebase_auth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth




class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Message MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        auth = FirebaseAuth.getInstance()

        Log.i(TAG, "User: ${auth.currentUser}")

        with(binding) {
            signUpButton.setOnClickListener {

            }

            signInButton.setOnClickListener {

            }

            emailValidationButton.setOnClickListener {

            }

            accountValidationButton.setOnClickListener {

            }

            updateAccountButton.setOnClickListener {

            }

            signOutButton.setOnClickListener {

            }

            deleteAccountButton.setOnClickListener {

            }
        }
    }
}
