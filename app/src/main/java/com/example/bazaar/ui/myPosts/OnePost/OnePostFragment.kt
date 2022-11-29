package com.example.bazaar.ui.myPosts.OnePost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bazaar.databinding.FragmentOnePostBinding
import com.example.bazaar.ui.myPosts.MyPostsViewModel

class OnePostFragment: Fragment() {

    private var _binding: FragmentOnePostBinding? = null
    private val viewModel: MyPostsViewModel by activityViewModels()
    private lateinit var mediaAdapterOnePost: OnePostMediaAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnePostBinding.inflate(inflater, container, false)

        val currUserPost = viewModel.getCurrentUserPost()
        val pictureUUIDs = currUserPost.pictureUUIDs

        binding.titleText.text = currUserPost.title
        binding.descriptionText.text = currUserPost.description
        binding.priceText.text = currUserPost.price.toString()

        mediaAdapterOnePost = OnePostMediaAdapter(viewModel)

        binding.mediaRV.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.mediaRV.adapter = mediaAdapterOnePost
        mediaAdapterOnePost.submitList(pictureUUIDs)

        val root: View = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}