package com.example.bazaar.ui.createPost

import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bazaar.R
import android.location.Geocoder
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bazaar.databinding.FragmentCriteriaCreatePostBinding
import java.util.*

class PostCriteriaFragment : Fragment() {

    private var _binding: FragmentCriteriaCreatePostBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var geocoder: Geocoder
    private val viewModel: CreatePostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCriteriaCreatePostBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val radioGroup = binding.categories

        geocoder = Geocoder(this.context, Locale.getDefault())

        val continueButton = binding.continueButton

        continueButton.setOnClickListener{

            val address = binding.locationEditText.text.toString()

            if (address.isEmpty()) {
                Toast.makeText(activity,
                    "Enter an address!",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val geoAddress = geocoder.getFromLocationName(address, 1)

            if (geoAddress.size > 0) {
                val location: Address = geoAddress[0]
                val city = location.locality
                val state = location.adminArea
                // val country = location.countryName
                val countryCode = location.countryCode

                val currLocation = listOf<String>(city, state, countryCode)

                viewModel.setLocation(currLocation)
                viewModel.setCategory(getCurrentCategory())

                findNavController().navigate(R.id.action_post_criteria_to_post_info)
            } else {
                Toast.makeText(activity,
                    "Enter a valid address in the US!",
                    Toast.LENGTH_LONG).show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentCategory(): Category {
        return when (binding.categories.checkedRadioButtonId) {
            binding.radioApartments.id -> Category.APARTMENT
            binding.radioVideoGames.id -> Category.VIDEO_GAMES
            binding.radioElectronics.id -> Category.ELECTRONICS
            binding.radioTools.id -> Category.TOOLS
            binding.radioCollectibleCards.id -> Category.COLLECTIBLE_CARDS
            else -> Category.OTHER
        }
    }

}