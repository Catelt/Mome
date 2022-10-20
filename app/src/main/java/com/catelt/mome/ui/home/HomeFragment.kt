package com.catelt.mome.ui.home


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
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
import com.catelt.mome.utils.ImageUrlParser
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

    private var positionNowPlaying = Random.nextInt(0, 10)
    private val onMovieClicked = { movieId: Int ->
        MediaDetailsBottomSheet.newInstance(movieId,viewModel.getIsMovie())
            .show(requireActivity().supportFragmentManager, movieId.toString())
    }

    private var trailerMedia : List<Presentable> = emptyList()
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
                    }else{
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })

        binding.apply {
            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
            layoutHeaderHome.btnPlay.setOnClickListener {
                findNavController().navigate(R.id.detailTvShowFragment)
            }
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


            if (isShowTitleAppBar){
                isShowTitleAppBar = false
                if (viewModel.getIsMovie()){
                    txtMovies.callOnClick()
                    onClickMovie()
                }
                else{
                    txtTvShows.callOnClick()
                    onClickTvShow()
                }
            }

            btnBack.setOnClickListener {
                viewModel.setIsMovie(!viewModel.getIsMovie())
                setupAppBar(false)
                subTitleAppBar.transitionToStart()
            }

            btnProfile.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }

            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
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

    private fun onClickMovie(){
        binding.apply {
            if (!isShowTitleAppBar){
                viewModel.setIsMovie(true)
                setupAppBar(true)
                txtTitleAppBar.text = getString(R.string.movies)
                nestScrollView.scrollTo(0,0)
                randomMedia()
                setupUIMoviePlaying(trailerMedia[positionNowPlaying])
            }
        }
    }

    private fun onClickTvShow(){
        binding.apply {
            if (!isShowTitleAppBar){
                viewModel.setIsMovie(false)
                setupAppBar(true)
                txtTitleAppBar.text = getString(R.string.tv_shows)
                nestScrollView.scrollTo(0,0)
                randomMedia()
                setupUIMoviePlaying(trailerMedia[positionNowPlaying])
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun setUpViewModel() {
        viewModel.apply {
            lifecycleScope.launch {
                imageUrlParser.collectLatest { imageParser ->
                    this@HomeFragment.imageParser = imageParser

                    uiState.collectLatest {
                        when (it.homeState) {
                            is HomeState.MovieData -> {
                                it.homeState.moviesState.apply {
                                    nowPlaying.flowOn(Dispatchers.IO).onEach { list ->
                                        val adapter = HorizontalAdapter()

                                        adapter.addLoadStateListener {
                                            adapter.snapshot().items.let { items ->
                                                if (items.isNotEmpty()) {
                                                    trailerMedia = items
                                                    setupUIMoviePlaying(items[positionNowPlaying])
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
                                it.homeState.tvShowsState.apply {
                                    onTheAir.flowOn(Dispatchers.IO).onEach { list ->
                                        val adapter = HorizontalAdapter()

                                        adapter.addLoadStateListener {
                                            adapter.snapshot().items.let { items ->
                                                if (items.isNotEmpty()) {
                                                    trailerMedia = items
                                                    setupUIMoviePlaying(items[positionNowPlaying])
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
                    }
                }
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
        saveStateMotion =  binding.root.transitionState
    }

    override fun onResume() {
        super.onResume()
        saveStateMotion?.let {
            binding.root.transitionState = saveStateMotion
        }
    }

    private fun randomMedia(){
        positionNowPlaying = Random.nextInt(0, 10)
    }
}