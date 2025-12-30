package com.example.blogproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.blogproject.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Load user data
        loadUserData()

        // "Add new article" button click
        binding.btnAddArticle.setOnClickListener {
            startActivity(Intent(this, AddArticalActivity::class.java))
        }

        // "Log out" button click
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            // Redirect to WelcomeActivity
            val intent = Intent(this, com.example.blogproject.Register.WelcomeActivity::class.java)
            // Clear the back stack so the user can't go back to the profile
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(UserData::class.java)
                    if (user != null) {
                        binding.tvUserName.text = user.name
                        if (!user.profileImage.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(user.profileImage, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Glide.with(this).load(bitmap).into(binding.ivProfileImage)
                            } catch (e: Exception) {
                                // Handle potential decoding errors or fallback to URL if needed
                                Glide.with(this).load(user.profileImage).into(binding.ivProfileImage)
                            }
                        }
                    }
                }
            }
    }
}
