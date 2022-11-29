package com.example.bazaar.ui.createPost

import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.FireBaseAuth.FirestoreAuthLiveData
import com.example.bazaar.Model.UserPost
import com.example.bazaar.Storage.DBHelper
import com.example.bazaar.Storage.Storage


class PostInformationViewModel : ViewModel() {

    private val storage = Storage()
    private var pictureUUID: String =""
    private var usLocation = MutableLiveData<String>()
    private var chosenCategory = MutableLiveData<Category>()

    private var firebaseAuthLiveData = FirestoreAuthLiveData()
    // Database access
    private val dbHelp = DBHelper()

    private lateinit var crashMe: String

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

    fun setLocation(location: String) {
        usLocation.value = location
    }

    fun setCategory(currentCategory: Category) {
        chosenCategory.value = currentCategory
    }

    fun observeCategory():LiveData<Category> {
        return chosenCategory
    }


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
        }
    }

    fun getMediaFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
    }

    fun takeMediaSuccess() {

        val mediaFile = PostInformationFragment.localMediaFile(pictureUUID)
        // Wait until photo is successfully uploaded before calling back
        storage.uploadMedia(mediaFile, pictureUUID) {
            mediaSuccess(pictureUUID)
            mediaSuccess = ::defaultPhoto
            pictureUUID = ""
        }
    }
    fun takeMediaFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
    }

    fun createUserPost(postInfo: List<String>, pictureUUIDs: List<String>) {

        val title = postInfo[0]
        val description = postInfo[1]

        val currentUser = firebaseAuthLiveData.getCurrentUser()!!
        val userPost = UserPost(
            userName = currentUser.displayName ?: "Anonymous user",
            ownerUid = currentUser.uid,
            title = title,
            description = description,
            pictureUUIDs = pictureUUIDs
            // database sets firestoreID
        )
        dbHelp.createUserPost(userPost, usLocation.value!!, chosenCategory.value!!)
    }

    fun deleteImages(savedPictureUUIDs: List<String>) {
        savedPictureUUIDs.forEach {
            storage.deleteImage(it)
        }
    }
}