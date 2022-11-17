package com.example.bazaar.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentPostBinding
import com.example.bazaar.ui.dashboard.DashboardFragment
import com.example.bazaar.ui.postInformation.PostInformationFragment

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val postViewModel =
            ViewModelProvider(this).get(PostViewModel::class.java)

        _binding = FragmentPostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val radioGroup = binding.categories

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->

        }

        val continueButton = binding.continueButton

        continueButton.setOnClickListener{
            parentFragmentManager.commit {
                replace(R.id.nav_host_fragment_activity_main, PostInformationFragment())
                setReorderingAllowed(true)
                addToBackStack("postInfo")
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}