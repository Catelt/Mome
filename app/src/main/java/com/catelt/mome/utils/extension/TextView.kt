package com.catelt.mome.utils.extension

import android.widget.TextView
import com.catelt.mome.R


fun TextView.setAgeTitle(isAdult: Boolean){
    if (isAdult) {
        this.text = context.getString(R.string.text_18_plus)
    } else {
        this.text = context.getString(R.string.text_13_plus)
    }
}