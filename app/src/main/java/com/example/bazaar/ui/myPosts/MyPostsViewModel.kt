package com.example.bazaar.ui.myPosts

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bazaar.FireBaseAuth.FirestoreAuthLiveData
import com.example.bazaar.Model.AptInfo
import com.example.bazaar.Model.UserPost
import com.example.bazaar.Storage.DBHelper
import com.example.bazaar.Storage.Storage
import com.example.bazaar.ui.createPost.Category
import com.example.bazaar.ui.createPost.CreatePostFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPostsViewModel : ViewModel() {
    private var firebaseAuthLiveData = FirestoreAuthLiveData()
    private var userPostsList = MutableLiveData<List<UserPost>>()
    private var currentUserPost = MutableLiveData<UserPost>()
    private var currentUserUuid = MutableLiveData<String?>()
    // Database access
    private val dbHelp = DBHelper()
    var fetchDone : MutableLiveData<Boolean> = MutableLiveData(false)
    var deletingPhoto : MutableLiveData<Boolean> = MutableLiveData(false)
    var uploadingPhoto : MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        fetchUserPosts()
    }

    fun fetchUserPosts() {

        if (firebaseAuthLiveData.getCurrentUser()?.uid == null) {
            return
        }
        viewModelScope.launch (viewModelScope.coroutineContext
                + Dispatchers.IO)
        {
            dbHelp.fetchInitialUserPosts(firebaseAuthLiveData.getCurrentUser()?.uid!!, userPostsList)
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

    fun setCurrentUser(userUuid: String?) {
        currentUserUuid.value = userUuid
    }

    fun observeCurrentUser(): LiveData<String?> {
        return currentUserUuid
    }

    // Media section
    private val storage = Storage()
    private lateinit var crashMe: String
    private var pictureUUID: String =""

    private fun noMedia() {
        Log.d(javaClass.simpleName, "Function must be initialized to something that can start the media intent")
        crashMe.plus("Media")
    }
    private fun noPhoto(debugName1: String, debugName2: String) {
        Log.d(javaClass.simpleName, "Function must be initialized to something that can start the camera intent")
        crashMe.plus(debugName1)
    }

    private var getExistingMediaIntent: () -> Unit = ::noMedia
    private var takeNewMediaIntent: (String, String) -> Unit = ::noPhoto

    private fun defaultPhoto(@Suppress("UNUSED_PARAMETER") path: String) {
        Log.d(javaClass.simpleName, "Function must be initialized to photo callback" )
        crashMe.plus(" ")
    }
    private var mediaSuccess: (path: String) -> Unit = ::defaultPhoto

    /////////////////////////////////////////////////////////////
    // This is intended to be set once by MainActivity.
    // The bummer is that taking a photo requires startActivityForResult
    // which has to be called from an activity.
    fun setExistingMediaIntent(_getExistingMediaIntent: () -> Unit) {
        getExistingMediaIntent = _getExistingMediaIntent
    }

    /////////////////////////////////////////////////////////////
    // This is intended to be set once by MainActivity.
    // The bummer is that taking a photo requires startActivityForResult
    // which has to be called from an activity.
    fun setNewMediaIntent(_takeMediaIntent: (String, String) -> Unit) {
        takeNewMediaIntent = _takeMediaIntent
    }


    /////////////////////////////////////////////////////////////
    // Get callback for when camera intent returns.
    // Send intent to take picture
    fun getExistingMedia(uuid: String, _mediaSuccess: (String) -> Unit) {
        mediaSuccess = _mediaSuccess
        uploadingPhoto.value = true
        getExistingMediaIntent()
        // Have to remember this in the view model because
        // MainActivity can't remember it without savedInstanceState
        // crap.
        pictureUUID = uuid
    }

    /////////////////////////////////////////////////////////////
    // Get callback for when camera intent returns.
    // Send intent to take picture
    fun getNewMedia(uuid: String, intentType: String, _mediaSuccess: (String) -> Unit) {
        mediaSuccess = _mediaSuccess
        uploadingPhoto.value = true
        takeNewMediaIntent(uuid, intentType)
        // Have to remember this in the view model because
        // MainActivity can't remember it without savedInstanceState
        // crap.
        pictureUUID = uuid
    }

    fun getMediaSuccess(mediaUri: Uri) {

        storage.uploadUserSelectedMedia(mediaUri, pictureUUID) {
            mediaSuccess(pictureUUID)
            mediaSuccess = ::defaultPhoto
            pictureUUID = ""
            uploadingPhoto.value = false
        }
    }

    fun getMediaFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
    }

    fun takeMediaSuccess() {

        val mediaFile = CreatePostFragment.localMediaFile(pictureUUID)
        // Wait until photo is successfully uploaded before calling back
        storage.uploadMedia(mediaFile, pictureUUID) {
            mediaSuccess(pictureUUID)
            mediaSuccess = ::defaultPhoto
            pictureUUID = ""
            uploadingPhoto.value = false
        }
    }
    fun takeMediaFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
    }

    fun updateUserPost(userPost: UserPost) {
        dbHelp.updateUserPost(userPost, userPostsList)
    }

    fun deleteUserPost() {
        dbHelp.deleteUserPost(currentUserPost.value!!, userPostsList)
    }

    fun deleteImage(pictureUUID: String) {
        storage.deleteImage(pictureUUID)

    }
}
