package com.catelt.mome.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.databinding.ItemMediaBinding

class ListGridAdapter : BaseAdapter<String>() {
    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        binding.apply {

        }
    }
}