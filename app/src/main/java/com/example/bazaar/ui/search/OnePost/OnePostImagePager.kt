package com.example.bazaar.ui.search.OnePost

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

        val viewPagerAdapter = OnePostImagePagerAdapter(this, pictureUUIDs!!)

        val viewPager = onePostBinding.idViewPager
        viewPager.adapter = viewPagerAdapter

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false); // remove the icon

    }
}