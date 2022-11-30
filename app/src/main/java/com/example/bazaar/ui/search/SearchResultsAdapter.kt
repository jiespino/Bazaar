package com.example.bazaar.ui.search
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaar.Model.UserPost
import com.example.bazaar.R
import com.example.bazaar.ui.search.SearchResultsAdapter
import com.example.bazaar.ui.search.SearchResultsViewModel
import com.example.bazaar.databinding.SearchResultRowBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.bazaar.ui.createPost.Category
import com.google.android.material.internal.ContextUtils.getActivity
import java.util.*

class SearchResultsAdapter(private val viewModel: SearchResultsViewModel, private val fragManager: FragmentManager) :
    ListAdapter<UserPost, SearchResultsAdapter.VH>(Diff()) {
    // This class allows the adapter to compute what has changed
    class Diff : DiffUtil.ItemCallback<UserPost>() {
        override fun areItemsTheSame(oldItem: UserPost, newItem: UserPost): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: UserPost, newItem: UserPost): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(private val searchRowBinding: SearchResultRowBinding) :
        RecyclerView.ViewHolder(searchRowBinding.root) {
        private var photoIB: ImageButton = searchRowBinding.mediaThumbnail
        fun bind(currUserPost: UserPost) {

            if (currUserPost.pictureUUIDs.isNotEmpty()) {
                val pictureUUID = currUserPost.pictureUUIDs[0]
                SearchResultsViewModel.glideFetch(pictureUUID, photoIB)
            }

            val city = currUserPost.city
            val state = currUserPost.state
            val countryCode = currUserPost.countryCode
            searchRowBinding.postLocation.text = "$city $state, $countryCode"
            searchRowBinding.postCategory.text = currUserPost.category.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            searchRowBinding.postTitle.text = currUserPost.title
            searchRowBinding.postDescription.text = currUserPost.description
            searchRowBinding.postPrice.text = currUserPost.price.toString()
            searchRowBinding.postEmail.text = currUserPost.userEmail

            val price = currUserPost.price
            val context = photoIB.context

            if (currUserPost.phoneNumber.isNotEmpty()) {
                searchRowBinding.postPhone.text = currUserPost.phoneNumber
            }

            if (currUserPost.category == Category.APARTMENT.toString()) {
                val sqFeetHelpText = context.getString(R.string.sq_feet_abbr_help_text)
                val sqFeetText = currUserPost.aptInfo?.squareFeet.toString()
                searchRowBinding.postSquareFeet.text = "$sqFeetHelpText $sqFeetText"

                val roomHelpText = context.getString(R.string.room_help_text)
                val roomText = currUserPost.aptInfo?.rooms.toString()
                searchRowBinding.postRooms.text = "$roomHelpText $roomText"

                val bathHelpText = context.getString(R.string.bath_help_text)
                val bathText = currUserPost.aptInfo?.baths.toString()
                searchRowBinding.postBaths.text = "$bathHelpText $bathText"
            } else {
                searchRowBinding.postSquareFeet.visibility = View.GONE
                searchRowBinding.postRooms.visibility = View.GONE
                searchRowBinding.postBaths.visibility = View.GONE
            }

            val priceHelpText = context.getString(R.string.price_help_text)
            searchRowBinding.postPrice.text = "$priceHelpText $price"

            searchRowBinding.mediaThumbnail.setOnClickListener {
                viewModel.setUserPost(currUserPost)
                fragManager.primaryNavigationFragment?.findNavController()?.navigate(R.id.one_post_for_search)
            }

            searchRowBinding.searchResultCard.setOnClickListener {
                viewModel.setUserPost(currUserPost)
                fragManager.primaryNavigationFragment?.findNavController()?.navigate(R.id.one_post_for_search)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val searchRowBinding = SearchResultRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VH(searchRowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[position])
    }
}