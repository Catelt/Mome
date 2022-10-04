package com.catelt.mome.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.databinding.ItemFeedHorizontalListBinding
import com.catelt.mome.ui.home.HorizontalAdapter
import com.catelt.mome.utils.ImageUrlParser
import kotlinx.coroutines.launch

class HorizontalList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding =
        ItemFeedHorizontalListBinding.inflate(LayoutInflater.from(context), this, true)

    fun init(
        imageUrlParser: ImageUrlParser?,
        lifecycleOwner: LifecycleOwner,
        onMovieClicked: (Int) -> Unit,
        data: PagingData<Presentable>,
        title: String
    ) {
        val adapter = HorizontalAdapter()
        adapter.imageUrlParser = imageUrlParser
        adapter.onMovieClicked = onMovieClicked
        binding.txtTitle.text = title
        binding.recyclerViewHorizontal.adapter = adapter
        lifecycleOwner.lifecycleScope.launch {
            adapter.submitData(data)
        }
    }
}