package com.catelt.mome.ui.detail.movie


import android.os.Bundle
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
import com.catelt.mome.adapter.ListGridAdapter
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.databinding.FragmentDetailMovieBinding
import com.catelt.mome.ui.bottomsheet.MediaDetailsBottomSheet
import com.catelt.mome.ui.components.CustomPlayerUiController
import com.catelt.mome.ui.detail.TrailerAdapter
import com.catelt.mome.utils.*
import com.catelt.mome.utils.extension.getCalendarRelease
import com.catelt.mome.utils.extension.getRunTime
import com.catelt.mome.utils.extension.loadDefault
import com.catelt.mome.utils.extension.setAgeTitle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
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
class DetailMovieFragment : BaseFragment<FragmentDetailMovieBinding>(
    FragmentDetailMovieBinding::inflate
) {
    override val viewModel: DetailMovieViewModel by viewModels()
    override var isTransitionInflater = true
    private var listener: YouTubePlayerListener? = null

    private val trailerAdapter = TrailerAdapter()
    private val likeThisAdapter = ListGridAdapter()

    private var isUpComing = ""

    override fun setUpArgument(bundle: Bundle) {
        bundle.apply {
            isUpComing = getString(BUNDLE_DETAIL_UPCOMING,"")
        }
    }

    override fun setUpViews() {
        binding.apply {
            if (isUpComing.isNotBlank()){
                layoutHeader.apply {
                    // TODO: Handle Button Play
//                    btnPlay.visibility = setVisionView(false)
                    txtArrivalDate.visibility = setVisionView(true)
                    txtArrivalDate.text = isUpComing
                    root.visibility = setVisionView(false)
                }
                layoutShimmer.visibility = setVisionView(true)
                layoutShimmer.startShimmer()
            }

            recyclerViewLikeThis.adapter = likeThisAdapter
            likeThisAdapter.onMovieClicked = { movieId ->
                MediaDetailsBottomSheet.newInstance(movieId, true)
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
            toastMessage.observe(viewLifecycleOwner){
                if (it.isNotBlank()){
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
                    likeThisAdapter.imageUrlParser = imageParser

                    uiState.collectLatest { movieDetailSate ->
                        movieDetailSate.movieDetails?.let { movieDetails ->
                            setupMovieDetail(movieDetails)
                            trailerAdapter.movie = movieDetails
                        }

                        movieDetailSate.associatedContent.videos?.let { videos ->
                            val trailers = mutableListOf<Video>()
                            videos.forEach { video ->
                                if (video.type == getString(R.string.trailer)) {
                                    trailers.add(video)
                                }
                            }
                            trailerAdapter.submitList(trailers)
                            setupYoutubeView(trailers, videos)
                        }

                        movieDetailSate.associatedContent.credits?.let { credits ->
                            setupCredit(credits)
                        }

                        movieDetailSate.associatedMovies.similar.flowOn(Dispatchers.IO)
                            .onEach { list ->
                                likeThisAdapter.submitData(list)
                            }.launchIn(lifecycleScope)

                        // TODO: Handle Button Play
//                        binding.layoutHeader.btnPlay.apply {
//                            setEnable(movieDetailSate.ophim?.status == true)
//                            setOnClickListener {
//                                setOnClickPlayVideo()
//                            }
//                        }
                    }

                }
            }
        }
    }

    private fun setupYoutubeView(trailers : MutableList<Video>, videos : List<Video> ){
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

                    println(it.type)
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

                    listener?.let{ listener ->
                        initialize(listener, options)
                    }
                }

                binding.layoutThumbnail.youtubePlayerView.visibility =
                    setVisionView(video != null)
            }
        }
    }

    private fun setOnClickPlayVideo(position: Int = 0) {
        viewModel.uiState.value.apply {
            ophim?.apply {
                findNavController().navigate(
                    R.id.videoPlayerFragment,
                    bundleOf(
                        BUNDLE_TITLE_MEDIA to binding.layoutHeader.txtTitle.text,
                        BUNDLE_ID_MEDIA to movieDetails?.id,
                        BUNDLE_SLUG_MEDIA to movie.slug,
                        BUNDLE_CURRENT_EPISODE to position,
                    )
                )
            }
        }
    }

    private fun setupMovieDetail(movie: MovieDetails) {
        binding.apply {
            layoutHeader.apply {
                root.visibility = setVisionView(true)
                layoutShimmer.visibility = setVisionView(false)
                layoutShimmer.stopShimmer()

                txtTitle.text = movie.title
                txtYear.text = movie.getCalendarRelease()?.get(Calendar.YEAR).toString()
                txtRuntime.text = movie.getRunTime()
                txtOverview.text = movie.overview
                txtAge.setAgeTitle(movie.adult)

                btnList.apply {
                    setOnClickListener {
                        if (isExisted){
                            viewModel.onRemoveClick(movie)
                        }
                        else{
                            viewModel.onAddMediaClick(movie)
                        }
                        setUI(!isExisted)
                    }
                }

                // TODO: Handle Button Rate
//                btnRate.setOnClickListener {
//                    toast(getString(R.string.message_feature_coming_soon))
//                }

                // TODO: Handle Button Share
//                lifecycleScope.launch {
//                    launch {
//                        ShareUtils.share(movie,true,isUpComing).collectLatest { link ->
//                            btnShare.setOnClickListener {
//                                ShareUtils.shareUrl(requireActivity(),link,movie.title)
//                            }
//                        }
//                    }
//                }
            }
            layoutThumbnail.imgBackdrop.loadDefault(likeThisAdapter.imageUrlParser?.getImageUrl(movie.backdropPath, ImageUrlParser.ImageType.Backdrop))
        }
    }

    private fun setupCredit(credits: Credits) {
        binding.layoutHeader.apply {
            txtListStarring.text = credits.toStringCast()
            txtDirector.text = credits.getDirector()?.name
        }
    }

    override fun onStop() {
        super.onStop()
        listener = null
    }
}