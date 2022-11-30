package com.example.bazaar.ui.search

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bazaar.Model.UserPost
import com.example.bazaar.Storage.DBHelper
import com.example.bazaar.Storage.Storage
import com.example.bazaar.glide.Glide
import com.example.bazaar.ui.createPost.Category
import com.example.bazaar.ui.search.OnePost.OnePostImagePager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchResultsViewModel : ViewModel() {
    private var userPostsList = MutableLiveData<List<UserPost>>()
    private var currentLocation = MutableLiveData<List<String>>()
    private var currentPriceRange = MutableLiveData<List<Float>>()
    private var currentSearchText = MutableLiveData<String>()
    private var chosenCategory = MutableLiveData<Category>()
    private var currentUserPost = MutableLiveData<UserPost>()
    // Database access
    private val dbHelp = DBHelper()
    var fetchDone : MutableLiveData<Boolean> = MutableLiveData(false)

    // Notes, memory cache and database interaction

    fun fetchPostsForSearch() {
        viewModelScope.launch (viewModelScope.coroutineContext
                + Dispatchers.IO)
        {
            dbHelp.fetchPostsForSearch(currentLocation.value!!, chosenCategory.value!!, userPostsList)
            fetchDone.postValue(true)
        }
    }

    fun setLocation(location: List<String>) {
        currentLocation.value = location
    }

    fun setCategory(category: Category) {
        chosenCategory.value = category
    }

    fun setPriceRange(priceRange: List<Float>) {
        currentPriceRange.value = priceRange
    }

    fun setSearchText(searchText: String) {
        currentSearchText.value = searchText
    }

    fun setUserPost(userPost: UserPost) {
        currentUserPost.value = userPost
    }

    fun getCurrentUserPost(): UserPost {
        return currentUserPost.value!!
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

        fun doOnePostImages(context: Context, singlePictureUUID: String, pictureUUIDs: List<String>) {
            val onePostImageIntent = Intent(context, OnePostImagePager::class.java)
            onePostImageIntent.putStringArrayListExtra("pictureUUIDs",  ArrayList(pictureUUIDs))
            onePostImageIntent.putExtra("singlePictureUUID",  singlePictureUUID)
            context.startActivity(onePostImageIntent)
        }
    }
}