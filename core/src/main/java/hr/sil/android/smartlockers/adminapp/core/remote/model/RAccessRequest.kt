package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName
import java.util.*

class RAccessRequest {

    var id: Int = 0
    var dateRequested: Date = Date()

    @SerializedName("master___id")
    var masterId: Int = 0

    @SerializedName("master___mac")
    var masterMac: String = ""


    @SerializedName("master___name")
    var masterName: String = ""

    @SerializedName("master___address")
    var masterAddress: String = ""

    @SerializedName("group___id")
    var groupId: Int= 0

    @SerializedName("group___owner___id")
    var groupOwnerId: Int= 0

    @SerializedName("group___owner___name")
    var groupOwnerName: String= ""

    @SerializedName("group___owner___email")
    var groupOwnerEmail: String= ""



}