package hr.sil.android.smartlockers.adminapp.core.remote.model

class RLinuxSizeReducedMobilityRequest {
    var actionRequired: RActionRequired? = RActionRequired.NULL
    var lockerId: Int = 0
    var lockerSize:String =""
    var reducedMobility: Boolean = true
}