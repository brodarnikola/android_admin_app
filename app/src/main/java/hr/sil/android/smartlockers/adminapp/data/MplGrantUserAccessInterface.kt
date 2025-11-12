package hr.sil.android.smartlockers.adminapp.data

interface MplGrantUserAccessInterface {

    fun allowUserAccessToMPL(accessId: Int, status: MPLAppDeviceStatus, index: Int = 0)

    fun removeUserAccessFromMPL(  masterMac: String, index: Int )

    fun removeUserRequestFromMPL( accessId: Int, status: MPLAppDeviceStatus )
}