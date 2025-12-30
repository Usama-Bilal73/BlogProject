package com.example.blogproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.blogproject.Register.WelcomeActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences("BlogProjectPrefs", MODE_PRIVATE)
            val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

            if (isFirstTime) {
                // First time launch, go to WelcomeActivity
                startActivity(Intent(this, WelcomeActivity::class.java))
                sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
            } else {
                // Not the first time, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 2000) // 2 second delay
    }
}
