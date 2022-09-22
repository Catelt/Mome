package com.catelt.mome.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.databinding.ItemPosterBinding

class HorizontalAdapter: BaseAdapter<String>() {
    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemPosterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemPosterBinding).apply {

        }
    }

}