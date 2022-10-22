package com.catelt.mome.ui.upcoming

import androidx.paging.PagingData
import com.catelt.mome.data.model.movie.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


data class UpcomingUIState(
    val newHotState: Upcoming,
){
    companion object {
        val default: UpcomingUIState = UpcomingUIState(
            newHotState = Upcoming.default,
        )
    }
}

data class Upcoming(
    val upcoming: Flow<PagingData<Movie>>,
) {
    companion object {
        val default: Upcoming = Upcoming(
            upcoming = emptyFlow()
        )
    }
}