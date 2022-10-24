package com.catelt.mome.data.model.account

import com.catelt.mome.data.model.MediaType
import com.catelt.mome.data.model.Presentable

data class Media(
    override val id: Int = 0,
    override val title: String = "",
    override val posterPath: String? = null,
    val type: MediaType? = null
): Presentable