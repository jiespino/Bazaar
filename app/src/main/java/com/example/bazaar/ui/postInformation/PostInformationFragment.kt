package com.example.bazaar.ui.postInformation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentPostInformationBinding
import com.example.bazaar.ui.post.PostViewModel

class PostInformationFragment : Fragment() {

    private var _binding: FragmentPostInformationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        _binding = FragmentPostInformationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}