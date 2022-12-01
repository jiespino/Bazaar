package com.example.bazaar.ui.createPost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bazaar.Model.AptInfo
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentCreatePostBinding
import com.example.bazaar.ui.search.SearchResultsViewModel
import java.io.File
import java.io.IOException
import java.util.*


class CreatePostFragment : Fragment() {

    companion object {
        private val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        fun localMediaFile(mediaName : String): File {
            // Create the File where the photo should go
            val localPhotoFile = File(storageDir, "${mediaName}.jpg")
            Log.d("MainActivity", "photo path ${localPhotoFile.absolutePath}")
            return localPhotoFile
        }

    }
    private var _binding: FragmentCreatePostBinding? = null
    private val viewModel: CreatePostViewModel by activityViewModels()
    private lateinit var pictureUUIDs: List<String>
    private lateinit var mediaAdapter: MediaAdapter
    private var postSaved: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var existingMediaLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data!!

                viewModel.getExistMediaSuccess(uri)
                Log.d(javaClass.simpleName, "result ok")
            } else {
                viewModel.getMediaFailure()
                Log.w(javaClass.simpleName, "Bad activity return code ${result.resultCode}")
            }
        }

    private val newMediaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.takeNewMediaSuccess()
        } else {
            viewModel.takeMediaFailure()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.setNewMediaIntent(::getNewMediaIntent)
        viewModel.setExistingMediaIntent(::getExistingMediaIntent)
        pictureUUIDs = listOf()

        if (viewModel.getCategory() != Category.APARTMENT) {
            hideAptLayout()
        }

        val phoneNumEditText = binding.phoneNumberEditText
        phoneNumEditText.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        val currLocation = viewModel.getLocation()
        val city = currLocation[0]
        val state = currLocation[1]
        val countryCode = currLocation[2]
        binding.locationText.text = "$city $state, $countryCode"
        binding.categoryText.text = viewModel.getCategory().toString().lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        val mediaAttachButton = binding.mediaAttachButton

        mediaAttachButton.setOnClickListener {

            if (viewModel.uploadingPhoto.value!!) {
                Toast.makeText(activity,
                    "Still uploading previous photo!",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val uuid = UUID.randomUUID().toString()
            viewModel.getExistingMedia(uuid) {
                pictureUUIDs.toMutableList().apply{
                    add(uuid)
                    Log.d(javaClass.simpleName, "photo added $uuid len ${this.size}")
                    pictureUUIDs = this
                    mediaAdapter.submitList(pictureUUIDs)
                }
            }
        }

        mediaAdapter = MediaAdapter(::deletePos, ::launchImagePager)

        binding.mediaRV.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.mediaRV.adapter = mediaAdapter
        mediaAdapter.submitList(pictureUUIDs)

        val photoCreateButton = binding.photoCreateButton
        photoCreateButton.setOnClickListener {

            if (viewModel.uploadingPhoto.value!!) {
                Toast.makeText(activity,
                    "Still uploading previous photo!",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            getNewMedia()
        }


        val cancelButton = binding.cancelButton
        cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val saveButton = binding.saveButton
        saveButton.setOnClickListener {
            savePostInfo()
            postSaved = true
        }

        return root
    }

    override fun onDestroyView() {

        if (!postSaved) {
            viewModel.deleteImages(pictureUUIDs)
        }

        super.onDestroyView()
        _binding = null
    }

    // Hide fields specific to only apartments
    private fun hideAptLayout() {
        val sqFtHelpText = binding.sqFeetHelpText
        sqFtHelpText.visibility = View.INVISIBLE

        val sqFtEditText = binding.sqFeetEditText
        sqFtEditText.visibility = View.INVISIBLE

        val priceHelpText = binding.priceHelpText
        priceHelpText.text = getString(R.string.price_help_text)

        val roomHelpText = binding.roomsHelpText
        roomHelpText.visibility = View.INVISIBLE

        val roomEditText = binding.roomsEditText
        roomEditText.visibility = View.INVISIBLE

        val bathHelpText = binding.bathsHelpText
        bathHelpText.visibility = View.INVISIBLE

        val bathEditText = binding.bathsEditText
        bathEditText.visibility = View.INVISIBLE

    }


    private fun savePostInfo() {
        val titleText = binding.titleEditText.text.toString()
        val descriptionText = binding.descriptionEditText.text.toString()
        val phoneNumberText = binding.phoneNumberEditText.text.toString()
        val priceText = binding.priceEditText.text.toString()
        val squareFeet = binding.sqFeetEditText.text.toString()
        val rooms = binding.roomsEditText.text.toString()
        val baths = binding.bathsEditText.text.toString()

        val validationInfo = validateInput()
        val isBadInput = validationInfo[0] as Boolean
        val errMsg = validationInfo[1] as String



       if (isBadInput) {
            Toast.makeText(activity,
                errMsg,
                Toast.LENGTH_LONG).show()
        }
        else {
            Log.d(javaClass.simpleName, "create post len ${pictureUUIDs.size} pos")

            val aptInfo: AptInfo?
            if (viewModel.getCategory() == Category.APARTMENT) {
                aptInfo = AptInfo(
                    squareFeet = squareFeet.toInt(),
                    rooms = rooms.toInt(),
                    baths = baths.toInt()
                )
            } else {
                aptInfo = null
            }

            val postInfo = listOf(titleText, descriptionText, priceText, phoneNumberText)
            viewModel.createUserPost(postInfo, aptInfo, pictureUUIDs)
            findNavController().popBackStack()
        }
    }

    private fun getNewMedia() {
        val uuid = UUID.randomUUID().toString()
        viewModel.getNewMedia(uuid, MediaStore.ACTION_IMAGE_CAPTURE) {
            pictureUUIDs.toMutableList().apply{
                add(uuid)
                Log.d(javaClass.simpleName, "photo added $uuid len ${this.size}")
                pictureUUIDs = this
                mediaAdapter.submitList(pictureUUIDs)
            }
        }
    }

    private fun getExistingMediaIntent() {
        Intent(Intent.ACTION_GET_CONTENT).also { getMediaIntent ->
            getMediaIntent.type = "image/*"
            getMediaIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            existingMediaLauncher.launch(getMediaIntent)
        }

        Log.d(javaClass.simpleName, "getExistMediaIntent")
    }

    private fun getNewMediaIntent(uniqueId: String, intentType: String) {
        Intent(intentType).also { takePhotoIntent ->
            val localMediaFile = localMediaFile(uniqueId)
            val mediaUri = mediaUri(localMediaFile)
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri)
            newMediaLauncher.launch(takePhotoIntent)
        }
        Log.d(javaClass.simpleName, "getNewMediaIntent")
    }

    private fun mediaUri(localMediaFile: File) : Uri {
        var mediaUri : Uri? = null
        // Create the File where the photo should go
        try {
            mediaUri = FileProvider.getUriForFile(
                context!!,
                "com.example.bazaar",
                localMediaFile)
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.d(javaClass.simpleName, "Cannot create file", ex)
        }
        // CRASH.  Production code should do something more graceful
        return mediaUri!!
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        return if (phone.trim { it <= ' ' } != "" && phone.length > 10) {
            Patterns.PHONE.matcher(phone).matches()
        } else false
    }

    private fun validateInput(): List<Any> {

        val titleText = binding.titleEditText.text.toString()
        val descriptionText = binding.descriptionEditText.text.toString()
        val phoneNumberText = binding.phoneNumberEditText.text.toString()
        val priceText = binding.priceEditText.text.toString()
        val squareFeet = binding.sqFeetEditText.text.toString()
        val rooms = binding.roomsEditText.text.toString()
        val baths = binding.bathsEditText.text.toString()

        var errorMessage = ""


        if (titleText.isEmpty()) {
            errorMessage = "Enter a title!"
            return listOf(true, errorMessage)
        }
        else if(descriptionText.isEmpty()) {
            errorMessage = "Enter a description!"
            return listOf(true, errorMessage)
        }

        else if(priceText.isEmpty()) {
            errorMessage = "Enter a price!"
            return listOf(true, errorMessage)
        }

        else if(phoneNumberText.isNotEmpty() and !isValidPhoneNumber(phoneNumberText)) {
            errorMessage = "Enter a valid phone number!"
            return listOf(true, errorMessage)

        }

        if (viewModel.getCategory() != Category.APARTMENT) {
            return listOf(false, errorMessage)
        }


        if(squareFeet.isEmpty()) {
            errorMessage = "Enter the square feet!"
            return listOf(true, errorMessage)

        } else if(rooms.isEmpty()) {
            errorMessage = "Enter the number of rooms!"
            return listOf(true, errorMessage)

        } else if(baths.isEmpty()) {
            errorMessage = "Enter the number of baths!"
            return listOf(true, errorMessage)
        }
        return listOf(false, errorMessage)
    }

    private fun deletePos(pictureUUIDPosition: Int) {
        Log.d(javaClass.simpleName, "pictureUUIDs del $pictureUUIDPosition")
        val shorterList = pictureUUIDs.toMutableList()
        val currentPictureUUID = shorterList[pictureUUIDPosition]
        viewModel.deleteImage(currentPictureUUID)
        shorterList.removeAt(pictureUUIDPosition)
        pictureUUIDs = shorterList
        mediaAdapter.submitList(pictureUUIDs)
    }

    private fun launchImagePager(pictureUUID: String) {
        SearchResultsViewModel.doOnePostImages(context!!, pictureUUID, pictureUUIDs)
    }

}