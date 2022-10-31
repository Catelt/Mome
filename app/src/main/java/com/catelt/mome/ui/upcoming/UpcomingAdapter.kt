package com.catelt.mome.ui.upcoming

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.catelt.mome.R
import com.catelt.mome.core.BasePagingAdapter
import com.catelt.mome.data.model.Genre
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.databinding.ItemComingMovieBinding
import com.catelt.mome.utils.DateFormat
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.extension.loadDefault
import java.time.LocalDate

class UpcomingAdapter : BasePagingAdapter<Movie>() {
    var imageUrlParser: ImageUrlParser? = null
    var onMovieClicked: ((Int,String) -> Unit)? = null
    var genres: List<Genre>? = null


    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemComingMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val movie = getItem(position)

        (binding as ItemComingMovieBinding).apply {
            movie?.let {
                txtTitle.text = movie.title
                txtOverview.text = movie.overview
                var genreStr = ""
                genres?.let { list ->
                    for (genre in movie.genreIds) {
                        for (genre2 in list) {
                            if (genre == genre2.id) {
                                genreStr += if (genre == movie.genreIds.last()) {
                                    genre2.name
                                } else {
                                    "${genre2.name} • "
                                }
                            }
                        }
                    }
                }
                txtGenres.text = genreStr

                val localDate = LocalDate.parse(movie.releaseDate, DateFormat.default)

                txtArrivalDate.text = root.context.getString(
                    R.string.text_arrival_date, localDate.format(DateFormat.upcoming)
                )

                txtMonth.text = localDate.format(DateFormat.monthAverage)
                txtDay.text = localDate.dayOfMonth.toString()

                imgBackdrop.loadDefault(
                    imageUrlParser?.getImageUrl(
                        movie.backdropPath,
                        ImageUrlParser.ImageType.Backdrop
                    )
                )
            }
        }
    }

    override fun setOnClickItem(position: Int) {
        val movie = getItem(position)

        val localDate = LocalDate.parse(movie?.releaseDate, DateFormat.default)

        val textArrivalDate = "Coming ${localDate.format(DateFormat.upcoming)}"

        movie?.let {
            onMovieClicked?.invoke(movie.id,textArrivalDate)
        }
    }
}