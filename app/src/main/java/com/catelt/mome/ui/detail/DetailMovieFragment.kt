package com.catelt.mome.ui.detail


import androidx.core.view.isVisible
import com.catelt.mome.adapter.ListGridAdapter
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentDetailMovieBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class DetailMovieFragment : BaseFragment<FragmentDetailMovieBinding>(
    FragmentDetailMovieBinding::inflate
) {
    private val trailerAdapter = TrailerAdapter()
    private val likeThisAdapter = ListGridAdapter()

    override fun setUpViews() {
        binding.apply {
            recyclerViewLikeThis.adapter = likeThisAdapter
            recyclerViewTrailer.adapter = trailerAdapter
            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val isTrailerList = tab?.position == 1
                    recyclerViewLikeThis.isVisible = !isTrailerList
                    recyclerViewTrailer.isVisible = isTrailerList
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }
    }
}