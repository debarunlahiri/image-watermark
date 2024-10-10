package com.summitcodeworks.watermarklib

import android.content.Context
import android.graphics.*
import androidx.core.content.res.ResourcesCompat

data class TextWatermarkOptions(
    val text: String,
    val textSize: Float = 24f,
    val color: Int = Color.WHITE,
    val alpha: Int = 150,
    val rotation: Float = -45f,
    val fontResId: Int? = null,
    val tile: Boolean = false,
    val x: Float? = null,
    val y: Float? = null
)

object WatermarkUtil {

    fun addTextWatermark(
        context: Context,
        originalBitmap: Bitmap,
        options: TextWatermarkOptions
    ): Bitmap {
        val result = originalBitmap.copy(originalBitmap.config, true)
        val canvas = Canvas(result)
        val paint = Paint()

        paint.color = options.color
        paint.alpha = options.alpha
        paint.textSize = options.textSize
        paint.isAntiAlias = true
        options.fontResId?.let {
            paint.typeface = ResourcesCompat.getFont(context, it)
        }

        val positionX = options.x ?: (result.width - paint.measureText(options.text)) / 2
        val positionY = options.y ?: (result.height / 2) + (paint.textSize / 2)

        if (options.tile) {
            for (x in 0 until result.width step ((paint.measureText(options.text) + 20).toInt())) {
                for (y in 0 until result.height step 50) {
                    canvas.save()
                    canvas.rotate(options.rotation, x + 10f, y + paint.textSize)
                    canvas.drawText(options.text, x.toFloat(), y + paint.textSize, paint)
                    canvas.restore()
                }
            }
        } else {
            canvas.save()
            canvas.rotate(options.rotation, positionX + 10f, positionY)
            canvas.drawText(options.text, positionX, positionY, paint)
            canvas.restore()
        }

        return result
    }
}
