package com.example.bazaar.ui.postInformation

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bazaar.Storage.Storage
import com.example.bazaar.glide.Glide

class PostInformationViewModel : ViewModel() {

    private val storage = Storage()

    private var pictureUUID: String =""

    private lateinit var crashMe: String
    private fun noPhoto(name: String) {
        Log.d(javaClass.simpleName, "Function must be initialized to something that can start the camera intent")
        crashMe.plus(name)
    }
    private var takePhotoIntent: (String) -> Unit = ::noPhoto

    private fun defaultPhoto(@Suppress("UNUSED_PARAMETER") path: String) {
        Log.d(javaClass.simpleName, "Function must be initialized to photo callback" )
        crashMe.plus(" ")
    }
    private var photoSuccess: (path: String) -> Unit = ::defaultPhoto


    /////////////////////////////////////////////////////////////
    // This is intended to be set once by MainActivity.
    // The bummer is that taking a photo requires startActivityForResult
    // which has to be called from an activity.
    fun setPhotoIntent(_takePhotoIntent: (String) -> Unit) {
        takePhotoIntent = _takePhotoIntent
    }

    /////////////////////////////////////////////////////////////
    // Get callback for when camera intent returns.
    // Send intent to take picture
    fun takePhoto(uuid: String, _photoSuccess: (String) -> Unit) {
        photoSuccess = _photoSuccess
        takePhotoIntent(uuid)
        // Have to remember this in the view model because
        // MainActivity can't remember it without savedInstanceState
        // crap.
        pictureUUID = uuid
    }

    fun pictureSuccess() {
        val photoFile = PostInformationFragment.localPhotoFile(pictureUUID)
        // Wait until photo is successfully uploaded before calling back
        storage.uploadImage(photoFile, pictureUUID) {
            photoSuccess(pictureUUID)
            photoSuccess = ::defaultPhoto
            pictureUUID = ""
        }
    }
    fun pictureFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
    }

    fun glideFetch(pictureUUID: String, imageView: ImageView) {
        Glide.fetch(storage.uuid2StorageReference(pictureUUID),
            imageView)
    }
}