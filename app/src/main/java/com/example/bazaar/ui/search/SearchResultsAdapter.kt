package com.example.bazaar.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaar.Model.UserPost
import com.example.bazaar.R
import com.example.bazaar.ui.search.SearchResultsAdapter
import com.example.bazaar.ui.search.SearchResultsViewModel
import com.example.bazaar.databinding.SearchResultRowBinding

class SearchResultsAdapter(private val viewModel: SearchResultsViewModel,
                           private val deletePos:((Int)->Unit)? = null) :
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
        fun bind(currUserPost: UserPost, position: Int) {

            if (currUserPost.pictureUUIDs.isNotEmpty()) {
                val pictureUUID = currUserPost.pictureUUIDs[0]
                viewModel.glideFetch(pictureUUID, photoIB)
            }
            searchRowBinding.postTitle.text = currUserPost.title
            searchRowBinding.postDescription.text = currUserPost.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val searchRowBinding = SearchResultRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VH(searchRowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[position], position)
    }
}