package com.summitcodeworks.watermarklib

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import com.squareup.picasso.Picasso

// Watermark options for text
data class TextWatermarkOptions(
    val text: String,
    val textSize: Float = 24f,
    val color: Int = Color.WHITE,
    val alpha: Int = 150,
    val positionX: Float = 50f,
    val positionY: Float = 50f,
    val rotation: Float = -45f,
    val fontResId: Int? = null // Option for custom font
)

// Watermark options for image
data class ImageWatermarkOptions(
    val watermarkUri: Uri,
    val scale: Float = 0.25f,
    val alpha: Int = 150,
    val positionX: Float = 0f,
    val positionY: Float = 0f
)

object WatermarkUtil {

    // Function to add text watermark to a Bitmap using customizable options
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
        paint.textAlign = Paint.Align.LEFT

        // Rotate and draw the text on canvas
        canvas.rotate(options.rotation, result.width / 2f, result.height / 2f)
        canvas.drawText(options.text, options.positionX, options.positionY, paint)

        return result
    }

    // Function to add image watermark to a Bitmap using customizable options
    fun addImageWatermark(
        context: Context,
        originalBitmap: Bitmap,
        options: ImageWatermarkOptions
    ): Bitmap {
        val watermarkBitmap = Picasso.get().load(options.watermarkUri).get()
        val scaledWidth = (watermarkBitmap.width * options.scale).toInt()
        val scaledHeight = (watermarkBitmap.height * options.scale).toInt()
        val scaledWatermark = Bitmap.createScaledBitmap(watermarkBitmap, scaledWidth, scaledHeight, true)

        val result = originalBitmap.copy(originalBitmap.config, true)
        val canvas = Canvas(result)
        val paint = Paint()
        paint.alpha = options.alpha

        // Draw watermark image on canvas
        canvas.drawBitmap(scaledWatermark, options.positionX, options.positionY, paint)

        return result
    }
}
