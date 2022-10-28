package com.catelt.mome.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BasePagingAdapter
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.databinding.ItemPosterBinding
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.loadDefault

class HorizontalAdapter: BasePagingAdapter<Presentable>() {
    var imageUrlParser: ImageUrlParser? = null
    var onMovieClicked: ((Int) -> Unit)? = null

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemPosterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemPosterBinding).apply {
            item?.let {
                imgPoster.loadDefault(imageUrlParser?.getImageUrl(it.posterPath,ImageUrlParser.ImageType.Poster))
                imgPoster.setOnClickListener {
                    onMovieClicked?.invoke(item.id)
                }
                if (position == 0) root.setPadding(15,0,0,0)
            }
        }
    }

}