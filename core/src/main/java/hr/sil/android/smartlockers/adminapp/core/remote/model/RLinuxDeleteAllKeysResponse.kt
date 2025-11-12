package hr.sil.android.smartlockers.adminapp.core.remote.model

class RLinuxDeleteAllKeysResponse {
    var error: RLinuxDeleteAllKeysEnumResponse = RLinuxDeleteAllKeysEnumResponse.UNHANDLED_EXCEPTION
    var lockerMac:String =""
    var success: Boolean = true
}