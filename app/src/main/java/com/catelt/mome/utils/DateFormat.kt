package com.catelt.mome.utils

import java.time.format.DateTimeFormatter
import java.util.*


object DateFormat {
    private val localDefault = Locale.ENGLISH
    val month = DateTimeFormatter.ofPattern("MMMM",localDefault)
    val monthAverage = DateTimeFormatter.ofPattern("MMM",localDefault)
    val upcoming = DateTimeFormatter.ofPattern("MMMM dd",localDefault)
    val default = DateTimeFormatter.ofPattern("yyyy-MM-dd",localDefault)
}