package com.example.testeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import com.daasuu.gpuv.egl.filter.GlOverlayFilter

class GlBitmapOverlaySampleFilter(val bitmap: Bitmap) : GlOverlayFilter() {

    override fun drawCanvas(canvas: Canvas?) {
        if (!bitmap.isRecycled) {
            canvas?.drawBitmap(bitmap, 0F, 100F, null);
        }
    }

    override fun release() {
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}