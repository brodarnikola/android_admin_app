package hr.sil.android.silwarebullet.ui.drawable

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable

/**
 * @author mfatiga
 */
sealed class BackgroundDrawable(
    private val fillColor: Int?,
    private val borderColor: Int?,
    protected val borderWidth: Float?,
    private val antiAlias: Boolean) : Drawable() {

    open fun isEquivalentTo(other: BackgroundDrawable): Boolean {
        if (fillColor != other.fillColor) return false
        if (borderColor != other.borderColor) return false
        if (borderWidth != other.borderWidth) return false
        if (antiAlias != other.antiAlias) return false
        return true
    }

    fun asRipple(rippleColor: Int): RippleDrawable {
        return RippleDrawable(
            ColorStateList(arrayOf(intArrayOf()), intArrayOf(rippleColor)),
            this,
            null
        )
    }

    protected val fillPaint: Paint?
    protected val borderPaint: Paint?

    init {
        if (fillColor != null) {
            fillPaint = Paint()
            fillPaint.style = Paint.Style.FILL
            fillPaint.color = fillColor
            fillPaint.isAntiAlias = if (borderColor != null) false else antiAlias
        } else {
            fillPaint = null
        }

        if (borderColor != null) {
            borderPaint = Paint()
            borderPaint.style = Paint.Style.STROKE
            borderPaint.color = borderColor
            borderPaint.isAntiAlias = antiAlias
            borderPaint.strokeWidth = borderWidth ?: 1.0f
        } else {
            borderPaint = null
        }
    }

    override fun setAlpha(alpha: Int) {
        fillPaint?.alpha = alpha
        borderPaint?.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint?.colorFilter = colorFilter
        borderPaint?.colorFilter = colorFilter
    }

    class Circle(
        fillColor: Int? = null,
        borderColor: Int? = null,
        borderWidth: Float? = null,
        antiAlias: Boolean = false
    ) : BackgroundDrawable(fillColor, borderColor, borderWidth, antiAlias) {
        override fun isEquivalentTo(other: BackgroundDrawable): Boolean {
            if (other == this) return true
            if (javaClass != other.javaClass) return false

            other as Circle

            return super.isEquivalentTo(other)
        }

        override fun draw(canvas: Canvas) {
            val width = bounds.width()
            val height = bounds.height()
            val centerX = width / 2.0F
            val centerY = height / 2.0F

            if (fillPaint != null) {
                canvas.drawCircle(centerX, centerY, centerX, fillPaint)
            }

            if (borderPaint != null) {
                canvas.drawCircle(centerX, centerY, centerX, borderPaint)
            }
        }
    }

    class Rect(
        fillColor: Int? = null,
        borderColor: Int? = null,
        borderWidth: Float? = null,
        antiAlias: Boolean = false,
        private val cornerRadius: Float? = null
    ) : BackgroundDrawable(fillColor, borderColor, borderWidth, antiAlias) {
        override fun isEquivalentTo(other: BackgroundDrawable): Boolean {
            if (other == this) return true
            if (javaClass != other.javaClass) return false

            other as Rect

            if (!super.isEquivalentTo(other)) return false
            if (cornerRadius != other.cornerRadius) return false

            return true
        }

        override fun draw(canvas: Canvas) {
            val h = bounds.height()
            val w = bounds.width()
            val offset = (borderWidth ?: 0f) / 2f
            val rect = RectF(offset, offset, w.toFloat() - offset, h.toFloat() - offset)
            if (fillPaint != null) {
                if (cornerRadius != null && cornerRadius > 0.0f) {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)
                } else {
                    canvas.drawRect(rect, fillPaint)
                }
            }

            if (borderPaint != null) {
                if (cornerRadius != null && cornerRadius > 0.0f) {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
                } else {
                    canvas.drawRect(rect, borderPaint)
                }
            }
        }
    }
}