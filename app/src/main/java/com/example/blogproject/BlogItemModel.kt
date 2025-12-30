package com.example.blogproject

import com.google.firebase.firestore.Exclude

data class BlogItemModel(
    @get:Exclude var blogId: String? = null,
    var heading: String? = null,
    var userName: String? = null,
    var date: String? = null,
    var post: String? = null,
    var likeCount: Int = 0,
    var profileImage: String? = null,
    var isExpanded: Boolean = false,
    var userId: String? = null,
    @get:Exclude var isLiked: Boolean = false, // For UI state
    @get:Exclude var isSaved: Boolean = false  // For UI state
) {
    // No-argument constructor required for Firebase
    constructor() : this(null, null, null, null, null, 0, null, false, null, false, false)
}