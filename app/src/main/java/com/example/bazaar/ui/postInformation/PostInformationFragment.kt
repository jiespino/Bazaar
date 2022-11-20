package com.example.bazaar.ui.postInformation

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bazaar.databinding.FragmentPostInformationBinding
import java.io.File
import java.io.IOException
import java.util.*


class PostInformationFragment : Fragment() {

    companion object {
        private val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        fun localPhotoFile(pictureName : String): File {
            // Create the File where the photo should go
            val localPhotoFile = File(storageDir, "${pictureName}.jpg")
            Log.d("MainActivity", "photo path ${localPhotoFile.absolutePath}")
            return localPhotoFile
        }
    }
    private var _binding: FragmentPostInformationBinding? = null
    private val viewModel: PostInformationViewModel by viewModels()
    private lateinit var pictureUUIDs: List<String>
    private lateinit var mediaAdapter: MediaAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var resultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(javaClass.simpleName, "result ok")
            } else {
                Log.w(javaClass.simpleName, "Bad activity return code ${result.resultCode}")
            }
        }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.pictureSuccess()
        } else {
            viewModel.pictureFailure()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPostInformationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel.setPhotoIntent(::takePictureIntent)
        pictureUUIDs = listOf()


        val mediaAttachButton = binding.mediaAttachButton

        val mediaGetIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        mediaAttachButton.setOnClickListener {

            resultLauncher.launch(mediaGetIntent)
        }


        mediaAdapter = MediaAdapter(viewModel) { pictureUUIDPosition ->
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
            val uuid = UUID.randomUUID().toString()
            viewModel.takePhoto(uuid) {
                pictureUUIDs.toMutableList().apply{
                    add(uuid)
                    Log.d(javaClass.simpleName, "photo added $uuid len ${this.size}")
                    pictureUUIDs = this
                    mediaAdapter.submitList(pictureUUIDs)
                }
            }
        }

        val videoCreateButton = binding.videoCreateButton

        val videoCreateIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE )

        videoCreateButton.setOnClickListener {
            val uuid = UUID.randomUUID().toString()

        }

        

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun takePictureIntent(name: String) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePhotoIntent ->
            val localPhotoFile = localPhotoFile(name)
            val photoUri = photoUri(localPhotoFile)
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(takePhotoIntent)
        }
        Log.d(javaClass.simpleName, "takePhotoIntent")
    }

    private fun photoUri(localPhotoFile: File) : Uri {
        var photoUri : Uri? = null
        // Create the File where the photo should go
        try {
            photoUri = FileProvider.getUriForFile(
                context!!,
                "com.example.bazaar",
                localPhotoFile)
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.d(javaClass.simpleName, "Cannot create file", ex)
        }
        // CRASH.  Production code should do something more graceful
        return photoUri!!
    }



}