package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired


interface PeripheralsP16Interface {

    fun onItemSelected(lockerIndex: Int, lockerP16Status: LockerP16Status, adapterPosition: Int )
    fun disableEnableReducedMobility(lockerIndex: Int, adapterPosition: Int, disableEnableReducedMobility: Boolean)
    fun cleanOrDirtyLocker(lockerIndex: Int, adapterPosition: Int, cleaningNeeded: RActionRequired)
}