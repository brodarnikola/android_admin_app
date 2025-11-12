package hr.sil.android.smartlockers.adminapp.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger


class P16LockerSizeAdapter(val lockerSizeList: MutableList<String>) : PagerAdapter() {

    val log = logger()
    var lockerSizeTextColor = 0

    override fun instantiateItem(container: ViewGroup, position: Int): View {

        val layoutInflater = container.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = layoutInflater.inflate(R.layout.layout_locker_size_slider, container, false)

        val tvLockerSize = view?.findViewById(R.id.tvLockerSize) as TextView
        tvLockerSize.text = lockerSizeList[position]
        tvLockerSize.setTextColor(lockerSizeTextColor)

        container.addView(view)

        return view
    }

    fun setSizeTextColor( textColor: Int ) {
        lockerSizeTextColor = textColor
        log.info("text color is: ${textColor} ")
    }

    override fun getCount(): Int {
        return lockerSizeList.size
    }

    override fun isViewFromObject(view: View, objectView: Any): Boolean {
        return view === objectView
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

}
