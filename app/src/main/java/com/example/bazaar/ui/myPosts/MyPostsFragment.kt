package com.example.bazaar.ui.myPosts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentSearchResultsBinding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MyPostsFragment: Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val viewModel: MyPostsViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)

        val searchResultsAdapter = MyPostsAdapter(viewModel, parentFragmentManager)
        binding.searchResultsRV.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRV.adapter = searchResultsAdapter
        viewModel.observeUserPosts().observe(viewLifecycleOwner) {
            Log.d(javaClass.simpleName, "noteList observe len ${it.size}")
            //toggleEmptyNotes()
            searchResultsAdapter.submitList(it)
        }

        initSwipeLayout(binding.swipeRefreshLayout)

        val root: View = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initSwipeLayout(swipe : SwipeRefreshLayout) {

        viewModel.fetchDone.observe(viewLifecycleOwner) {
            swipe.isRefreshing = false
        }
        swipe.setOnRefreshListener {
            viewModel.fetchUserPosts()
        }
    }
}