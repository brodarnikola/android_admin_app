package hr.sil.android.smartlockers.adminapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.smartlockers.adminapp.R

class LockerSizeAdapter(val lockerSizeList: List<String>) : BaseAdapter() {

    override fun getItem(position: Int): Any {
        return lockerSizeList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return lockerSizeList.size
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: NetworkConfigurationViewHolder
        val itemView =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_locker_size, viewGroup, false)

        if (convertView == null) {
            view = itemView
            itemRowHolder = NetworkConfigurationViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as NetworkConfigurationViewHolder
        }

        itemRowHolder.textView.text = lockerSizeList[position]
        return view
    }

    inner class NetworkConfigurationViewHolder(row: View) {
        val textView: TextView

        init {
            this.textView = row.findViewById(R.id.item_netowrk_config_name)
        }
    }
}