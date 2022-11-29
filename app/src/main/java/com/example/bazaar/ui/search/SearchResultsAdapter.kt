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
import com.google.android.material.internal.ContextUtils.getActivity

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
            searchRowBinding.postTitle.text = currUserPost.title
            searchRowBinding.postDescription.text = currUserPost.description

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