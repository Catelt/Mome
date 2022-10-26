package com.catelt.mome.ui.detail.tvshow.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.R
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.data.model.Episode
import com.catelt.mome.databinding.ItemEpisodeBinding
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.loadDefault

class EpisodeAdapter : BaseAdapter<Episode>() {
    var imageUrlParser: ImageUrlParser? = null
    var onMovieClicked: ((Int, String) -> Unit)? = null

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemEpisodeBinding).apply {
            item?.let { tvshow ->
                imgBackdrop.loadDefault(imageUrlParser?.getImageUrl(tvshow.stillPath,ImageUrlParser.ImageType.Backdrop))
                txtOverview.text = tvshow.overview
                txtTitle.text = root.context.getString(R.string.title_episode,tvshow.episodeNumber,tvshow.name)
            }
        }
    }

    override fun setOnClickItem(position: Int) {
        val item = getItem(position)
        item?.let {
            onMovieClicked?.invoke(item.episodeNumber,item.name)
        }
    }
}