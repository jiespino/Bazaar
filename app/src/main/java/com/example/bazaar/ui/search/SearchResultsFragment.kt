package com.example.bazaar.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bazaar.R
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

        viewModel.fetchPostsForSearch()

        val searchResultsAdapter = SearchResultsAdapter(viewModel, parentFragmentManager)
        binding.searchResultsRV.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRV.adapter = searchResultsAdapter
        viewModel.observeUserPosts().observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.noResults.visibility = View.VISIBLE
                binding.noResults.text = getString(R.string.no_results_text)
            } else {
                binding.noResults.visibility = View.GONE
            }
            Log.d(javaClass.simpleName, "postList observe len ${it.size}")
            searchResultsAdapter.submitList(it)
        }
        initSwipeLayout(binding.swipeRefreshLayout)

        binding.logOutButton.visibility = View.GONE

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
            viewModel.fetchPostsForSearch()
        }
    }
}