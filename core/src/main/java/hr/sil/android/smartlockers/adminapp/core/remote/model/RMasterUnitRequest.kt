package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

class RMasterUnitRequest {
    var name:String =""
    var address: String = ""

    @SerializedName("address___country")
    var address___country: String = ""

    @SerializedName("address___houseNumber")
    var address___houseNumber: String = ""
    @SerializedName("address___postcode")
    var address___postcode: String = ""
    @SerializedName("address___street")
    var address___street: String = ""
    @SerializedName("address___town")
    var address___town: String = ""

    var allowPinSave: Boolean = false
    var powerType: RPowerTypeEnum = RPowerTypeEnum.BATTERY
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    @SerializedName("networkConfiguration___id")
    var networkConfigurationId: Int = 0

    var epdType___id: Int? = 0

    var alertsEnabled: Boolean = false

    var supportsDoorbell: Boolean? = false
}