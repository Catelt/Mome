package com.catelt.mome.data.remote.api

import kotlin.time.Duration.Companion.seconds

object ApiParams {
    const val secureBaseUrl = "https://api.themoviedb.org/3/"
    const val ophimUrl = "https://ophim1.com/"

    //10 MB cache
    const val cacheSize = (10 * 1024 * 1024).toLong()

    object Timeouts {
        val connect = 10.seconds
        val write = 10.seconds
        val read = 10.seconds
    }
}