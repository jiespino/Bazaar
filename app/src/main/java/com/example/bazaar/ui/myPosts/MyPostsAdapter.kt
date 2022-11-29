package com.example.bazaar.ui.myPosts

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaar.Model.UserPost
import com.example.bazaar.R
import com.example.bazaar.databinding.SearchResultRowBinding
import com.example.bazaar.ui.myPosts.OnePost.OnePostFragment
import com.example.bazaar.ui.search.SearchResultsViewModel

class MyPostsAdapter(private val viewModel: MyPostsViewModel, private val fragManager: FragmentManager) :
    ListAdapter<UserPost, MyPostsAdapter.VH>(Diff()) {
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
                fragManager.primaryNavigationFragment?.findNavController()?.navigate(R.id.one_post_for_my_post)
            }

            searchRowBinding.searchResultCard.setOnClickListener {
                viewModel.setUserPost(currUserPost)
                fragManager.primaryNavigationFragment?.findNavController()?.navigate(R.id.one_post_for_my_post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val searchRowBinding = SearchResultRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return VH(searchRowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[position])
    }
}