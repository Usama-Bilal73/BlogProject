package com.example.blogproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.blogproject.databinding.ActivityBlogDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BlogDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isLiked = false
    private var isSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val blogId = intent.getStringExtra("BLOG_ID") ?: return

        loadBlogDetails(blogId)
        checkInitialStatus(blogId)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabSave.setOnClickListener { 
            if (isSaved) {
                // Already saved, so unsave it
                toggleSave(blogId)
            } else {
                // Not saved, so save it and then go to the saved articles
                toggleSave(blogId)
                startActivity(Intent(this, SavedBlogsActivity::class.java))
            }
        }
        binding.fabLike.setOnClickListener { toggleLike(blogId) }
    }

    private fun loadBlogDetails(blogId: String) {
        firestore.collection("blogs").document(blogId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val blog = document.toObject(BlogItemModel::class.java)
                    if (blog != null) {
                        binding.tvBlogTitle.text = blog.heading
                        binding.tvBlogDescription.text = blog.post
                        binding.tvBlogDate.text = blog.date
                        binding.tvUserName.text = blog.userName

                        // Load profile image as the "blog image" or author image
                        if (!blog.profileImage.isNullOrEmpty()) {
                             try {
                                val imageBytes = Base64.decode(blog.profileImage, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Glide.with(this)
                                    .load(bitmap)
                                    .centerCrop()
                                    .placeholder(R.drawable.profile)
                                    .into(binding.ivBlogImage)
                            } catch (e: Exception) {
                                Glide.with(this)
                                    .load(blog.profileImage)
                                    .centerCrop()
                                    .placeholder(R.drawable.profile)
                                    .into(binding.ivBlogImage)
                            }
                        } else {
                            binding.ivBlogImage.setImageResource(R.drawable.profile)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkInitialStatus(blogId: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("likedBlogs").document(blogId).get()
            .addOnSuccessListener { document ->
                isLiked = document.exists()
                updateLikeIcon()
            }
        
        firestore.collection("users").document(userId).collection("savedBlogs").document(blogId).get()
            .addOnSuccessListener { document ->
                isSaved = document.exists()
                updateSaveIcon()
            }
    }

    private fun toggleSave(blogId: String) {
        val userId = auth.currentUser?.uid ?: return
        val savedBlogRef = firestore.collection("users").document(userId).collection("savedBlogs").document(blogId)

        if (isSaved) {
            savedBlogRef.delete().addOnSuccessListener {
                isSaved = false
                updateSaveIcon()
                Toast.makeText(this, "Unsaved", Toast.LENGTH_SHORT).show()
            }
        } else {
            savedBlogRef.set(mapOf("saved" to true)).addOnSuccessListener {
                isSaved = true
                updateSaveIcon()
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleLike(blogId: String) {
        val userId = auth.currentUser?.uid ?: return
        val blogRef = firestore.collection("blogs").document(blogId)
        val likedBlogRef = firestore.collection("users").document(userId).collection("likedBlogs").document(blogId)

        if (isLiked) {
            likedBlogRef.delete().addOnSuccessListener {
                blogRef.update("likeCount", FieldValue.increment(-1))
                isLiked = false
                updateLikeIcon()
            }
        } else {
            likedBlogRef.set(mapOf("liked" to true)).addOnSuccessListener {
                blogRef.update("likeCount", FieldValue.increment(1))
                isLiked = true
                updateLikeIcon()
            }
        }
    }

    private fun updateLikeIcon() {
        binding.fabLike.setImageResource(if (isLiked) R.drawable.ic_like_red else R.drawable.ic_like_border_black)
    }

    private fun updateSaveIcon() {
        binding.fabSave.setImageResource(if (isSaved) R.drawable.ic_save_black else R.drawable.ic_save_border_black)
    }
}
