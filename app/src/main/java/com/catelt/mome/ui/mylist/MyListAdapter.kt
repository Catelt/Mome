package com.catelt.mome.ui.mylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.core.BasePagingAdapter
import com.catelt.mome.data.model.MediaType
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.databinding.ItemMediaBinding
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.loadDefault

class MyListAdapter : BasePagingAdapter<Media>() {
    var imageUrlParser: ImageUrlParser? = null
    var onMovieClicked: ((Int, MediaType) -> Unit)? = null

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position) ?: return
        (binding as ItemMediaBinding).apply {
            item.let {
                imgPoster.loadDefault(
                    imageUrlParser?.getImageUrl(
                        it.posterPath,
                        ImageUrlParser.ImageType.Poster
                    )
                )
                imgPoster.setOnClickListener {
                    onMovieClicked?.invoke(item.id, item.type ?: MediaType.Movie)
                }
            }
        }
    }
}