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
import com.catelt.mome.databinding.BottomSheetMediaDetailsBinding
import com.catelt.mome.utils.BUNDLE_ID_MEDIA
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.getCalendarRelease
import com.catelt.mome.utils.extension.getRunTime
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MediaDetailsBottomSheet(
    private val movieId: Int,
) : BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetMediaDetailsBinding
    private val viewModel: MediaDetailsBottomViewModel by viewModels()

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
    }

    private fun setupObserve(){
        viewModel.apply {
            lifecycleScope.launch{
                imageUrlParser.collectLatest { imageParser ->
                    movieDetails.observe(viewLifecycleOwner) {
                        if (it != null) {
                            setupUI(it,imageParser)
                        }
                    }
                    viewModel.getMovieDetail(movieId)
                }
            }
        }
    }

    private fun setupUI(data: MovieDetails, imageUrlParser: ImageUrlParser?) {
        binding.apply {
            btnClose.setOnClickListener {
                dismiss()
            }

            txtTitle.text = data.title
            txtOverview.text = data.overview
            txtYear.text = data.getCalendarRelease()?.get(Calendar.YEAR).toString()
            txtRuntime.text = getString(R.string.text_run_time,data.getRunTime())
            imgPoster.load(
                imageUrlParser?.getImageUrl(
                    data.posterPath,
                    ImageUrlParser.ImageType.Poster
                )
            )
            btnDetail.setOnClickListener {
                dismiss()
                findNavController().navigate(
                    R.id.detailMovieFragment,
                    bundleOf(BUNDLE_ID_MEDIA to data.id)
                )
            }
        }
    }

    companion object {
        fun newInstance(
            movieId: Int,
        ): MediaDetailsBottomSheet {
            return MediaDetailsBottomSheet(movieId)
        }
    }
}