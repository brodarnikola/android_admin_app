package hr.sil.android.smartlockers.adminapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLinuxDeleteAllKeysEnumResponse
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLinuxDeleteAllKeysResponse
import hr.sil.android.smartlockers.adminapp.core.util.logger

class UnsuccessfullyDeleteAllKeysLinuxAdapter(
    private var unsuccessfullyDeleteKeysList: MutableList<RLinuxDeleteAllKeysResponse>
) : RecyclerView.Adapter<UnsuccessfullyDeleteAllKeysLinuxAdapter.UserViewHolder>() {

    val log = logger()

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bindItem(unsuccessfullyDeleteKeysList[position])

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_unsuccessfully_delete_all_keys_linux, parent, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount() = unsuccessfullyDeleteKeysList.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val log = logger()
        private val tvErrorResponse: TextView = itemView.findViewById(R.id.tvErrorResponse)
        private val tvLockerMac: TextView = itemView.findViewById(R.id.tvLockerMac)

        fun bindItem(currentItem: RLinuxDeleteAllKeysResponse) {

            when (currentItem.error) {
                RLinuxDeleteAllKeysEnumResponse.LOCKER_DOOR_OPEN_FAIL -> tvErrorResponse.text = itemView.resources.getString(R.string.locker_door_open_fail_linux)
                RLinuxDeleteAllKeysEnumResponse.NO_ACTIVE_KEY -> tvErrorResponse.text = itemView.resources.getString(R.string.no_active_key_linux)
                else -> tvErrorResponse.text = itemView.resources.getString(R.string.unhandled_exception_linux)
            }

            tvLockerMac.text = currentItem.lockerMac
        }
    }
}