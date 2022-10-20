package com.catelt.mome.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.catelt.mome.R
import com.catelt.mome.databinding.ViewButtonPlayBinding

class PlayButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private val binding = ViewButtonPlayBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setDefault()
    }

    fun setLoading(){
        binding.apply {
            layoutLoading.visibility = View.VISIBLE
            layoutButton.visibility = View.GONE
        }
    }

    fun setDefault(){
        binding.apply {
            layoutLoading.visibility = View.GONE
            layoutButton.visibility = View.VISIBLE
        }
    }

    fun setEnable(isEnable: Boolean){
        binding.apply {
            root.isEnabled = isEnable
            if (isEnable){
                mainContainer.background = ContextCompat.getDrawable(context, R.color.white)
            }
            else{
                mainContainer.background = ContextCompat.getDrawable(context, R.color.grey_light_1)
                setDefault()
            }
        }
    }
}