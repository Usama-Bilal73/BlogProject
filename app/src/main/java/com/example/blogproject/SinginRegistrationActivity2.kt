package com.example.blogproject

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogproject.databinding.ActivitySinginRegistration2Binding

class SinginRegistrationActivity2 : AppCompatActivity() {
    private val binding: ActivitySinginRegistration2Binding by lazy {
        ActivitySinginRegistration2Binding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val action: String? = intent.getStringExtra("action")

        // Adjust visibility based on action
        if (action == "login") {
            binding.loginButton.visibility = View.VISIBLE
            binding.loginEmailAddress.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            
            binding.registerButton.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.registerName.visibility = View.GONE
            binding.registerCardView.visibility = View.GONE // Hide user image in login
        } else if (action == "register") {
            binding.loginButton.visibility = View.GONE
            binding.loginEmailAddress.visibility = View.GONE
            binding.loginPassword.visibility = View.GONE
            
            binding.registerButton.visibility = View.VISIBLE
            binding.registerEmail.visibility = View.VISIBLE
            binding.registerPassword.visibility = View.VISIBLE
            binding.registerName.visibility = View.VISIBLE
            binding.registerCardView.visibility = View.VISIBLE // Show user image in register
        }
    }
}
