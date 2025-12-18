package com.example.blogproject.Register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogproject.R
import com.example.blogproject.SinginRegistrationActivity2
import com.example.blogproject.databinding.ActivityWellcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWellcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWellcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

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
