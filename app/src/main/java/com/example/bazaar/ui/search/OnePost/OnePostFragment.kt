package com.example.bazaar.ui.search.OnePost

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bazaar.Model.UserPost
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentOnePostBinding
import com.example.bazaar.ui.createPost.Category
import com.example.bazaar.ui.search.SearchResultsViewModel
import java.util.*

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

        val currUserPost = viewModel.getCurrentUserPost()
        val pictureUUIDs = currUserPost.pictureUUIDs

        val city = currUserPost.city
        val state = currUserPost.state
        val countryCode = currUserPost.countryCode

        binding.locationText.text = "$city $state, $countryCode"
        binding.categoryText.text = currUserPost.category.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        binding.titleText.text = currUserPost.title
        binding.descriptionText.text = currUserPost.description
        binding.emailText.text = currUserPost.userEmail
        binding.phoneText.text = currUserPost.phoneNumber
        binding.priceText.text = currUserPost.price.toString()

        val phoneNumber = currUserPost.phoneNumber

        if (phoneNumber.isEmpty()) {
            binding.phoneHelpText.visibility = View.GONE
            binding.phoneText.visibility = View.GONE
        } else {
            binding.phoneText.text = currUserPost.phoneNumber
        }

        binding.priceText.text = currUserPost.price.toString()


        if (currUserPost.category == Category.APARTMENT.toString()) {
            binding.sqFeetText.text = currUserPost.aptInfo?.squareFeet.toString()
            binding.roomText.text = currUserPost.aptInfo?.rooms.toString()
            binding.bathText.text = currUserPost.aptInfo?.baths.toString()
            binding.priceHelpText.text = getString(R.string.price_apt_help_text)
        } else {
            binding.sqFeetHelpText.visibility = View.GONE
            binding.sqFeetText.visibility = View.GONE
            binding.roomHelpText.visibility = View.GONE
            binding.roomText.visibility = View.GONE
            binding.bathHelpText.visibility = View.GONE
            binding.bathText.visibility = View.GONE
        }

        binding.editButton.visibility = View.GONE
        binding.deleteButton.visibility = View.GONE

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

    private fun composeMmsMessage(userPost: UserPost) {
        val phoneNumber = userPost.phoneNumber
        val userName = userPost.userName
        val uri = Uri.parse("smsto:$phoneNumber")
        val smsIntent = Intent(Intent.ACTION_SENDTO, uri)
        smsIntent.putExtra("sms_body", "Hey $userName")
        startActivity(smsIntent)

        if (smsIntent.resolveActivity(activity!!.packageManager) != null) {
            startActivity(smsIntent)
        }
    }
}