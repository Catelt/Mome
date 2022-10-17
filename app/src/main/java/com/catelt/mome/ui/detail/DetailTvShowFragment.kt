package com.catelt.mome.ui.detail

import androidx.core.view.isVisible
import com.catelt.mome.adapter.ListGridAdapter
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentDetailTvShowBinding
import com.google.android.material.tabs.TabLayout

class DetailTvShowFragment : BaseFragment<FragmentDetailTvShowBinding>(
    FragmentDetailTvShowBinding::inflate
) {
    private val episodeAdapter = EpisodeAdapter()
    private val trailerAdapter = TrailerAdapter()
    private val likeThisAdapter = ListGridAdapter()

    override fun setUpAdapter() {
        binding.apply {
            recyclerEpisode.adapter = episodeAdapter
            recyclerViewLikeThis.adapter = likeThisAdapter
            recyclerViewTrailer.adapter = trailerAdapter
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val position = tab?.position ?: 0
                    txtNameTvShow.isVisible = position == EPISODE
                    recyclerEpisode.isVisible = position == EPISODE
                    recyclerViewTrailer.isVisible = position == TRAILER
                    recyclerViewLikeThis.isVisible = position == LIKE_THIS
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }
    }

    companion object{
        const val EPISODE = 0
        const val TRAILER = 1
        const val LIKE_THIS = 2
    }
}