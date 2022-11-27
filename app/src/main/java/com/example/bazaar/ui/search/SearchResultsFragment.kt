package com.example.bazaar.ui.search

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaar.databinding.FragmentSearchResultsBinding


class SearchResultsFragment: Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val viewModel: SearchResultsViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)

        viewModel.fetchInitialUserPosts()

        val searchResultsAdapter = SearchResultsAdapter(viewModel, parentFragmentManager)
        binding.searchResultsRV.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRV.adapter = searchResultsAdapter
        viewModel.observeUserPosts().observe(viewLifecycleOwner) {
            Log.d(javaClass.simpleName, "noteList observe len ${it.size}")
            //toggleEmptyNotes()
            searchResultsAdapter.submitList(it)
        }

        val root: View = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}