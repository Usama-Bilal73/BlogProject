package com.example.blogproject.Register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.blogproject.R
import com.example.blogproject.SinginRegistrationActivity2
import com.example.blogproject.databinding.ActivityWellcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWellcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Re-enabled for modern UI

        // Explicitly set status bar color after enabling edge-to-edge
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        binding = ActivityWellcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Login button
        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this, SinginRegistrationActivity2::class.java)
            intent.putExtra("action", "login")
            startActivity(intent)
        }

        // Register button
        binding.buttonRegister.setOnClickListener {
            val intent = Intent(this, SinginRegistrationActivity2::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)
        }
    }
}
