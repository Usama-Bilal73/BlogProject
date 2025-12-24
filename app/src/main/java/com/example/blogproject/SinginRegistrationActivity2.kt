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
import com.example.blogproject.databinding.ActivitySinginRegistration2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class SinginRegistrationActivity2 : AppCompatActivity() {

    private val binding: ActivitySinginRegistration2Binding by lazy {
        ActivitySinginRegistration2Binding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
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
        database = FirebaseDatabase.getInstance()

        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        val action = intent.getStringExtra("action")
        if (action == "login") {
            setupLoginUI()
        } else {
            setupRegisterUI()
        }
    }

    /* ---------------- LOGIN ---------------- */

    private fun setupLoginUI() {
        binding.loginButton.visibility = View.VISIBLE
        binding.loginEmailAddress.visibility = View.VISIBLE
        binding.loginPassword.visibility = View.VISIBLE

        binding.registerButton.visibility = View.GONE
        binding.registerEmail.visibility = View.GONE
        binding.registerPassword.visibility = View.GONE
        binding.registerName.visibility = View.GONE
        binding.registerCardView.visibility = View.GONE

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmailAddress.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    /* ---------------- REGISTER ---------------- */

    private fun setupRegisterUI() {
        binding.loginButton.visibility = View.GONE
        binding.loginEmailAddress.visibility = View.GONE
        binding.loginPassword.visibility = View.GONE

        binding.registerButton.visibility = View.VISIBLE
        binding.registerEmail.visibility = View.VISIBLE
        binding.registerPassword.visibility = View.VISIBLE
        binding.registerName.visibility = View.VISIBLE
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

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val uid = auth.currentUser!!.uid
                        val imageString =
                            if (imageUri != null) encodeImageToBase64(imageUri!!) else ""

                        saveUserData(uid, name, email, password, imageString)

                    } else {

                        when (task.exception) {

                            is FirebaseAuthUserCollisionException -> {
                                Toast.makeText(
                                    this,
                                    "This email is already registered. Please login.",
                                    Toast.LENGTH_LONG
                                ).show()
                                setupLoginUI()
                            }

                            else -> {
                                Toast.makeText(
                                    this,
                                    task.exception?.message ?: "Registration failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }
    }

    /* ---------------- HELPERS ---------------- */

    private fun encodeImageToBase64(uri: Uri): String {
        return try {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    private fun saveUserData(
        userId: String,
        name: String,
        email: String,
        password: String,
        image: String
    ) {
        val ref = database.getReference("users").child(userId)

        val userData = UserData(
            name = name,
            email = email,
            password = password,
            profileImage = image
        )

        ref.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                auth.currentUser?.delete()
                Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
