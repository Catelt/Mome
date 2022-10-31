package com.catelt.mome.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.catelt.mome.R
import com.catelt.mome.core.BaseFragment
import com.catelt.mome.databinding.FragmentMainHomeBinding
import com.catelt.mome.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainHomeFragment : BaseFragment<FragmentMainHomeBinding>(
    FragmentMainHomeBinding::inflate
) {
    override val viewModel: HomeViewModel by activityViewModels()
    override var isFullScreen = true

    private lateinit var host: NavHostFragment
    private var saveStateMotion: Bundle? = null
    private var isShowTitleAppBar: Boolean = true

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpViews() {
        saveStateMotion?.let {
            binding.root.transitionState = saveStateMotion
        }

        host = childFragmentManager.findFragmentById(R.id.nav_home_fragment) as NavHostFragment

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.getCountStack() > 0) {
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

            txtMovies.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    onClickMovie()
                }
                false
            }

            txtCategories.setOnClickListener {
                findNavController().navigate(
                    R.id.itemPickerFragment, bundleOf(
                        BUNDLE_ID_GENRE to viewModel.genreId.value
                    )
                )
            }

            txtAllCategories.setOnClickListener {
                findNavController().navigate(
                    R.id.itemPickerFragment, bundleOf(
                        BUNDLE_ID_GENRE to viewModel.genreId.value
                    )
                )
            }

            txtTvShows.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    onClickTvShow()
                }
                false
            }

            if (viewModel.getCountStack() > 0) {
                if (viewModel.getIsMovie()) {
                    subTitleAppBar.transitionToState(R.id.end2, 0)
                    onClickMovie()
                } else {
                    subTitleAppBar.transitionToState(R.id.end1, 0)
                    onClickTvShow()
                }
            }

            btnBack.setOnClickListener {
                viewModel.popStack()
                host.navController.navigateUp()
                if (viewModel.getCountStack() == 0) {
                    setupAppBar(false)
                    subTitleAppBar.transitionToStart()
                }
                nestScrollView.scrollTo(0, 0)
            }

            btnProfile.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }

            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
            listenerDataFragment()

        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            lifecycleScope.launch {
                launch {
                    countStack.collectLatest {
                        if (it > 0) {
                            setupAppBar(true)
                        } else {
                            setupAppBar(false)
                        }
                    }
                }
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

    private fun onClickMovie(idGenre: Int? = null) {
        binding.apply {
            if (!isShowTitleAppBar || idGenre != null) {
                viewModel.addStack()
                txtTitleAppBar.text = getString(R.string.movies)
                nestScrollView.scrollTo(0, 0)

                host.navController.navigate(
                    R.id.action_homeFragment_self, bundleOf(
                        BUNDLE_IS_MOVIE to true,
                        BUNDLE_ID_GENRE to idGenre
                    )
                )
            }
        }
    }

    private fun onClickTvShow(idGenre: Int? = null) {
        binding.apply {
            if (!isShowTitleAppBar || idGenre != null) {
                viewModel.addStack()
                txtTitleAppBar.text = getString(R.string.tv_shows)
                nestScrollView.scrollTo(0, 0)

                host.navController.navigate(
                    R.id.action_homeFragment_self, bundleOf(
                        BUNDLE_IS_MOVIE to false,
                        BUNDLE_ID_GENRE to idGenre
                    )
                )
            }
        }
    }

    private fun handleCallOnClick(idGenre: Int) {
        if (viewModel.getIsMovie()) {
            binding.subTitleAppBar.transitionToState(R.id.end2, 300)
            onClickMovie(idGenre)
        } else {
            binding.subTitleAppBar.transitionToState(R.id.end1, 300)
            onClickTvShow(idGenre)
        }
    }

    private fun listenerDataFragment() {
        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            val result = bundle.getInt(BUNDLE_ID_GENRE)
            handleCallOnClick(result)
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(REQUEST_KEY,viewLifecycleOwner) { _, bundle ->
            val id = bundle.getInt(BUNDLE_ID_GENRE_HOME)
            val isMovie = bundle.getBoolean(BUNDLE_IS_MOVIE)
            if (id != 0) {
                binding.txtContentCategories.text = viewModel.getGenre(id)
            } else {
                binding.txtContentCategories.text = getString(R.string.all_categories)
            }
            viewModel.setGenreId(id)
            viewModel.setIsMovie(isMovie)
        }
    }

    override fun onPause() {
        super.onPause()
        saveStateMotion = binding.root.transitionState
    }

}