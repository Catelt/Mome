package com.catelt.mome.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.catelt.mome.R
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.databinding.BottomSheetMediaDetailsBinding
import com.catelt.mome.utils.*
import com.catelt.mome.utils.extension.getCalendarRelease
import com.catelt.mome.utils.extension.getRunTime
import com.catelt.mome.utils.extension.setAgeTitle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MediaDetailsBottomSheet(
    private val movieId: Int,
    private val isMovie: Boolean,
) : BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetMediaDetailsBinding
    private val viewModel: MediaDetailsBottomViewModel by viewModels()
    private var isExisted = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetMediaDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObserve()
        binding.shimmerLayout.startShimmer()
        setupUI()
    }

    private fun setupObserve() {
        viewModel.apply {
            setMediaId(movieId)
            setIsMovie(isMovie)


            toastMessage.observe(viewLifecycleOwner){
                if (it.isNotBlank()){
                    toast(it)
                    toastMessage.postValue("")
                }
            }

            lifecycleScope.launch {
                imageUrlParser.collectLatest { imageParser ->
                    uiState.collectLatest { uiState ->
                        uiState.movieDetails?.let {
                            setupUIMovie(it, imageParser)
                        }
                        uiState.tvShowsDetails?.let {
                            setupUITvShow(it, imageParser)
                        }
                        uiState.ophim?.let {
                            binding.btnPlay.setOnClickListener {
                                viewModel.viewModelScope.launch {
                                    setOnClickPlayVideo()
                                    delay(100)
                                    dismiss()
                                }
                            }
                        }
                        binding.btnPlay.isEnabled = uiState.ophim != null

                        isExisted = uiState.isMyList
                        setUIMyList(uiState.isMyList)
                    }
                }
            }
        }
    }

    private fun setupUI(){
        binding.apply {
            btnClose.setOnClickListener {
                dismiss()
            }

            btnDownload.setOnClickListener {
                toast(getString(R.string.message_feature_coming_soon))
            }

            btnShare.setOnClickListener {
                toast(getString(R.string.message_feature_coming_soon))
            }

            btnDetail.setOnClickListener {
                mainContainer.callOnClick()
            }
        }
    }

    private fun setupUIMovie(data: MovieDetails, imageUrlParser: ImageUrlParser?) {
        binding.apply {
            shimmerLayout.visibility = View.GONE

            txtTitle.text = data.title
            txtOverview.text = data.overview
            txtYear.text = data.getCalendarRelease()?.get(Calendar.YEAR).toString()
            txtAge.setAgeTitle(data.adult)
            txtRuntime.text = getString(R.string.text_run_time, data.getRunTime())
            imgPoster.load(
                imageUrlParser?.getImageUrl(
                    data.posterPath,
                    ImageUrlParser.ImageType.Poster
                )
            )

            btnList.apply {
                setOnClickListener {
                    if (isExisted) {
                        viewModel.onRemoveClick(data)
                    } else {
                        viewModel.onAddMediaClick(data, true)
                    }
                    setUIMyList(!isExisted)
                }
            }

            mainContainer.setOnClickListener {
                dismiss()
                findNavController().navigate(
                    R.id.detailMovieFragment,
                    bundleOf(BUNDLE_ID_MEDIA to data.id)
                )
            }

        }
    }

    private fun setupUITvShow(data: TvShowDetails, imageUrlParser: ImageUrlParser?) {
        binding.apply {
            shimmerLayout.visibility = View.GONE

            txtTitle.text = data.title
            txtOverview.text = data.overview
            txtYear.text = data.getCalendarRelease()?.get(Calendar.YEAR).toString()
            txtAge.setAgeTitle(data.adult ?: false)
            if (data.numberOfSeasons > 1) {
                txtRuntime.text = getString(R.string.text_run_time_season, data.numberOfSeasons)
            } else {
                txtRuntime.text = getString(R.string.text_run_time_episodes, data.numberOfEpisodes)
            }
            imgPoster.load(
                imageUrlParser?.getImageUrl(
                    data.posterPath,
                    ImageUrlParser.ImageType.Poster
                )
            )

            btnList.apply {
                setOnClickListener {
                    if (isExisted) {
                        viewModel.onRemoveClick(data)
                    } else {
                        viewModel.onAddMediaClick(data, false)
                    }
                    setUIMyList(!isExisted)
                }
            }

            mainContainer.setOnClickListener {
                dismiss()
                findNavController().navigate(
                    R.id.detailTvShowFragment,
                    bundleOf(BUNDLE_ID_MEDIA to data.id)
                )
            }
        }
    }

    private fun setUIMyList(isExisted: Boolean = false) {
        binding.apply {
            if (isExisted) {
                imgAdd.visibility = View.GONE
                imgTick.visibility = View.VISIBLE
            } else {
                imgAdd.visibility = View.VISIBLE
                imgTick.visibility = View.GONE
            }
        }
        this.isExisted = isExisted
    }

    private fun setOnClickPlayVideo(position: Int = 0) {
        viewModel.uiState.value.apply {
            ophim?.apply {
                findNavController().navigate(
                    R.id.videoPlayerFragment,
                    bundleOf(
                        BUNDLE_TITLE_MEDIA to if (isMovie) movieDetails?.title else null,
                        BUNDLE_ID_MEDIA to movieId,
                        BUNDLE_SLUG_MEDIA to ophim.slug,
                        BUNDLE_CURRENT_EPISODE to position,
                    )
                )
            }
        }
    }

    private var toast : Toast? = null
    fun toast(message: String) {
        if (toast != null){
            toast?.cancel()
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    companion object {
        fun newInstance(
            movieId: Int,
            isMovie: Boolean
        ): MediaDetailsBottomSheet {
            return MediaDetailsBottomSheet(movieId, isMovie)
        }
    }
}