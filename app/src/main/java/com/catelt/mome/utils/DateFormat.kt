package com.catelt.mome.utils

import java.time.format.DateTimeFormatter


object DateFormat {
    val month = DateTimeFormatter.ofPattern("MMMM")
    val monthAverage = DateTimeFormatter.ofPattern("MMM")
    val upcoming = DateTimeFormatter.ofPattern("MMMM dd")
    val default = DateTimeFormatter.ofPattern("yyyy-MM-dd")
}