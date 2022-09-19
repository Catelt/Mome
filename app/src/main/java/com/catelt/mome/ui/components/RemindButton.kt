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
import com.catelt.mome.databinding.ViewButtonRemindBinding
import com.google.android.material.button.MaterialButton

class RemindButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private val binding = ViewButtonRemindBinding.inflate(LayoutInflater.from(context), this, true)
    var type: Boolean = true

    init {
        setDefault()
    }

    fun setDefault(isDefault: Boolean = true){
        binding.apply {
            if (isDefault){
                icon.background = ContextCompat.getDrawable(context,R.drawable.ic_baseline_notifications_none)
                txtNameButton.text = context.getString(R.string.remind_me)
            }
            else{
                icon.background = ContextCompat.getDrawable(context,R.drawable.ic_tick)
                txtNameButton.text = context.getString(R.string.reminder_set)
            }
        }
        this.type = isDefault
    }
}