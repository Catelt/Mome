package com.catelt.mome.ui.components

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.catelt.mome.databinding.ViewButtonMyListBinding

class MyListButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding = ViewButtonMyListBinding.inflate(LayoutInflater.from(context), this, true)
    var isExisted: Boolean = true
    var isHome: Boolean = false

    init {
        setUI()
    }

    fun setUI(isExisted: Boolean = false) {
        binding.apply {
            if (isExisted) {
                imgAdd.visibility = View.GONE
                imgTick.visibility = View.VISIBLE
            } else {
                imgAdd.visibility = View.VISIBLE
                imgTick.visibility = View.GONE
            }
            if (isHome) txtMyList.typeface = Typeface.DEFAULT_BOLD
        }
        this.isExisted = isExisted
    }
}