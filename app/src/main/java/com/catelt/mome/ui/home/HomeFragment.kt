package com.catelt.mome.ui.home


import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagingData
import coil.load
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.databinding.FragmentHomeBinding
import com.catelt.mome.ui.bottomsheet.MediaDetailsBottomSheet
import com.catelt.mome.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {
    override val viewModel: HomeViewModel by viewModels()
    override var isFullScreen = true
    private var genreId: Int? = null

    private val onMovieClicked = { movieId: Int ->
        MediaDetailsBottomSheet.newInstance(movieId, viewModel.getIsMovie())
            .show(requireActivity().supportFragmentManager, movieId.toString())
    }

    private var imageParser: ImageUrlParser? = null

    override fun setUpArgument(bundle: Bundle) {
        bundle.apply {
            genreId = getInt(BUNDLE_ID_GENRE)
        }
    }

    override fun setUpViews() {
        binding.apply {
            genreId?.let {
                sendData(it)
            }

            layoutHeaderHome.btnPlay.setEnable(viewModel.uiState.value.ophim?.status == true)

            layoutHeaderHome.btnList.apply {
                isHome = true
                setOnClickListener {
                    viewModel.apply {
                        if (isExisted) {
                            onRemoveClick(trailerMedia[positionNowPlaying])
                        } else {
                            onAddMediaClick(trailerMedia[positionNowPlaying])
                        }
                        setUI(!isExisted)
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun setUpViewModel() {
        viewModel.apply {
            if (trailerMedia.isNotEmpty()) {
                lifecycleScope.launch {
                    checkMediaInMyList(
                        trailerMedia[positionNowPlaying]
                    )
                }
            }

            toastMessage.observe(viewLifecycleOwner) {
                if (it.isNotBlank()) {
                    toast(it)
                    toastMessage.postValue("")
                }
            }

            lifecycleScope.launch {
                imageUrlParser.collectLatest { imageParser ->
                    this@HomeFragment.imageParser = imageParser
                    launch {
                        media.collectLatest { presentable ->
                            if (presentable.id != 0) {
                                setupUIMoviePlaying(presentable)
                            }
                        }
                    }


                    launch {
                        uiState.collectLatest { homeUIState ->
                            if (view != null) {
                                launch {
                                    homeUIState.isMyList.let {
                                        binding.layoutHeaderHome.btnList.setUI(it)
                                    }
                                }

                                when (homeUIState.homeState) {
                                    is HomeState.MovieData -> {
                                        homeUIState.homeState.moviesState.apply {
                                            nowPlaying.flowOn(Dispatchers.IO).onEach { list ->
                                                val adapter = HorizontalAdapter()

                                                adapter.addLoadStateListener {
                                                    adapter.snapshot().items.let { items ->
                                                        if (items.isNotEmpty()) {
                                                            trailerMedia = items
                                                            randomMedia(false)
                                                        }
                                                    }
                                                }
                                                adapter.submitData(list)

                                            }.launchIn(lifecycleScope)

                                            trending.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listTrending.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list,
                                                        getString(R.string.title_trending)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)

                                            topRated.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listUpCome.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list,
                                                        getString(R.string.title_top_rate)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)

                                            popular.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listPopular.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list as PagingData<Presentable>,
                                                        getString(R.string.title_popular)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)

                                            discover.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listExplore.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list,
                                                        getString(R.string.title_explore)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)
                                        }
                                    }
                                    is HomeState.TvShowData -> {
                                        homeUIState.homeState.tvShowsState.apply {
                                            onTheAir.flowOn(Dispatchers.IO).onEach { list ->
                                                val adapter = HorizontalAdapter()

                                                adapter.addLoadStateListener {
                                                    adapter.snapshot().items.let { items ->
                                                        if (items.isNotEmpty()) {
                                                            trailerMedia = items
                                                            randomMedia(false)
                                                        }
                                                    }
                                                }
                                                adapter.submitData(list)

                                            }.launchIn(lifecycleScope)

                                            trending.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listTrending.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list,
                                                        getString(R.string.title_trending_tv_show)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)

                                            topRated.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listUpCome.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list,
                                                        getString(R.string.title_top_rate_tv_show)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)

                                            airingToday.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listPopular.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list,
                                                        getString(R.string.title_airing_today_tv_show)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)

                                            discover.flowOn(Dispatchers.IO).onEach { list ->
                                                binding.listExplore.apply {
                                                    init(
                                                        imageParser,
                                                        viewLifecycleOwner,
                                                        onMovieClicked,
                                                        list,
                                                        getString(R.string.title_explore_tv_show)
                                                    )
                                                    visibility = setVisionView(true)
                                                }
                                            }.launchIn(lifecycleScope)
                                        }
                                    }
                                }

                                binding.layoutHeaderHome.btnPlay.setEnable(false)

                                homeUIState.ophim?.let { data ->
                                    binding.layoutHeaderHome.btnPlay.apply {
                                        setEnable(data.status)
                                        setOnClickListener {
                                            setOnClickPlayVideo()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setOnClickPlayVideo(position: Int = 0) {
        val host = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?

        viewModel.uiState.value.apply {
            ophim?.apply {
                host?.navController?.navigate(
                    R.id.videoPlayerFragment,
                    bundleOf(
                        BUNDLE_TITLE_MEDIA to if (viewModel.getIsMovie()) viewModel.media.value.title else null,
                        BUNDLE_ID_MEDIA to viewModel.media.value.id,
                        BUNDLE_SLUG_MEDIA to movie.slug,
                        BUNDLE_CURRENT_EPISODE to position,
                    )
                )
            }
        }
    }

    private fun setupUIMoviePlaying(presentable: Presentable) {
        binding.layoutHeaderHome.apply {
            viewModel.imageUrlParser.value?.getImageUrl(
                presentable.posterPath,
                ImageUrlParser.ImageType.Poster
            ).let { url ->
                imgHomeCover.load(url)
            }

            imgHomeCover.setOnClickListener {
                onMovieClicked(presentable.id)
            }
            btnInfo.setOnClickListener {
                onMovieClicked(presentable.id)
            }
        }
    }

    private fun sendData(genreId: Int) {
        requireActivity().supportFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_ID_GENRE_HOME to genreId))
    }
}