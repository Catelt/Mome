package com.catelt.mome.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.catelt.mome.R
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.databinding.BottomSheetMediaDetailsBinding
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.getCalendarRelease
import com.catelt.mome.utils.extension.getRunTime
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class MediaDetailsBottomSheet(
    private val data: MovieDetails,
    private val imageUrlParser: ImageUrlParser?,
) : BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetMediaDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetMediaDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        }
    }

    companion object {
        fun newInstance(
            data: MovieDetails,
            imageUrlParser: ImageUrlParser?
        ): MediaDetailsBottomSheet {
            return MediaDetailsBottomSheet(data, imageUrlParser)
        }
    }
}