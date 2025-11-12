package hr.sil.android.smartlockers.adminapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.util.ListDiffer

class SearchOptionsLinuxAdapter ( val searchOptions: MutableList<String>) : RecyclerView.Adapter<SearchOptionsLinuxAdapter.PeripheralItemViewHolder>() {

    val log = logger()


    override fun onBindViewHolder(holder: PeripheralItemViewHolder, position: Int) {
        holder.bindItem(searchOptions[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeripheralItemViewHolder {

        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_search_options_linux, parent, false)
        return PeripheralItemViewHolder(itemView)
    }

    override fun getItemCount() = searchOptions.size

    fun updateDevices(updatedDevices: List<String>) {
        val listDiff = ListDiffer.getDiff(
            searchOptions,
            updatedDevices,
            { old, new ->
                 old == new
            })

        for (diff in listDiff) {
            when (diff) {
                is ListDiffer.DiffInserted -> {
                    searchOptions.addAll(diff.elements)
                    notifyItemRangeInserted(diff.position, diff.elements.size)
                }
                is ListDiffer.DiffRemoved -> {
                    //remove devices
                    for (i in (searchOptions.size - 1) downTo diff.position) {
                        searchOptions.removeAt(i)
                    }
                    notifyItemRangeRemoved(diff.position, diff.count)
                }
                is ListDiffer.DiffChanged -> {
                    searchOptions[diff.position] = diff.newElement
                    notifyItemChanged(diff.position)
                }
            }
        }
    }

    inner class PeripheralItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val log = logger()

        val tvSearchText: TextView = itemView.findViewById(R.id.tvSearchText)

        //?attr/
//        private fun getDrawableAttrValue(attr: Int): Drawable? {
//            val attrArray = intArrayOf(attr)
//            val typedArray = activity.obtainStyledAttributes(attrArray)
//            val result = try {
//                typedArray.getDrawable(0)
//            } catch (exc: Exception) {
//                null
//            }
//            typedArray.recycle()
//            return result
//        }
//
//        private fun getColorAttrValue(attr: Int): Int {
//            val attrArray = intArrayOf(attr)
//            val typedArray =
//                activity.obtainStyledAttributes(attrArray)
//            val result = typedArray.getColor(
//                0,
//                Color.WHITE
//            ) //try { typedArray.getColorOrThrow(0) } catch (exc: Exception) { null } ?: 0
//            typedArray.recycle()
//            return result
//        }

        fun bindItem(searchText: String) {
            tvSearchText.text = searchText
        }

    }

}
