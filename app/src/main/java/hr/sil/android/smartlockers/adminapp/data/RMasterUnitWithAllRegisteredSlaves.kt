package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerUnit
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnit


class RMasterUnitWithAllRegisteredSlaves {
    var masterUnit: RMasterUnit = RMasterUnit()
    var lockerUnits: List<RLockerUnit> = listOf()
}