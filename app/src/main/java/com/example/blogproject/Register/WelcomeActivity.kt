package com.example.blogproject.Register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.blogproject.SinginRegistrationActivity2
import com.example.blogproject.databinding.ActivityWellcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWellcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWellcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            val intent = Intent(this, SinginRegistrationActivity2::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this, SinginRegistrationActivity2::class.java)
            intent.putExtra("action", "login")
            startActivity(intent)
        }
    }
}
