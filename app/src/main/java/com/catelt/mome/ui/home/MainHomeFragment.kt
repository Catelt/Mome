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
import com.catelt.mome.utils.BUNDLE_ID_GENRE
import com.catelt.mome.utils.BUNDLE_IS_MOVIE
import com.catelt.mome.utils.REQUEST_KEY
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenerDataFragment()
    }
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
                findNavController().navigate(R.id.itemPickerFragment)
            }

            txtTvShows.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    onClickTvShow()
                }
                false
            }

            if (viewModel.getCountStack() > 0){
                if (viewModel.getIsMovie()) {
                    txtMovies.callOnClick()
                } else {
                    txtTvShows.callOnClick()
                }
            }

            btnBack.setOnClickListener {
                viewModel.setIsMovie(!viewModel.getIsMovie())
                if (viewModel.getCountStack() < 1){
                    setupAppBar(false)
                }
                else{
                    host.navController.navigateUp()
                    viewModel.popStack()
                }

                subTitleAppBar.transitionToStart()
                nestScrollView.scrollTo(0, 0)
            }

            btnProfile.setOnClickListener {
                findNavController().navigate(R.id.profileFragment)
            }

            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.searchFragment)
            }
        }
    }

    override fun setUpViewModel() {
        viewModel.apply {
            countStack.observe(viewLifecycleOwner){
                if (it > 0){
                    setupAppBar(true)
                }
                else{
                    setupAppBar(false)
                }
            }
            lifecycleScope.launch {
                launch {
                    category.collectLatest {
                        if (it != 0){
                            binding.txtContentCategories.text = getGenre(it)
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

    private fun onClickMovie() {
        binding.apply {
            if (!isShowTitleAppBar) {
                viewModel.setIsMovie(true)
                viewModel.addStack()
                txtTitleAppBar.text = getString(R.string.movies)
                nestScrollView.scrollTo(0, 0)
                host.navController.navigate(R.id.action_homeFragment_self, bundleOf(
                    BUNDLE_IS_MOVIE to true
                ))
            }
        }
    }

    private fun onClickTvShow() {
        binding.apply {
            if (!isShowTitleAppBar) {
                viewModel.setIsMovie(false)
                viewModel.addStack()
                txtTitleAppBar.text = getString(R.string.tv_shows)
                nestScrollView.scrollTo(0, 0)
                host.navController.navigate(
                    R.id.action_homeFragment_self, bundleOf(
                        BUNDLE_IS_MOVIE to false
                    )
                )
            }
        }
    }

    private fun listenerDataFragment(){
        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            val result = bundle.getInt(BUNDLE_ID_GENRE)
            viewModel.setCategory(result)
        }
    }

    override fun onPause() {
        super.onPause()
        saveStateMotion = binding.root.transitionState
    }

}