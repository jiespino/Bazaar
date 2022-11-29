package com.example.bazaar.ui.createPost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bazaar.databinding.FragmentPostInformationBinding
import java.io.File
import java.io.IOException
import java.util.*
import androidx.navigation.fragment.findNavController
import com.example.bazaar.R


class PostInformationFragment : Fragment() {

    companion object {
        private val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        fun localMediaFile(mediaName : String): File {
            // Create the File where the photo should go
            val localPhotoFile = File(storageDir, "${mediaName}.jpg")
            Log.d("MainActivity", "photo path ${localPhotoFile.absolutePath}")
            return localPhotoFile
        }

    }
    private var _binding: FragmentPostInformationBinding? = null
    private val viewModel: PostInformationViewModel by activityViewModels()
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

                viewModel.getMediaSuccess(uri)
                Log.d(javaClass.simpleName, "result ok")
            } else {
                viewModel.getMediaFailure()
                Log.w(javaClass.simpleName, "Bad activity return code ${result.resultCode}")
            }
        }

    private val newMediaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.takeMediaSuccess()
        } else {
            viewModel.takeMediaFailure()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPostInformationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.setNewMediaIntent(::getNewMediaIntent)
        viewModel.setExistingMediaIntent(::getExistingMediaIntent)
        pictureUUIDs = listOf()

        if (viewModel.observeCategory().value != Category.APARTMENT) {
            hideAptLayout()
        }

        val mediaAttachButton = binding.mediaAttachButton

        mediaAttachButton.setOnClickListener {
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


        mediaAdapter = MediaAdapter { pictureUUIDPosition ->
            Log.d(javaClass.simpleName, "pictureUUIDs del $pictureUUIDPosition")
            val shorterList = pictureUUIDs.toMutableList()
            shorterList.removeAt(pictureUUIDPosition)
            pictureUUIDs = shorterList
            mediaAdapter.submitList(pictureUUIDs)
        }

        binding.mediaRV.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.mediaRV.adapter = mediaAdapter
        mediaAdapter.submitList(pictureUUIDs)

        val photoCreateButton = binding.photoCreateButton
        photoCreateButton.setOnClickListener {
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
        sqFtHelpText.visibility = View.GONE

        val sqFtEditText = binding.sqFeetEditText
        sqFtEditText.visibility = View.GONE

        val priceHelpText = binding.priceHelpText
        priceHelpText.text = getString(R.string.price_help_text)

        val roomHelpText = binding.roomsHelpText
        roomHelpText.visibility = View.GONE

        val roomEditText = binding.roomsEditText
        roomEditText.visibility = View.GONE

        val bathHelpText = binding.bathsHelpText
        bathHelpText.visibility = View.GONE

        val bathEditText = binding.bathsEditText
        bathEditText.visibility = View.GONE
    }


    private fun savePostInfo() {
        val titleText = binding.titleEditText.text.toString()
        val descriptionText = binding.descriptionEditText.text.toString()
        val priceText = binding.priceEditText.text.toString()

        if (titleText.isEmpty()) {
            Toast.makeText(activity,
                "Enter a title!",
                Toast.LENGTH_LONG).show()
        }
        else if(descriptionText.isEmpty()) {
            Toast.makeText(activity,
                "Enter a description!",
                Toast.LENGTH_LONG).show()
        }

        else if(priceText.isEmpty()) {
            Toast.makeText(activity,
                "Enter a price!",
                Toast.LENGTH_LONG).show()
        }
        else {
            Log.d(javaClass.simpleName, "create post len ${pictureUUIDs.size} pos")
            val postInfo = listOf(titleText, descriptionText)
            viewModel.createUserPost(postInfo, pictureUUIDs)

            // When using navigation, don't manipulate the fragmentManger backstack
            // directly!  NO parentFragmentManager.popBackStack()
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

}