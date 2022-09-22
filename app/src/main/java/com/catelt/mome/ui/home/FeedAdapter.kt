package com.catelt.mome.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.databinding.ItemFeedHorizontalListBinding

class FeedAdapter : BaseAdapter<String>(){
    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemFeedHorizontalListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemFeedHorizontalListBinding).apply {
            txtTitle.text = item
            val adapter = HorizontalAdapter()
            recyclerViewHorizontal.adapter = adapter
            adapter.submitList(listOf("a","b","c","a","b","c","a","b","c"))
        }
    }
}