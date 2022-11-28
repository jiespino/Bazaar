package com.example.bazaar.ui.search.OnePost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bazaar.databinding.FragmentOnePostBinding
import com.example.bazaar.ui.search.SearchResultsViewModel

class OnePostFragment: Fragment() {

    private var _binding: FragmentOnePostBinding? = null
    private val viewModel: SearchResultsViewModel by activityViewModels()
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

        val currUserPost = viewModel.observeSingleUserPost().value
        val pictureUUIDs = currUserPost?.pictureUUIDs

        binding.titleText.text = currUserPost?.title
        binding.descriptionText.text = currUserPost?.description
        binding.priceText.text = currUserPost?.price.toString()

        mediaAdapterOnePost = OnePostMediaAdapter()

        binding.mediaRV.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.mediaRV.adapter = mediaAdapterOnePost
        mediaAdapterOnePost.submitList(pictureUUIDs)

        binding.mediaRV.setOnClickListener {
            SearchResultsViewModel.doOnePostImages(binding.root.context, currUserPost!!)
        }

        binding.titleText.setOnClickListener {
            SearchResultsViewModel.doOnePostImages(binding.root.context, currUserPost!!)
        }

        val root: View = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}