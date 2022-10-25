package com.catelt.mome.ui.videoplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.R
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.data.model.ophim.Media
import com.catelt.mome.databinding.ItemEpisodeVideoViewBinding
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.loadDefault

class EpisodeVideoPlayerAdapter : BaseAdapter<Media>() {
    var imageUrlParser: ImageUrlParser? = null
    var onMovieClicked: ((Int) -> Unit)? = null

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemEpisodeVideoViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemEpisodeVideoViewBinding).apply {
            item?.let { tvshow ->
                imgBackdrop.loadDefault(imageUrlParser?.getImageUrl(tvshow.stillPath,ImageUrlParser.ImageType.Backdrop))
                imgBackdrop.setOnClickListener {
                    onMovieClicked?.invoke(position)
                }
                txtOverview.text = tvshow.overview
                txtTitle.text = root.context.getString(R.string.title_episode,tvshow.numberEpisode,tvshow.title)
            }
        }
    }
}