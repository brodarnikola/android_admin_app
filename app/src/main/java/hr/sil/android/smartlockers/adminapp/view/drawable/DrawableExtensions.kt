package hr.sil.android.silwarebullet.ui.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * @author mfatiga
 */
fun Drawable.toBitmap(): Bitmap {
    return if (this is BitmapDrawable) {
        this.bitmap
    } else {
        val bitmap = Bitmap.createBitmap(
            this.intrinsicWidth,
            this.intrinsicHeight,
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)
        bitmap
    }
}