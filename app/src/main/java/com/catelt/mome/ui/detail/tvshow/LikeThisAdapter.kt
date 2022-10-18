package com.catelt.mome.ui.detail.tvshow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import coil.load
import com.catelt.mome.core.BasePagingAdapter
import com.catelt.mome.data.model.tvshow.TvShow
import com.catelt.mome.databinding.ItemMediaBinding
import com.catelt.mome.utils.ImageUrlParser

class LikeThisAdapter: BasePagingAdapter<TvShow>() {
    var imageUrlParser: ImageUrlParser? = null
    var onMovieClicked: ((Int) -> Unit)? = null

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemMediaBinding).apply {
            item?.let {
                imgPoster.load(imageUrlParser?.getImageUrl(it.posterPath, ImageUrlParser.ImageType.Poster)){
                    crossfade(true)
                }
                imgPoster.setOnClickListener {
                    onMovieClicked?.invoke(item.id)
                }
            }
        }
    }
}