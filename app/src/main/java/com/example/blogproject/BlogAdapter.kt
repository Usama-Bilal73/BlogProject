package com.example.blogproject

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogproject.databinding.BlogItemBinding

class BlogAdapter(
    private val items: ArrayList<BlogItemModel>,   // changed to ArrayList (recommended)
    private val onBlogClick: (BlogItemModel) -> Unit,
    private val onSaveClick: (BlogItemModel, Int) -> Unit,
    private val onLikeClick: (BlogItemModel, Int) -> Unit
) : RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val binding =
            BlogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blog: BlogItemModel) {
            binding.tvTitle.text = blog.heading
            binding.tvAuthorName.text = blog.userName
            binding.tvDate.text = blog.date
            binding.tvDescription.text = blog.post
            binding.tvLikeCount.text = blog.likeCount.toString()

            // Profile image
            if (!blog.profileImage.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(blog.profileImage, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    Glide.with(binding.ivProfile.context)
                        .load(bitmap)
                        .centerCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.ivProfile)
                } catch (e: Exception) {
                    // Try loading as URL if Base64 fails
                    Glide.with(binding.ivProfile.context)
                        .load(blog.profileImage)
                        .centerCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.ivProfile)
                }
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_profile_placeholder)
            }

            // Like icon
            binding.ivLike.setImageResource(
                if (blog.isLiked) R.drawable.ic_like_red
                else R.drawable.ic_like_border_black
            )

            // Save icon
            binding.btnSave.setImageResource(
                if (blog.isSaved) R.drawable.ic_save_black
                else R.drawable.ic_save_border_black
            )

            // Click listeners
            binding.root.setOnClickListener { onBlogClick(blog) }
            binding.btnReadMore.setOnClickListener { onBlogClick(blog) } // Added listener for Read More button
            binding.btnSave.setOnClickListener { onSaveClick(blog, adapterPosition) }
            binding.ivLike.setOnClickListener { onLikeClick(blog, adapterPosition) }
        }
    }
}
