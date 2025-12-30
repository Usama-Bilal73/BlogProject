package com.example.blogproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.blogproject.Register.WelcomeActivity
import com.example.blogproject.databinding.ActivitySinginRegistration2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class SinginRegistrationActivity2 : AppCompatActivity() {

    private val binding by lazy {
        ActivitySinginRegistration2Binding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var imageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(binding.registerUserImage)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        val action = intent.getStringExtra("action")
        if (action == "login") {
            setupLoginUI()
        } else {
            setupRegisterUI()
        }
    }

    private fun setupLoginUI() {
        binding.loginButton.visibility = View.VISIBLE
        binding.loginEmailAddress.visibility = View.VISIBLE
        binding.loginPassword.visibility = View.VISIBLE
        
        binding.registerButton.visibility = View.GONE
        binding.registerName.visibility = View.GONE
        binding.registerEmail.visibility = View.GONE
        binding.registerPassword.visibility = View.GONE
        binding.registerCardView.visibility = View.GONE
        
        // Ensure switch text view is visible (if present in XML)
        // Note: The previous turn removed the reference to 'tvSwitch' due to errors.
        // If the user hasn't updated the XML to include 'tvSwitch', we can't reference it here safely.
        // But the user's latest request is about adding imports, implying the file is being edited.
        // The user didn't ask to re-add the switch logic, so I will stick to fixing imports or missing code if any.

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmailAddress.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent double clicks
            binding.loginButton.isEnabled = false
            binding.loginButton.text = "Logging In..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Re-enable button on failure
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Login"
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setupRegisterUI() {
        binding.loginButton.visibility = View.GONE
        binding.loginEmailAddress.visibility = View.GONE
        binding.loginPassword.visibility = View.GONE
        
        binding.registerButton.visibility = View.VISIBLE
        binding.registerName.visibility = View.VISIBLE
        binding.registerEmail.visibility = View.VISIBLE
        binding.registerPassword.visibility = View.VISIBLE
        binding.registerCardView.visibility = View.VISIBLE

        binding.registerCardView.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.registerButton.setOnClickListener {
            val name = binding.registerName.text.toString().trim()
            val email = binding.registerEmail.text.toString().trim()
            val password = binding.registerPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Just disable the button, keep text as "Register"
            binding.registerButton.isEnabled = false
            // binding.registerButton.text = "Registering..." // Removed this line

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        val image = if (imageUri != null) encodeImageToBase64(imageUri!!) else ""
                        saveUserDataToFirestore(uid, name, email, image)
                    } else {
                        // Re-enable button on failure
                        binding.registerButton.isEnabled = true
                        binding.registerButton.text = "Register"
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun encodeImageToBase64(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            // Further reduced quality to 30% to prevent "The value is too large" error
            // Also resizing the bitmap if it's too large
            val scaledBitmap = getResizedBitmap(bitmap, 500) // Max 500px width/height
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun saveUserDataToFirestore(
        userId: String,
        name: String,
        email: String,
        imageBase64: String
    ) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "profileImage" to imageBase64
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                
                // Directly navigate to WelcomeActivity (login page) after registration
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish() // Close current activity so user can't go back with back button
            }
            .addOnFailureListener { e ->
                // Clean up the auth user if database write fails
                auth.currentUser?.delete()
                
                // Re-enable button on failure
                binding.registerButton.isEnabled = true
                binding.registerButton.text = "Register"
                Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
