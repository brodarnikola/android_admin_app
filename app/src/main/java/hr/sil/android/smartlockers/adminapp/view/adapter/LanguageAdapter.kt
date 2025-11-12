package hr.sil.android.smartlockers.adminapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLanguage

class LanguageAdapter(val listOfLanguages: List<RLanguage>) : BaseAdapter() {

    override fun getItemId(position: Int): Long {
        return listOfLanguages[position].id.toLong()
    }

    override fun getItem(position: Int): Any {
        return listOfLanguages[position]
    }

    override fun getCount(): Int {
        return listOfLanguages.size
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: NetworkConfigurationViewHolder
        val itemView =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_languages, viewGroup, false)

        if (convertView == null) {
            view = itemView
            itemRowHolder = NetworkConfigurationViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as NetworkConfigurationViewHolder
        }

        itemRowHolder.textView.text = listOfLanguages[position].name
        return view
    }

    inner class NetworkConfigurationViewHolder(row: View) {
        val textView: TextView

        init {
            this.textView = row.findViewById(R.id.item_netowrk_config_name)
        }
    }
}