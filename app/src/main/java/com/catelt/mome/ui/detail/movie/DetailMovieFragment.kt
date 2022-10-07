package com.catelt.mome.ui.detail.movie


import android.os.Bundle
import android.transition.TransitionInflater
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.adapter.ListGridAdapter
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.data.model.Credits
import com.catelt.mome.data.model.getDirector
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.toStringCast
import com.catelt.mome.databinding.FragmentDetailMovieBinding
import com.catelt.mome.ui.bottomsheet.MediaDetailsBottomSheet
import com.catelt.mome.ui.detail.TrailerAdapter
import com.catelt.mome.utils.extension.getCalendarRelease
import com.catelt.mome.utils.extension.getRunTime
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class DetailMovieFragment : BaseFragment<FragmentDetailMovieBinding>(
    FragmentDetailMovieBinding::inflate
) {
    private val trailerAdapter = TrailerAdapter()
    private val likeThisAdapter = ListGridAdapter()
    override val viewModel: DetailMovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun setUpViews() {
        binding.apply {
            recyclerViewLikeThis.adapter = likeThisAdapter
            likeThisAdapter.onMovieClicked = { movieId ->
                MediaDetailsBottomSheet.newInstance(movieId)
                    .show(requireActivity().supportFragmentManager, movieId.toString())
            }

            recyclerViewTrailer.adapter = trailerAdapter
            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
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

            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            lifecycleScope.launch {
                imageUrlParser.collectLatest { imageParser ->
                    likeThisAdapter.imageUrlParser = imageParser

                    uiState.collectLatest {
                        it.movieDetails?.let { movieDetails ->
                            setupMovieDetail(movieDetails)
                            trailerAdapter.movie = movieDetails
                        }

                        it.associatedContent.videos?.let { videos ->
                            binding.layoutThumbnail.youtubePlayerView.apply {
                                lifecycle.addObserver(this)

                                addYouTubePlayerListener(object :
                                    AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        youTubePlayer.loadVideo(videos[0].key, 0f)
                                    }
                                })
                            }

                            val trailers = mutableListOf<com.catelt.mome.data.model.Video>()
                            videos.forEach { video ->
                                if (video.type == "Trailer") {
                                    trailers.add(video)
                                }
                            }
                            trailerAdapter.submitList(trailers)
                        }

                        it.associatedContent.credits?.let { credits ->
                            setupCredit(credits)
                        }

                        it.associatedMovies.similar.flowOn(Dispatchers.IO).onEach { list ->
                            likeThisAdapter.submitData(list)
                        }.launchIn(lifecycleScope)
                    }

                }
            }
        }
    }

    private fun setupMovieDetail(movie: MovieDetails) {
        binding.apply {
            layoutHeader.apply {
                txtTitle.text = movie.title
                txtYear.text = movie.getCalendarRelease()?.get(Calendar.YEAR).toString()
                txtRuntime.text = movie.getRunTime()
                txtOverview.text = movie.overview
                if (movie.adult) {
                    txtAge.text = getString(R.string.text_18_plus)
                } else {
                    txtAge.text = getString(R.string.text_13_plus)
                }
            }
        }
    }

    private fun setupCredit(credits: Credits) {
        binding.layoutHeader.apply {
            txtListStarring.text = credits.toStringCast()
            txtDirector.text = credits.getDirector()?.name
        }
    }
}