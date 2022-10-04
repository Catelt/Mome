package com.catelt.mome.utils.extension

import com.catelt.mome.data.model.movie.MovieDetails
import java.util.*


fun MovieDetails.getRunTime(): String?{
    val time = this.runtime
    time?.let {
        return "${time/60}h ${time%60}m"
    }
    return null
}

fun MovieDetails.getCalendarRelease(): Calendar?{
    return this.releaseDate?.let {
        val calendar = Calendar.getInstance()
        calendar.time = it
        calendar
    }
}

