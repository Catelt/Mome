package com.catelt.mome.ui.videoplayer

import android.view.ScaleGestureDetector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

class CustomOnScaleGestureListener(
    private val player: PlayerView
) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    private var scaleFactor = 0f

    override fun onScale(
        detector: ScaleGestureDetector
    ): Boolean {
        scaleFactor = detector.scaleFactor
        return true
    }

    override fun onScaleBegin(
        detector: ScaleGestureDetector
    ) : Boolean{
        return true
    }
    override fun onScaleEnd(detector: ScaleGestureDetector) {
        if (scaleFactor > 1) {
            player.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            player.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }
}