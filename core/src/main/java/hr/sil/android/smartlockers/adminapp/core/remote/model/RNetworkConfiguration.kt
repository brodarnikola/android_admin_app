package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

class RNetworkConfiguration {
    val id: Int = 0
    val name: String = ""

    @SerializedName("customer___id")
    val customerId: Int = 0

    @SerializedName("customer___name")
    val customerName: String = ""

    val apnUrl: String = ""
    val apnUser: String? = null
    val apnPass: String? = null

    val modemRadioAccess: RRadioAccessTechnology = RRadioAccessTechnology.NB1
    val plmn: String? =""
    val band: Int? = 0

}