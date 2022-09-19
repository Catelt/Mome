package com.catelt.mome.ui.new

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.databinding.ItemComingMovieBinding

class UpComingAdapter : BaseAdapter<String>() {
    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemComingMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val movie = getItem(position)
        (binding as ItemComingMovieBinding).apply {
            txtTitle.text = movie
            btnRemind.setOnClickListener {
                btnRemind.setDefault(!btnRemind.type)
            }
        }
    }

}