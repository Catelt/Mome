package com.catelt.mome.data.model

import com.catelt.mome.utils.formatted
import java.util.*

data class DateParam(private val date: Date) {
    override fun toString(): String {
        return date.formatted("yyyy-MM-dd")
    }
}
