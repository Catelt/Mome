package com.catelt.mome.ui.home


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import kotlin.random.Random

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {
    override val viewModel: HomeViewModel by viewModels()
    private var saveStateMotion: Bundle? = null

    private var positionNowPlaying = Random.nextInt(0, 15)
    private val onMovieClicked = { movieId: Int ->
        MediaDetailsBottomSheet.newInstance(movieId, viewModel.getIsMovie())
            .show(requireActivity().supportFragmentManager, movieId.toString())
    }

    private var trailerMedia: List<Presentable> = emptyList()
    private var imageParser: ImageUrlParser? = null
    private var isShowTitleAppBar: Boolean = false

    init {
        isFullScreen = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpViews() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isShowTitleAppBar) {
                        binding.btnBack.callOnClick()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })

        binding.apply {
            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }

            layoutHeaderHome.btnPlay.setEnable(viewModel.uiState.value.ophim?.status == true)
            txtMovies.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    onClickMovie()
                }
                false
            }

            txtTvShows.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    onClickTvShow()
                }
                false
            }


            if (isShowTitleAppBar) {
                isShowTitleAppBar = false
                if (viewModel.getIsMovie()) {
                    txtMovies.callOnClick()
                    onClickMovie()
                } else {
                    txtTvShows.callOnClick()
                    onClickTvShow()
                }
            }

            layoutHeaderHome.btnList.apply {
                isHome = true
                setOnClickListener {
                    if (isExisted) {
                        viewModel.onRemoveClick(trailerMedia[positionNowPlaying])
                    } else {
                        viewModel.onAddMediaClick(trailerMedia[positionNowPlaying])
                    }
                    setUI(!isExisted)
                }
            }

            btnBack.setOnClickListener {
                viewModel.setIsMovie(!viewModel.getIsMovie())
                setupAppBar(false)
                subTitleAppBar.transitionToStart()
                nestScrollView.scrollTo(0, 0)
            }

            btnProfile.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }

            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }

            txtCategories.setOnClickListener {
                toast(getString(R.string.message_feature_coming_soon))
            }

            txtAllCategories.setOnClickListener {
                toast(getString(R.string.message_feature_coming_soon))
            }
        }
    }

    private fun setupAppBar(isShow: Boolean) {
        binding.apply {
            isShowTitleAppBar = isShow
            txtTitleAppBar.isVisible = isShow
            btnBack.isVisible = isShow
            imgIconApp.isVisible = !isShow
        }
    }

    private fun onClickMovie() {
        binding.apply {
            if (!isShowTitleAppBar) {
                viewModel.setIsMovie(true)
                setupAppBar(true)
                txtTitleAppBar.text = getString(R.string.movies)
                nestScrollView.scrollTo(0, 0)
                randomMedia()
            }
        }
    }

    private fun onClickTvShow() {
        binding.apply {
            if (!isShowTitleAppBar) {
                viewModel.setIsMovie(false)
                setupAppBar(true)
                txtTitleAppBar.text = getString(R.string.tv_shows)
                nestScrollView.scrollTo(0, 0)
                randomMedia()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun setUpViewModel() {
        viewModel.apply {
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
        viewModel.uiState.value.apply {
            ophim?.apply {
                findNavController().navigate(
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

    override fun onPause() {
        super.onPause()
        saveStateMotion = binding.root.transitionState
    }

    override fun onResume() {
        super.onResume()
        saveStateMotion?.let {
            binding.root.transitionState = saveStateMotion
        }
    }

    private fun randomMedia(isRandom: Boolean = true) {
        if (isRandom) {
            positionNowPlaying = Random.nextInt(0, 15)
        }
        if (trailerMedia.isNotEmpty()) {
            viewModel.setCurrentMedia(trailerMedia[positionNowPlaying])
        }
    }
}