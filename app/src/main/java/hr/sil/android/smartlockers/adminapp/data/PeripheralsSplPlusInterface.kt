package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.data.LockerP16Status

interface PeripheralsSplPlusInterface {

    fun onItemSelected(lockerIndex: Int, lockerP16Status: LockerP16Status, adapterPosition: Int )
}