package com.catelt.mome.ui.videoplayer

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.ByteBuffer
import java.security.MessageDigest

private const val MAX_COLUMNS = 8
private const val THUMBNAILS_EACH = 180*1000 // milliseconds

class GlideThumbnailTransformation(position: Long) : BitmapTransformation() {

    private val x: Int
    private val y: Int

    init {
        // Remainder of position on one minute because we just need to know which square of the current miniature
        val square = (position/ THUMBNAILS_EACH).toInt()
        y = square / MAX_COLUMNS
        x = square % MAX_COLUMNS
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width / MAX_COLUMNS
        val height = toTransform.height / MAX_COLUMNS
        return Bitmap.createBitmap(toTransform, x * width, y * height, width, height)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val data: ByteArray = ByteBuffer.allocate(8).putInt(x).putInt(y).array()
        messageDigest.update(data)
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        if (other !is GlideThumbnailTransformation) {
            return false
        }
        return other.x == x && other.y == y
    }
}