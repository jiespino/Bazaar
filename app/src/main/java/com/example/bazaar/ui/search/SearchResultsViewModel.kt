package com.example.bazaar.ui.search

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.Model.UserPost
import com.example.bazaar.Storage.DBHelper
import com.example.bazaar.Storage.Storage
import com.example.bazaar.glide.Glide
import com.example.bazaar.ui.createPost.Category
import com.example.bazaar.ui.search.OnePost.OnePostImagePager

class SearchResultsViewModel : ViewModel() {
    private var userPostsList = MutableLiveData<List<UserPost>>()
    private var currentLocation = MutableLiveData<String>()
    private var chosenCategory = MutableLiveData<Category>()
    private var currentUserPost = MutableLiveData<UserPost>()
    // Database access
    private val dbHelp = DBHelper()

    // Notes, memory cache and database interaction
    fun fetchInitialCategoryPosts() {
        dbHelp.fetchInitialCategoryPosts(currentLocation.value!!, chosenCategory.value!!, userPostsList)
    }

    fun setLocation(location: String) {
        currentLocation.value = location
    }

    fun setCategory(category: Category) {
        chosenCategory.value = category
    }

    fun setUserPost(userPost: UserPost) {
        currentUserPost.value = userPost
    }

    fun observeSingleUserPost(): LiveData<UserPost> {
        return currentUserPost
    }

    fun observeUserPosts(): LiveData<List<UserPost>> {
        return userPostsList
    }

    companion object {
        private val storage = Storage()
        fun glideFetch(pictureUUID: String, imageView: ImageView) {
            Glide.fetch(
                storage.uuid2StorageReference(pictureUUID),
                imageView
            )
        }

        fun doOnePostImages(context: Context, userPost: UserPost) {
            val onePostImageIntent = Intent(context, OnePostImagePager::class.java)
            onePostImageIntent.putStringArrayListExtra("pictureUUIDs",  ArrayList(userPost.pictureUUIDs))
            context.startActivity(onePostImageIntent)
        }
    }
}