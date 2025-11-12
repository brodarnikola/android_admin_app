package hr.sil.android.smartlockers.adminapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.RNetworkConfiguration

class NetworkConfigurationAdapter (val listOfNetworkConfiguration: List<RNetworkConfiguration>) : BaseAdapter() {
    override fun getItemId(p0: Int): Long {
        return listOfNetworkConfiguration[p0].id.toLong()
    }

    override fun getItem(p0: Int): Any {
        return listOfNetworkConfiguration[p0]
    }

    override fun getCount(): Int {
        return listOfNetworkConfiguration.size
    }

    override fun getView(p0: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: NetworkConfigurationViewHolder
        // val networkItemViewUI = NetworkItemViewUI()
        //val itemView = networkItemViewUI.createView(AnkoContext.Companion.create(viewGroup.context, viewGroup))
        val itemView =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_networks, viewGroup, false)

        //return UserViewHolder(itemView)

        if (convertView == null) {
            view = itemView
            itemRowHolder = NetworkConfigurationViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as NetworkConfigurationViewHolder
        }

        val band = BandTypes.values().filter { it.value == listOfNetworkConfiguration[p0].band }
        var bandName = ""
        if ( band.isNotEmpty()) {
            bandName = band.first().name
        }
        itemRowHolder.textView.text = listOfNetworkConfiguration[p0].name + " ($bandName)"

        //itemRowHolder.textView.text = listOfNetworkConfiguration[p0].userName
        return view
    }

    inner class NetworkConfigurationViewHolder(row: View) {
        val textView: TextView

        init {
            this.textView = row.findViewById(R.id.tvNetworkConfigName)
        }
    }

    enum class BandTypes(var value: Int) {
        DEFAULT(0),
        B1(1),
        B2(2),
        B3(3),
        B4(4),
        B5(5),
        B8(6),
        B12(7),
        B13(8),
        B18(9),
        B19(10),
        B20(11),
        B26(12),
        B28(13)
    }
}