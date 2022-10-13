package com.catelt.mome.ui.components

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.catelt.mome.databinding.ViewEditTextAuthBinding
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE

class AuthEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding = ViewEditTextAuthBinding.inflate(LayoutInflater.from(context), this, true)
    var handleTextOnChange : ((String) -> Unit)? = null

    fun setupView(
        txtHint: String = "",
        isPassword: Boolean = false,
        inputType: Int = InputType.TYPE_CLASS_TEXT
    ){
        binding.apply {
            if (isPassword){
                txtLayout.endIconMode = END_ICON_PASSWORD_TOGGLE
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            else{
                editText.inputType = inputType
            }

            txtLayout.hint = txtHint


            editText.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    handleTextOnChange?.invoke((p0?: "").toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })
        }
    }

    fun setError(string: String?){
        binding.apply {
            layoutError.visibility = if(!string.isNullOrBlank()) View.VISIBLE else View.GONE
            txtError.text = string
        }
    }
}