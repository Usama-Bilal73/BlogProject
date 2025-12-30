package com.example.blogproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogproject.databinding.ActivitySavedBlogsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class SavedBlogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedBlogsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var savedBlogsAdapter: BlogAdapter

    private val savedBlogItems = ArrayList<BlogItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedBlogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // RecyclerView setup
        binding.rvSavedBlogs.layoutManager = LinearLayoutManager(this)

        // --- ADAPTER INITIALIZATION ---
        savedBlogsAdapter = BlogAdapter(
            items = savedBlogItems,
            onBlogClick = { blogItem ->
                val intent = Intent(this, BlogDetailsActivity::class.java).apply {
                    putExtra("BLOG_ID", blogItem.blogId)
                    putExtra("BLOG_TITLE", blogItem.heading)
                    putExtra("BLOG_DESC", blogItem.post)
                    putExtra("BLOG_DATE", blogItem.date)
                    putExtra("BLOG_IMAGE_URL", blogItem.profileImage)
                }
                startActivity(intent)
            },
            onSaveClick = { blogItem, position ->
                // Save logic (optional for this screen)
            },
            onLikeClick = { blogItem, position ->
                // Like logic (optional for this screen)
            }
        )
        binding.rvSavedBlogs.adapter = savedBlogsAdapter

        fetchSavedBlogs()
    }

    private fun fetchSavedBlogs() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users")
            .document(userId)
            .collection("savedBlogs")
            .get()
            .addOnSuccessListener { documents ->
                savedBlogItems.clear()

                if (documents.isEmpty) {
                    Toast.makeText(this, "No saved blogs yet!", Toast.LENGTH_SHORT).show()
                    savedBlogsAdapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                // Counter to track progress
                val totalDocs = documents.size()
                var loadedCount = 0

                for (document in documents) {
                    val blogId = document.id

                    firestore.collection("blogs")
                        .document(blogId)
                        .get()
                        .addOnSuccessListener { blogDoc ->
                            if (blogDoc.exists()) {
                                blogDoc.toObject<BlogItemModel>()?.let { item ->
                                    item.blogId = blogDoc.id
                                    savedBlogItems.add(item)
                                }
                            }

                            // Increment counter
                            loadedCount++

                            // Only notify adapter once ALL items are processed
                            if (loadedCount == totalDocs) {
                                savedBlogsAdapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                            // Even if one fails, we must increment so the others can eventually load
                            loadedCount++
                            if (loadedCount == totalDocs) {
                                savedBlogsAdapter.notifyDataSetChanged()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load saved blogs", Toast.LENGTH_SHORT).show()
            }
    }
}
