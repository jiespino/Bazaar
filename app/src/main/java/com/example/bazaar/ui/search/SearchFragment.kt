package com.example.bazaar.ui.search

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentSearchBinding
import com.example.bazaar.ui.postInformation.Category
import java.util.*

class SearchFragment: Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var geocoder: Geocoder
    private val viewModel: SearchResultsViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
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
                val country = location.countryName
                val countryCode = location.countryCode

                val usLocation = listOf<String>(city, state, countryCode)

                viewModel.setLocation(usLocation.joinToString(","))
                viewModel.setCategory(getCurrentCategory())

                // val p1 = LatLng(location.latitude, location.longitude)
                parentFragmentManager.commit {
                    replace(R.id.nav_host_fragment_activity_main, SearchResultsFragment())
                    setReorderingAllowed(true)
                    addToBackStack("postInfo")
                }
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