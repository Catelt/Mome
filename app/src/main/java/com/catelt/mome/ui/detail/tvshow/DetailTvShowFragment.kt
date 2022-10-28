package com.catelt.mome.ui.detail.tvshow

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.data.model.AggregatedCredits
import com.catelt.mome.data.model.Video
import com.catelt.mome.data.model.getThumbnailUrl
import com.catelt.mome.data.model.toStringCast
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.databinding.FragmentDetailTvShowBinding
import com.catelt.mome.ui.bottomsheet.MediaDetailsBottomSheet
import com.catelt.mome.ui.components.CustomPlayerUiController
import com.catelt.mome.ui.detail.TrailerAdapter
import com.catelt.mome.ui.detail.tvshow.adapter.EpisodeAdapter
import com.catelt.mome.ui.detail.tvshow.adapter.LikeThisAdapter
import com.catelt.mome.utils.BUNDLE_CURRENT_EPISODE
import com.catelt.mome.utils.BUNDLE_ID_MEDIA
import com.catelt.mome.utils.BUNDLE_SLUG_MEDIA
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.getCalendarRelease
import com.catelt.mome.utils.extension.loadDefault
import com.catelt.mome.utils.extension.setAgeTitle
import com.google.android.material.tabs.TabLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class DetailTvShowFragment : BaseFragment<FragmentDetailTvShowBinding>(
    FragmentDetailTvShowBinding::inflate
) {
    override val viewModel: DetailTvShowViewModel by viewModels()

    private var listener: YouTubePlayerListener? = null
    private val episodeAdapter = EpisodeAdapter()
    private val trailerAdapter = TrailerAdapter()
    private val likeThisAdapter = LikeThisAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun setUpViews() {
        binding.apply {
            likeThisAdapter.onMovieClicked = { tvShowID ->
                MediaDetailsBottomSheet.newInstance(tvShowID, false)
                    .show(requireActivity().supportFragmentManager, tvShowID.toString())
            }

            episodeAdapter.onMovieClicked = { episodeNumber, name ->
                viewModel.uiState.value.associatedContentTvShow.ophim?.apply {
                    episodeResponses[0].let {
                        it.episodes.let { list ->
                            if (list.size >= episodeNumber) {
                                setOnClickPlayVideo(episodeNumber - 1)
                            }
                        }
                    }
                }
            }

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

            layoutHeader.layoutDirector.visibility = setVisionView(false)

            btnProfile.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }

            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            toastMessage.observe(viewLifecycleOwner) {
                if (it.isNotBlank()) {
                    toast(it)
                    toastMessage.postValue("")
                }
            }

            lifecycleScope.launch {
                launch {
                    isMyList.collectLatest {
                        binding.layoutHeader.btnList.setUI(it)
                    }
                }

                imageUrlParser.collectLatest { imageParser ->
                    episodeAdapter.imageUrlParser = imageParser
                    likeThisAdapter.imageUrlParser = imageParser

                    uiState.collectLatest { uiState ->
                        uiState.tvShowDetails?.let { tvShowDetails ->
                            setupTvShowDetail(tvShowDetails)
                            trailerAdapter.tvShow = tvShowDetails
                        }

                        uiState.associatedContentTvShow.videos?.let { videos ->
                            val trailers = mutableListOf<Video>()
                            videos.forEach { video ->
                                if (video.type == getString(R.string.trailer)) {
                                    trailers.add(video)
                                }
                            }
                            trailerAdapter.submitList(trailers)
                            setupYoutubeView(trailers,videos)
                        }
                        uiState.associatedContentTvShow.credits?.let { credits ->
                            setupCredit(credits)
                        }

                        uiState.associatedTvShow.seasonDetails?.let {
                            val list = it.episodes.filter { episode ->
                                episode.isReleased() && episode.overview.isNotBlank()
                            }
                            episodeAdapter.submitList(list)
                        }

                        uiState.associatedTvShow.similar.flowOn(Dispatchers.IO).onEach { list ->
                            likeThisAdapter.submitData(list)
                        }.launchIn(lifecycleScope)

                        binding.layoutHeader.btnPlay.apply {
                            setEnable(uiState.associatedContentTvShow.ophim?.status == true)
                            setOnClickListener {
                                setOnClickPlayVideo()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupYoutubeView(trailers : MutableList<Video>,videos : List<Video> ){
        binding.layoutThumbnail.youtubePlayerView.apply {
            val customPlayerUi =
                inflateCustomPlayerUi(R.layout.view_custom_youtube_player)
            val video = if (trailers.isEmpty()) {
                if (videos.isNotEmpty()) {
                    videos.last()
                } else {
                    null
                }
            } else {
                trailers.last()
            }

            if (listener == null) {
                lifecycle.addObserver(this)
                video?.let {
                    customPlayerUi.findViewById<ImageView>(R.id.imgBackdrop)
                        .apply {
                            load(video.getThumbnailUrl())
                        }
                    customPlayerUi.findViewById<TextView>(R.id.txtTypeVideo)
                        .apply {
                            visibility = View.VISIBLE
                            text = video.type
                        }

                    listener = object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            val customPlayerUiController =
                                CustomPlayerUiController(
                                    requireContext(),
                                    customPlayerUi,
                                    youTubePlayer,
                                    binding.layoutThumbnail.youtubePlayerView
                                )
                            youTubePlayer.addListener(
                                customPlayerUiController
                            )
                            youTubePlayer.loadVideo(video.key, 0f)
                        }
                    }

                    val options: IFramePlayerOptions =
                        IFramePlayerOptions.Builder().controls(0).build()

                    listener?.let{
                        initialize(it, options)
                    }
                }
            }

            binding.layoutThumbnail.youtubePlayerView.visibility =
                setVisionView(video != null)
            binding.layoutThumbnail.imgBackdrop.visibility =
                setVisionView(video == null)
        }
    }

    private fun setOnClickPlayVideo(position: Int = 0) {
        viewModel.uiState.value.apply {
            associatedContentTvShow.ophim?.apply {
                findNavController().navigate(
                    R.id.videoPlayerFragment,
                    bundleOf(
                        BUNDLE_ID_MEDIA to tvShowDetails?.id,
                        BUNDLE_SLUG_MEDIA to movie.slug,
                        BUNDLE_CURRENT_EPISODE to position,
                    )
                )
            }
        }
    }

    private fun setupTvShowDetail(tvShow: TvShowDetails) {
        binding.apply {
            layoutHeader.apply {
                txtTitle.text = tvShow.title
                txtYear.text = tvShow.getCalendarRelease()?.get(Calendar.YEAR).toString()
                if (tvShow.numberOfSeasons > 1) {
                    txtRuntime.text =
                        getString(R.string.text_run_time_season, tvShow.numberOfSeasons)
                } else {
                    txtRuntime.text =
                        getString(R.string.text_run_time_episodes, tvShow.numberOfEpisodes)
                }
                txtOverview.text = tvShow.overview
                txtAge.setAgeTitle(tvShow.adult ?: false)

                btnList.apply {
                    setOnClickListener {
                        if (isExisted) {
                            viewModel.onRemoveClick(tvShow)
                        } else {
                            viewModel.onAddMediaClick(tvShow)
                        }
                        setUI(!isExisted)
                    }
                }

                btnRate.setOnClickListener {
                    toast(getString(R.string.message_feature_coming_soon))
                }

                btnShare.setOnClickListener {
                    toast(getString(R.string.message_feature_coming_soon))
                }
            }
            txtNameTvShow.text = tvShow.name
            layoutThumbnail.imgBackdrop.loadDefault(
                likeThisAdapter.imageUrlParser?.getImageUrl(
                    tvShow.backdropPath,
                    ImageUrlParser.ImageType.Backdrop
                )
            )
        }
    }

    private fun setupCredit(credits: AggregatedCredits) {
        binding.layoutHeader.apply {
            txtListStarring.text = credits.toStringCast()
        }
    }

    override fun onStop() {
        super.onStop()
        listener = null
    }

    companion object {
        const val EPISODE = 0
        const val TRAILER = 1
        const val LIKE_THIS = 2
    }
}