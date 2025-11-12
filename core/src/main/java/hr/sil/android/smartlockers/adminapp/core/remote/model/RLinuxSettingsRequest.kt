package hr.sil.android.smartlockers.adminapp.core.remote.model


class RLinuxSettingsRequest {
    var address: String = ""

    var addressObject: RLinuxAddressSettingsRequest = RLinuxAddressSettingsRequest()

    var alertsEnabled: Boolean = true
    var barcodeScannerEnabled: Boolean = true
    var courierIdentificationRequired: Boolean = true
    var endUserIdenfiticationEnabled: Boolean = true
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var masterId: Int? = 0
    var name:String =""
    var pahEnabled: Boolean = true
    var publicDevice: Boolean = true
}