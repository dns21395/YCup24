package com.example.ycup24.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import com.example.ycup24.R
import com.example.ycup24.ui.model.Point
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SaveGifRepository @Inject constructor(
    private val context: Context
) {

    fun saveGif(
        frames: List<List<Point>>,
        width: Int,
        height: Int,
        brushRadius: Float,
        delay: Int,
    ) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "ycup_gif_${System.currentTimeMillis()}.gif"
        )
        val fos = FileOutputStream(file)

        val gifEncoder = AnimatedGifEncoder()
        gifEncoder.start(fos)
        gifEncoder.setRepeat(0)
        gifEncoder.setDelay(delay)

        val background = ContextCompat.getDrawable(context, R.drawable.background)
        background?.setBounds(0, 0, width, height)

        for (frame in frames) {
            val bitmap = createBitmap(frame, width, height, brushRadius, background!!)
            gifEncoder.addFrame(bitmap)
            bitmap.recycle()
        }

        gifEncoder.finish()
        fos.close()

        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { p0, p1 -> }
    }

    private fun createBitmap(
        pointers: List<Point>,
        width: Int,
        height: Int,
        brushRadius: Float,
        background: Drawable,
    ): Bitmap {

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.RGB_565
        )
        val canvas = android.graphics.Canvas(bitmap)

        background.draw(canvas)

        for (point in pointers) {
            val paint = Paint().apply {
                color = androidx.compose.ui.graphics.Color(point.color).toArgb()
                style = Paint.Style.FILL
            }

            canvas.drawCircle(
                point.position.x.toFloat(),
                point.position.y.toFloat(),
                brushRadius,
                paint
            )
        }


        return bitmap
    }
}