package com.example.testing_firebase_auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.testing_firebase_auth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //Constant for Log
    companion object {
        const val TAG = "JAPM MainActivity"
    }

    //ViewModel
    private lateinit var viewModel: FirebaseViewModel

    //Binding Variable
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        binding.viewModel = viewModel

        binding.lifecycleOwner = this
    }

    override fun onResume() {
        super.onResume()
        viewModel.firebaseDB.addUserValidListener()
    }

    override fun onPause() {
        super.onPause()
        viewModel.firebaseDB.removeUserValidListener()
    }
}
