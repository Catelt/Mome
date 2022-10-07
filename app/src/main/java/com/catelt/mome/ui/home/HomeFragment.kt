package com.catelt.mome.ui.home


import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
    private var positionNowPlaying = Random.nextInt(0, 10)
    private val onMovieClicked = { movieId: Int ->
        MediaDetailsBottomSheet.newInstance(movieId).show(requireActivity().supportFragmentManager,movieId.toString())
    }

    private var imageParser: ImageUrlParser? = null

    init {
        isFullScreen = true
    }

    override fun setUpViews() {
        binding.apply {
            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
            layoutHeaderHome.btnPlay.setOnClickListener {
                findNavController().navigate(R.id.detailTvShowFragment)
            }

        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            lifecycleScope.launch {
                imageUrlParser.collectLatest { imageParser ->
                    this@HomeFragment.imageParser = imageParser
                    moviesState.collectLatest {
                        it.nowPlaying.flowOn(Dispatchers.IO).onEach { list ->
                            val adapter = HorizontalAdapter()

                            adapter.addLoadStateListener {
                                adapter.snapshot().items.let { items ->
                                    if (items.isNotEmpty()) {
                                        setupUIMoviePlaying(items[positionNowPlaying])
                                    }
                                }
                            }
                            adapter.submitData(list)

                        }.launchIn(lifecycleScope)

                        it.trending.flowOn(Dispatchers.IO).onEach { list ->
                            binding.listTrending.init(
                                imageParser,
                                viewLifecycleOwner,
                                onMovieClicked,
                                list,
                                getString(R.string.title_trending)
                            )
                        }.launchIn(lifecycleScope)

                        it.topRated.flowOn(Dispatchers.IO).onEach { list ->
                            binding.listUpCome.init(
                                imageParser,
                                viewLifecycleOwner,
                                onMovieClicked,
                                list,
                                getString(R.string.title_top_rate)
                            )
                        }.launchIn(lifecycleScope)

                        it.upcoming.flowOn(Dispatchers.IO).onEach { list ->
                            binding.listPopular.init(
                                imageParser,
                                viewLifecycleOwner,
                                onMovieClicked,
                                list,
                                getString(R.string.title_popular)
                            )
                        }.launchIn(lifecycleScope)

                        it.discover.flowOn(Dispatchers.IO).onEach { list ->
                            binding.listExplore.init(
                                imageParser,
                                viewLifecycleOwner,
                                onMovieClicked,
                                list,
                                getString(R.string.title_explore)
                            )
                        }.launchIn(lifecycleScope)
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
}