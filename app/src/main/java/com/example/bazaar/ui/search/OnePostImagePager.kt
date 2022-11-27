package com.example.bazaar.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bazaar.databinding.OnePostImagePagerBinding

class OnePostImagePager : AppCompatActivity() {

    private lateinit var onePostBinding : OnePostImagePagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onePostBinding = OnePostImagePagerBinding.inflate(layoutInflater)
        setContentView(onePostBinding.root)

        // XXX Write me Set our currentUser variable based on what MainActivity passed us
        val pictureUUIDs = intent.getStringArrayListExtra("pictureUUIDs")

        val viewPagerAdapter = OnePostViewPagerAdapter(this, pictureUUIDs!!)

        val viewPager = onePostBinding.idViewPager
        viewPager.adapter = viewPagerAdapter
    }
}