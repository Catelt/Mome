package com.catelt.mome.ui.home


import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {
    private val feedAdapter = FeedAdapter()
    init {
        isFullScreen = true
    }

    override fun setUpViews() {
        binding.apply {
            btnSearch.setOnClickListener{
                findNavController().navigate(R.id.searchFragment)
            }
            layoutHeaderHome.btnPlay.setOnClickListener {
                findNavController().navigate(R.id.detailTvShowFragment)
            }

            recyclerViewListFeed.adapter = feedAdapter

            feedAdapter.submitList(listOf(
                getString(R.string.title_trending),
                getString(R.string.title_popular),
                getString(R.string.title_top_rate)
            ))
        }
    }
}