package com.example.blogproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blogproject.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val blogItems = ArrayList<BlogItemModel>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: BlogAdapter
    private var blogsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadUserProfile()
        setupRecyclerView()
        // Note: fetchBlogs removed, using real-time listener in onResume

        // Navigate to Add Article Activity
        binding.floatingAddArticalButton.setOnClickListener {
            startActivity(Intent(this, AddArticalActivity::class.java))
        }

        // Navigate to Saved Blogs Activity
        binding.btnSave.setOnClickListener {
            startActivity(Intent(this, SavedBlogsActivity::class.java))
        }

        // Navigate to Profile Activity
        binding.cvProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        startListeningForBlogs()
        loadUserProfile()
    }

    override fun onPause() {
        super.onPause()
        blogsListener?.remove()
        blogsListener = null
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profileImage = document.getString("profileImage")
                        if (!profileImage.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(profileImage, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Glide.with(this)
                                    .load(bitmap)
                                    .centerCrop()
                                    .placeholder(R.drawable.profilemain)
                                    .into(binding.imageView)
                            } catch (e: Exception) {
                                Glide.with(this)
                                    .load(profileImage)
                                    .centerCrop()
                                    .placeholder(R.drawable.profilemain)
                                    .into(binding.imageView)
                            }
                        }
                    }
                }
        }
    }

    private fun setupRecyclerView() {
        binding.rvFeeds.layoutManager = LinearLayoutManager(this)
        adapter = BlogAdapter(
            items = blogItems,
            onBlogClick = { blogItem ->
                val intent = Intent(this, BlogDetailsActivity::class.java)
                intent.putExtra("BLOG_ID", blogItem.blogId)
                startActivity(intent)
            },
            onSaveClick = { blog, position ->
                toggleSave(blog, position)
            },
            onLikeClick = { blog, position ->
                toggleLike(blog, position)
            }
        )
        binding.rvFeeds.adapter = adapter
    }

    private fun startListeningForBlogs() {
        if (blogsListener != null) return

        blogsListener = firestore.collection("blogs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error fetching blogs: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val tempBlogs = ArrayList<BlogItemModel>()
                    for (document in snapshots) {
                        val blog = document.toObject(BlogItemModel::class.java)
                        blog.blogId = document.id
                        tempBlogs.add(blog)
                    }

                    // Update UI immediately (cache or server)
                    updateAdapter(tempBlogs)

                    // Then update icons based on user interactions
                    checkUserInteractions(tempBlogs)
                }
            }
    }

    private fun checkUserInteractions(tempBlogs: ArrayList<BlogItemModel>) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            updateAdapter(tempBlogs)
            return
        }

        val userId = currentUser.uid
        val userRef = firestore.collection("users").document(userId)

        userRef.collection("likedBlogs").get()
            .addOnSuccessListener { likedSnapshot ->
                val likedIds = likedSnapshot.documents.map { it.id }.toSet()

                userRef.collection("savedBlogs").get()
                    .addOnSuccessListener { savedSnapshot ->
                        val savedIds = savedSnapshot.documents.map { it.id }.toSet()

                        for (blog in tempBlogs) {
                            val id = blog.blogId
                            if (id != null) {
                                blog.isLiked = likedIds.contains(id)
                                blog.isSaved = savedIds.contains(id)
                            }
                        }
                        updateAdapter(tempBlogs)
                    }
                    .addOnFailureListener {
                        updateAdapter(tempBlogs)
                    }
            }
            .addOnFailureListener {
                updateAdapter(tempBlogs)
            }
    }

    private fun updateAdapter(newBlogs: ArrayList<BlogItemModel>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = blogItems.size

            override fun getNewListSize(): Int = newBlogs.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return blogItems[oldItemPosition].blogId == newBlogs[newItemPosition].blogId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = blogItems[oldItemPosition]
                val newItem = newBlogs[newItemPosition]
                return oldItem.heading == newItem.heading &&
                       oldItem.post == newItem.post &&
                       oldItem.isLiked == newItem.isLiked &&
                       oldItem.isSaved == newItem.isSaved &&
                       oldItem.likeCount == newItem.likeCount
            }
        })
        blogItems.clear()
        blogItems.addAll(newBlogs.map { it.copy() })
        diffResult.dispatchUpdatesTo(adapter)
    }

    private fun toggleSave(blog: BlogItemModel, position: Int) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to save blogs", Toast.LENGTH_SHORT).show()
            return
        }

        val blogId = blog.blogId
        if (blogId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Blog ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val savedRef = firestore.collection("users").document(currentUser.uid)
            .collection("savedBlogs").document(blogId)

        if (blog.isSaved) {
            blog.isSaved = false
            adapter.notifyItemChanged(position)
            
            savedRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Blog removed from saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    blog.isSaved = true
                    adapter.notifyItemChanged(position)
                }
        } else {
            blog.isSaved = true
            adapter.notifyItemChanged(position)
            
            val saveData = hashMapOf(
                "timestamp" to FieldValue.serverTimestamp(),
                "saved" to true
            )
            savedRef.set(saveData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Blog saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    blog.isSaved = false
                    adapter.notifyItemChanged(position)
                }
        }
    }

    private fun toggleLike(blog: BlogItemModel, position: Int) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to like", Toast.LENGTH_SHORT).show()
            return
        }

        val blogId = blog.blogId
        if (blogId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Blog ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val blogRef = firestore.collection("blogs").document(blogId)
        val likedRef = firestore.collection("users").document(currentUser.uid)
            .collection("likedBlogs").document(blogId)

        if (blog.isLiked) {
            blog.isLiked = false
            if (blog.likeCount > 0) blog.likeCount -= 1
            adapter.notifyItemChanged(position)

            likedRef.delete().addOnSuccessListener {
                blogRef.update("likeCount", FieldValue.increment(-1))
            }.addOnFailureListener {
                blog.isLiked = true
                blog.likeCount += 1
                adapter.notifyItemChanged(position)
            }
        } else {
            blog.isLiked = true
            blog.likeCount += 1
            adapter.notifyItemChanged(position)

            val likeData = hashMapOf(
                "timestamp" to FieldValue.serverTimestamp(),
                "liked" to true
            )
            likedRef.set(likeData).addOnSuccessListener {
                blogRef.update("likeCount", FieldValue.increment(1))
            }.addOnFailureListener {
                blog.isLiked = false
                if (blog.likeCount > 0) blog.likeCount -= 1
                adapter.notifyItemChanged(position)
            }
        }
    }
}
