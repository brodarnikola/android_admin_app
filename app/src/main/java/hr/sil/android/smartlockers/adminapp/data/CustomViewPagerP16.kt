package hr.sil.android.smartlockers.adminapp.data

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class CustomViewPagerP16 : ViewPager {


    private var initialXValue: Float = 0.toFloat()
    private var direction: ViewPagerSwipeDirection = ViewPagerSwipeDirection.ALL

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.isSwipeAllowed(event)) {
            super.onTouchEvent(event)
        } else false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.isSwipeAllowed(event)) {
            super.onInterceptTouchEvent(event)
        } else false

    }

    private fun isSwipeAllowed(event: MotionEvent): Boolean {
        if (this.direction === ViewPagerSwipeDirection.ALL) return true

        if (direction == ViewPagerSwipeDirection.NONE)
        //disable any swipe
            return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            initialXValue = event.x
            return true
        }

        if (event.action == MotionEvent.ACTION_MOVE) {
            try {
                val diffX = event.x - initialXValue
                if (diffX > 0 && direction == ViewPagerSwipeDirection.RIGHT) {
                    // swipe from left to right detected
                    return false
                } else if (diffX < 0 && direction == ViewPagerSwipeDirection.LEFT) {
                    // swipe from right to left detected
                    return false
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

        }

        return true
    }

    fun setAllowedSwipeDirection(direction: ViewPagerSwipeDirection) {
        this.direction = direction
    }

}