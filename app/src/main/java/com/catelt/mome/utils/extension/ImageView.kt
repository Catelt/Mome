package com.catelt.mome.utils.extension

import android.widget.ImageView
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.catelt.mome.R

fun ImageView.loadDefault(data: Any?){
    this.load(data){
        crossfade(true)
        listener(object : ImageRequest.Listener{
            override fun onError(request: ImageRequest, result: ErrorResult) {
                super.onError(request, result)
                this@loadDefault.setImageResource(R.drawable.background_no_image)
            }
        })
    }
}
