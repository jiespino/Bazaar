package com.example.bazaar.ui.search

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.Model.UserPost
import com.example.bazaar.Storage.DBHelper
import com.example.bazaar.Storage.Storage
import com.example.bazaar.glide.Glide
import com.example.bazaar.ui.postInformation.Category
import com.example.bazaar.ui.postInformation.PostInformationFragment
import com.firebase.ui.auth.data.model.User
import org.checkerframework.checker.units.qual.C

class SearchResultsViewModel : ViewModel() {
    private val storage = Storage()
    private var userPostsList = MutableLiveData<List<UserPost>>()
    private var usLocation = MutableLiveData<String>()
    private var chosenCategory = MutableLiveData<Category>()
    // Database access
    private val dbHelp = DBHelper()

    // Notes, memory cache and database interaction
    fun fetchInitialUserPosts() {
        dbHelp.fetchInitialUserPosts(usLocation.value!!, chosenCategory.value!!, userPostsList)
    }

    fun setLocation(location: String) {
        usLocation.value = location
    }

    fun setCategory(currentCategory: Category) {
        chosenCategory.value = currentCategory
    }

    fun observeUserPosts(): LiveData<List<UserPost>> {
        return userPostsList
    }

    fun glideFetch(pictureUUID: String, imageView: ImageView) {
        Glide.fetch(storage.uuid2StorageReference(pictureUUID),
            imageView)
    }
}