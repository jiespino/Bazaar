package com.example.bazaar.ui.myPosts

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentSearchResultsBinding
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bazaar.FireBaseAuth.AuthInit
import com.example.bazaar.FireBaseAuth.FirestoreAuthLiveData
import com.google.firebase.auth.FirebaseAuth

class MyPostsFragment: Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val viewModel: MyPostsViewModel by activityViewModels()
    private val firebaseAuthLiveData = FirestoreAuthLiveData()

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
            if (it.isEmpty()) {
                binding.noResults.visibility = View.VISIBLE
                binding.noResults.text = getString(R.string.no_results_text)
            } else {
                binding.noResults.visibility = View.GONE
            }
            Log.d(javaClass.simpleName, "noteList observe len ${it.size}")
            searchResultsAdapter.submitList(it)
        }

        binding.logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            AuthInit(firebaseAuthLiveData, signInLauncher)
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

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            firebaseAuthLiveData.updateUser()
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.d("MainActivity", "sign in failed ${result}")
        }
    }
}