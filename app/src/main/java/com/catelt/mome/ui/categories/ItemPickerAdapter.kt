package com.catelt.mome.ui.categories

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.catelt.mome.R
import com.catelt.mome.core.BaseAdapter
import com.catelt.mome.data.model.Genre
import com.catelt.mome.databinding.ItemPickerOptionBinding

class ItemPickerAdapter : BaseAdapter<Genre>() {

    var onClicked: ((Int) -> Unit)? = null
    var select: Int = 0
    private lateinit var context: Context

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        context = parent.context
        return ItemPickerOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bind(binding: ViewBinding, position: Int) {
        val item = getItem(position)
        (binding as ItemPickerOptionBinding).apply {
            txtOption.text = item.name

            txtOption.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey_light_1))
            txtOption.typeface = Typeface.DEFAULT
            select.let {
                if (it == item.id){
                    txtOption.setTextColor(Color.WHITE)
                    txtOption.typeface = Typeface.DEFAULT_BOLD
                }
            }

            txtOption.setOnClickListener {
                select = item.id
                onClicked?.invoke(item.id)
            }
        }
    }
}