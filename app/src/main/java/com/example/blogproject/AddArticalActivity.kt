package com.example.blogproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.blogproject.databinding.ActivityAddArticalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddArticalActivity : AppCompatActivity() {

    private val binding: ActivityAddArticalBinding by lazy {
        ActivityAddArticalBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Back Button Functionality
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnAddBlog.setOnClickListener {
            val title = binding.etBlogTitle.text.toString().trim()
            val description = binding.etBlogDescription.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (user == null) {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = user.uid
            val firestore = FirebaseFirestore.getInstance()

            // 1. Fetch User Details (Name & Image) first
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    var userName = "Unknown User"
                    var userImage = ""

                    if (document != null && document.exists()) {
                         userName = document.getString("name") ?: "Unknown User"
                         userImage = document.getString("profileImage") ?: ""
                    }

                    // 2. Create the Blog Map with ALL details
                    val blogMap = hashMapOf(
                        "heading" to title,
                        "post" to description,
                        "userId" to userId,
                        "userName" to userName,
                        "profileImage" to userImage,
                        "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        "likeCount" to 0,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    // 3. Save to Firestore
                    firestore.collection("blogs")
                        .add(blogMap)
                        .addOnSuccessListener {
                            showSuccessDialog()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add blog: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Your blog has been successfully added!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish() // Go back to MainActivity
            }
            .setCancelable(false)
            .show()
    }
}
