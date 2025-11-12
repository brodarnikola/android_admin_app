package hr.sil.android.smartlockers.adminapp.util

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by Stef on 15.12.2017..
 */
class RecyclerItemClickListener(val context: Context, val listener: OnItemClickListener) : RecyclerView.OnItemTouchListener {
    override fun onTouchEvent(p0: RecyclerView, p1: MotionEvent) {

    }


    val mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }
    })

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }


    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mGestureDetector.onTouchEvent(e)) {
            listener.onItemClick(childView, view.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}