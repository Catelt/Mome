package com.catelt.mome.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import coil.load
import com.catelt.mome.core.BasePagingAdapter
import com.catelt.mome.data.model.MediaType
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.databinding.ItemTopMovieBinding
import com.catelt.mome.utils.ImageUrlParser


class TopSearchAdapter : BasePagingAdapter<Movie>(){
    var imageUrlParser: ImageUrlParser? = null
    var onMovieClicked: ((Int, MediaType) -> Unit)? = null

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemTopMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemTopMovieBinding).apply {
            item?.let { movie ->
                imgBackdrop.load(imageUrlParser?.getImageUrl(movie.backdropPath,ImageUrlParser.ImageType.Backdrop)){
                    crossfade(true)
                }
                txtName.text = movie.title
            }
        }
    }

    override fun setOnClickItem(position: Int) {
        val item = getItem(position)
        item?.let {
            onMovieClicked?.invoke(item.id,MediaType.Movie)
        }
    }
}