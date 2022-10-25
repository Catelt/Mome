package com.catelt.mome.ui.detail.movie

import androidx.paging.PagingData
import com.catelt.mome.data.model.Credits
import com.catelt.mome.data.model.Image
import com.catelt.mome.data.model.Video
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.data.model.movie.MovieCollection
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.ophim.OphimResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class MovieDetailsScreenUIState(
    val movieDetails: MovieDetails?,
    val associatedMovies: AssociatedMovies,
    val associatedContent: AssociatedContent,
    val error: String?
) {
    companion object {
        fun getDefault(): MovieDetailsScreenUIState {
            return MovieDetailsScreenUIState(
                movieDetails = null,
                associatedMovies = AssociatedMovies.default,
                associatedContent = AssociatedContent.default,
                error = null
            )
        }
    }
}

data class AssociatedMovies(
    val collection: MovieCollection?,
    val similar: Flow<PagingData<Movie>>,
    val recommendations: Flow<PagingData<Movie>>,
    val directorMovies: DirectorMovies
) {
    companion object {
        val default: AssociatedMovies = AssociatedMovies(
            collection = null,
            similar = emptyFlow(),
            recommendations = emptyFlow(),
            directorMovies = DirectorMovies.default
        )
    }
}

data class DirectorMovies(
    val directorName: String,
    val movies: Flow<PagingData<Movie>>
) {
    companion object {
        val default: DirectorMovies = DirectorMovies(
            directorName = "",
            movies = emptyFlow()
        )
    }
}

data class AssociatedContent(
    val backdrops: List<Image>,
    val videos: List<Video>?,
    val credits: Credits?,
    val ophim: OphimResponse?,
) {
    companion object {
        val default: AssociatedContent = AssociatedContent(
            backdrops = emptyList(),
            videos = null,
            credits = null,
            ophim = null
        )
    }
}