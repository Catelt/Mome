package com.catelt.mome.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.catelt.mome.R
import com.catelt.mome.databinding.ViewButtonPlayBinding
import com.google.android.material.button.MaterialButton

class PlayButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private val binding = ViewButtonPlayBinding.inflate(LayoutInflater.from(context), this, true)
}