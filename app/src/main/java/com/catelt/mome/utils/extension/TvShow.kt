package com.catelt.mome.utils.extension

import com.catelt.mome.data.model.tvshow.TvShowDetails
import java.util.*

fun TvShowDetails.getCalendarRelease(): Calendar?{
    return this.firstAirDate?.let {
        val calendar = Calendar.getInstance()
        calendar.time = it
        calendar
    }
}