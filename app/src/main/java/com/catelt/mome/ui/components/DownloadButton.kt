package com.catelt.mome.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.catelt.mome.databinding.ViewButtonDownloadBinding

class DownloadButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding = ViewButtonDownloadBinding.inflate(LayoutInflater.from(context), this, true)

}