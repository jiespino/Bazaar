package com.example.bazaar.ui.search

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentSearchCriteriaBinding
import com.example.bazaar.ui.createPost.Category
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import java.util.*


class SearchCriteriaFragment: Fragment() {

    private var _binding: FragmentSearchCriteriaBinding? = null
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

        _binding = FragmentSearchCriteriaBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val radioGroup = binding.categories

        val priceHelpText = binding.priceHelpText
        val priceSlider: RangeSlider = binding.priceSlider
        priceSlider.setValues(1000.0f, 5000.0f)
        priceSlider.stepSize = 100.0f

        val sqFtHelpText = binding.sqFeetHelpText
        val sqFtSlider: RangeSlider = binding.sqFeetSlider
        sqFtSlider.setValues(500f, 2000.0f)
        sqFtSlider.stepSize = 50.0F

        val roomHelpText = binding.roomHelpText
        val roomSlider: RangeSlider = binding.roomSlider
        roomSlider.setValues(1.0f, 5.0f)
        roomSlider.stepSize = 1.0f

        val bathHelpText = binding.bathHelpText
        val bathSlider: RangeSlider = binding.bathSlider
        bathSlider.setValues(1.0f, 5.0f)
        bathSlider.stepSize = 1.0f


        radioGroup.setOnCheckedChangeListener { _, i ->
            if (i == binding.radioApartments.id) {
                priceHelpText.text = resources.getString(R.string.price_apt_help_text)
                sqFtHelpText.visibility = View.VISIBLE
                sqFtSlider.visibility = View.VISIBLE

                roomHelpText.visibility = View.VISIBLE
                roomSlider.visibility = View.VISIBLE

                bathHelpText.visibility = View.VISIBLE
                bathSlider.visibility = View.VISIBLE


            } else {
                priceHelpText.text = resources.getString(R.string.price_help_text)
                sqFtHelpText.visibility = View.INVISIBLE
                sqFtSlider.visibility = View.INVISIBLE

                roomHelpText.visibility = View.INVISIBLE
                roomSlider.visibility = View.INVISIBLE

                bathHelpText.visibility = View.INVISIBLE
                bathSlider.visibility = View.INVISIBLE

            }
        }

        geocoder = Geocoder(this.context, Locale.getDefault())



        val continueButton = binding.continueButton
        continueButton.setOnClickListener{

            val address = binding.locationEditText.text.toString()

            if (address.isEmpty()) {
                Toast.makeText(activity,
                    "Enter a location!",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }



            val geoAddress = geocoder.getFromLocationName(address, 1)

            if (geoAddress.size > 0) {
                val location: Address = geoAddress[0]
                val city = location.locality
                val state = location.adminArea
                val countryCode = location.countryCode

                val usLocation = listOf<String>(city, state, countryCode)
                viewModel.setLocation(usLocation)

                val currentCategory = getCurrentCategory()
                viewModel.setCategory(currentCategory)

                val searchText = binding.searchEditText.text.toString()
                val priceRange = binding.priceSlider.values
                val sqFeetRange = binding.sqFeetSlider.values
                val roomRange = binding.roomSlider.values
                val bathRange = binding.bathSlider.values

                val aptCriteria = listOf(sqFeetRange, roomRange, bathRange)

                viewModel.setSearchCriteria(listOf(searchText, priceRange, aptCriteria))

                findNavController().navigate(R.id.search_criteria_to_search_results)
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