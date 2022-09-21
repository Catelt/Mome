package com.catelt.mome.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.databinding.ItemEpisodeBinding

class EpisodeAdapter : BaseAdapter<String>() {
    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        binding.apply {

        }
    }
}