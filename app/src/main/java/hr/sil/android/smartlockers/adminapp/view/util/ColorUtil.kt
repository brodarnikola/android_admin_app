package hr.sil.android.silwarebullet.ui.util

import androidx.annotation.ColorInt

/**
 * @author mfatiga
 */
object ColorUtil {
    private const val COLOR_MASK = 0x00FFFFFFL
    private const val ALPHA_MASK = 0xFF000000L

    fun getColorWithAlpha(color: Int, alpha: Float): Int {
        val colorOnly = color.toLong() and COLOR_MASK
        val alphaValue = (ALPHA_MASK * alpha.coerceIn(0f, 1f).toDouble()).toLong()
        return (colorOnly or alphaValue).toInt()
    }

    fun invertColor(color: Int): Int {
        val colorOnly = color.toLong() and COLOR_MASK
        val alphaOnly = color.toLong() and ALPHA_MASK
        val invertedColor = 0x00FFFFFFL xor colorOnly
        return (invertedColor or alphaOnly).toInt()
    }

    data class RGBColor(val r: Int, val g: Int, val b: Int) {
        companion object {
            fun fromColor(@ColorInt color: Int): RGBColor {
                return RGBColor(
                    r = (color and 0x00FF0000) shr 16,
                    g = (color and 0x0000FF00) shr 8,
                    b = (color and 0x000000FF) shr 0
                )
            }
        }

        fun toYUV() = YUVColor(
            y = (r *  0.29900 + g *  0.587   + b * 0.114).toInt().coerceIn(0, 255),
            u = (r * -0.16874 + g * -0.33126 + b * 0.50000 + 128).toInt().coerceIn(0, 255),
            v = (r *  0.50000 + g * -0.41869 + b * -0.08131 + 128).toInt().coerceIn(0, 255)
        )

        fun toColor() = (0xFF000000L or ((r shl 16) + (g shl 8) + (b shl 0)).toLong()).toInt()
    }

    data class YUVColor(val y: Int, val u: Int, val v: Int) {
        fun toRGB() = RGBColor(
            r = (y + (v - 128) *  1.40200).toInt().coerceIn(0, 255),
            g = (y + (u - 128) * -0.34414 + (v - 128) * -0.71414).toInt().coerceIn(0, 255),
            b = (y + (u - 128) *  1.77200).toInt().coerceIn(0, 255)
        )
    }

    fun oppositeColor(@ColorInt color: Int): Int {
        val yuv = RGBColor.fromColor(color).toYUV()
        val factor = 180
        val threshold = 100
        val y = (yuv.y + (if (yuv.y > threshold) -factor else factor)).coerceIn(0, 255)
        return yuv.copy(y = y).toRGB().toColor()
    }
}