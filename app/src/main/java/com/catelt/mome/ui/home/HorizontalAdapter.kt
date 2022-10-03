package com.catelt.mome.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import coil.load
import com.catelt.mome.core.BasePagingAdapter
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.databinding.ItemPosterBinding
import com.catelt.mome.utils.ImageUrlParser

class HorizontalAdapter: BasePagingAdapter<Presentable>() {
    var imageUrlParser: ImageUrlParser? = null

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemPosterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemPosterBinding).apply {
            item?.let {
                imgPoster.load(imageUrlParser?.getImageUrl(it.posterPath,ImageUrlParser.ImageType.Poster)){
                    crossfade(true)
                }
            }
        }
    }

}