package com.catelt.mome.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.databinding.ItemTopMovieBinding


class TopSearchAdapter : BaseAdapter<String>(){
    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemTopMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        binding.apply {

        }
    }
}