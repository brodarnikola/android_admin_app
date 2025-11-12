package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

class RAssignedGroup {
    var buttonIndex: Int = 0
    var id: Int = 0

    @SerializedName("group___name")
    var groupName: String = ""

    @SerializedName("group___owner___telephone")
    var telephone: String = ""

    @SerializedName("group___id")
    var groupId: Int = 0

    @SerializedName("group___owner___email")
    var email: String = ""

    @SerializedName("group___owner___name")
    var groupOwnerName: String = ""

    @SerializedName("group___owner___status")
    var groupOwnerStatus: GroupOwnerStatus= GroupOwnerStatus.NAN

    @SerializedName("group___owner___requestedAction")
    var groupOwnerRequestedAction: GroupActionStatus? = null

    // this field is required for backend servis, API, "endUser/hardDelete"
    // and for backend servis, API "endUser/deactivate"
    @SerializedName("group___owner___id")
    var endUserId: Int = 0


    @SerializedName("group___owner___vendor")
    var isVendor: Boolean = false

}