package hr.sil.android.smartlockers.adminapp.data

import android.view.View
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize


interface AddLockerInterface {

    fun addSlaveSizeDialog(itemView: View, slaveMacAddress: String, lockerSize: RLockerSize, reducedMobility: Boolean)
}