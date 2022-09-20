package com.catelt.mome.ui.components

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.catelt.mome.databinding.ViewSearchBarBinding

class SearchEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private val binding = ViewSearchBarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.apply {
            editTextSearch.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0.isNullOrBlank()){
                        setButtonClear(false)
                    }
                    else{
                        setButtonClear(true)
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })

            btnClear.setOnClickListener {
                editTextSearch.setText("")
            }
        }
    }

    fun setButtonClear(isClear: Boolean){
        binding.apply {
            btnClear.isVisible = isClear
            btnMic.isVisible = !isClear
        }
    }
}