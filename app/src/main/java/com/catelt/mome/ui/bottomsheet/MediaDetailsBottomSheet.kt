package com.catelt.mome.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.catelt.mome.R
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.databinding.BottomSheetMediaDetailsBinding
import com.catelt.mome.utils.BUNDLE_ID_MEDIA
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.getCalendarRelease
import com.catelt.mome.utils.extension.getRunTime
import com.catelt.mome.utils.extension.setAgeTitle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
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
    }

    private fun setupObserve() {
        viewModel.apply {
            lifecycleScope.launch {
                launch {
                    isMyList.collectLatest {
                        setUI(it)
                    }
                }

                launch {
                    imageUrlParser.collectLatest { imageParser ->
                        movieDetails.observe(viewLifecycleOwner) {
                            if (it != null) {
                                setupUIMovie(it, imageParser)
                                launch {
                                    checkMediaInMyList(it)
                                }
                            }
                        }
                        tvShowsDetails.observe(viewLifecycleOwner){
                            if (it != null) {
                                setupUITvShow(it, imageParser)
                                launch {
                                    checkMediaInMyList(it)
                                }
                            }
                        }
                        if (isMovie){
                            viewModel.getMovieDetail(movieId)
                        }
                        else{
                            viewModel.getTvshowDetail(movieId)
                        }
                    }
                }
            }
        }
    }

    private fun setupUIMovie(data: MovieDetails, imageUrlParser: ImageUrlParser?) {
        binding.apply {
            shimmerLayout.visibility = View.GONE
            btnClose.setOnClickListener {
                dismiss()
            }

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
                    if (isExisted){
                        viewModel.onRemoveClick(data)
                    }
                    else{
                        viewModel.onAddMediaClick(data,true)
                    }
                    setUI(!isExisted)
                }
            }

            mainContainer.setOnClickListener {
                dismiss()
                findNavController().navigate(
                    R.id.detailMovieFragment,
                    bundleOf(BUNDLE_ID_MEDIA to data.id)
                )
            }
            btnDetail.setOnClickListener {
                mainContainer.callOnClick()
            }
        }
    }

    private fun setupUITvShow(data: TvShowDetails, imageUrlParser: ImageUrlParser?) {
        binding.apply {
            shimmerLayout.visibility = View.GONE
            btnClose.setOnClickListener {
                dismiss()
            }

            txtTitle.text = data.title
            txtOverview.text = data.overview
            txtYear.text = data.getCalendarRelease()?.get(Calendar.YEAR).toString()
            txtAge.setAgeTitle(data.adult ?: false)
            if (data.numberOfSeasons > 1) {
                txtRuntime.text = getString(R.string.text_run_time_season,data.numberOfSeasons)
            }
            else{
                txtRuntime.text = getString(R.string.text_run_time_episodes,data.numberOfEpisodes)
            }
            imgPoster.load(
                imageUrlParser?.getImageUrl(
                    data.posterPath,
                    ImageUrlParser.ImageType.Poster
                )
            )

            btnList.apply {
                setOnClickListener {
                    if (isExisted){
                        viewModel.onRemoveClick(data)
                    }
                    else{
                        viewModel.onAddMediaClick(data,false)
                    }
                    setUI(!isExisted)
                }
            }

            mainContainer.setOnClickListener {
                dismiss()
                findNavController().navigate(
                    R.id.detailTvShowFragment,
                    bundleOf(BUNDLE_ID_MEDIA to data.id)
                )
            }
            btnDetail.setOnClickListener {
                mainContainer.callOnClick()
            }
        }
    }

    fun setUI(isExisted: Boolean = false) {
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

    companion object {
        fun newInstance(
            movieId: Int,
            isMovie: Boolean
        ): MediaDetailsBottomSheet {
            return MediaDetailsBottomSheet(movieId,isMovie)
        }
    }
}