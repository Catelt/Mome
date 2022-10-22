package com.catelt.mome.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BasePagingAdapter
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.databinding.ItemMediaBinding
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.loadDefault

class ListGridAdapter : BasePagingAdapter<Movie>() {
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
                imgPoster.loadDefault(imageUrlParser?.getImageUrl(it.posterPath,ImageUrlParser.ImageType.Poster))
                imgPoster.setOnClickListener {
                    onMovieClicked?.invoke(item.id)
                }
            }
        }
    }
}