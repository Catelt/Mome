package com.catelt.mome.ui.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import coil.load
import com.catelt.mome.R
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.data.model.Video
import com.catelt.mome.data.model.getThumbnailUrl
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.databinding.ItemVideoTrailerBinding

class TrailerAdapter : BaseAdapter<Video>() {
    var movie: MovieDetails? = null
    var tvShow: TvShowDetails? = null

    private lateinit var context: Context
    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        context = parent.context
        return ItemVideoTrailerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemVideoTrailerBinding).apply {
            imgBackdrop.load(item.getThumbnailUrl())
            if (movie != null){
                txtTitle.text =
                    context.getString(R.string.text_title_trailer, position + 1, movie?.title)
            }
            else{
                txtTitle.text =
                    context.getString(R.string.text_title_trailer, position + 1, tvShow?.title)
            }
        }
    }
}