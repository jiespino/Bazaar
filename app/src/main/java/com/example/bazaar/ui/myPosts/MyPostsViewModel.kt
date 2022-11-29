package com.example.bazaar.ui.myPosts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bazaar.FireBaseAuth.FirestoreAuthLiveData
import com.example.bazaar.Model.UserPost
import com.example.bazaar.Storage.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPostsViewModel : ViewModel() {
    private var firebaseAuthLiveData = FirestoreAuthLiveData()
    private var userPostsList = MutableLiveData<List<UserPost>>()
    private var currentUserPost = MutableLiveData<UserPost>()
    private var userUuid = firebaseAuthLiveData.getCurrentUser()!!.uid
    // Database access
    private val dbHelp = DBHelper()
    var fetchDone : MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        fetchUserPosts()
    }

    fun fetchUserPosts() {
        viewModelScope.launch (viewModelScope.coroutineContext
                + Dispatchers.IO)
        {
            dbHelp.fetchInitialUserPosts(userUuid, userPostsList)
            fetchDone.postValue(true)
        }
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
}