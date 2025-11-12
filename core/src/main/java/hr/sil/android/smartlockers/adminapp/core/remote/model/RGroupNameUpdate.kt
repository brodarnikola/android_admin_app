package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

class RGroupNameUpdate {
    @SerializedName("groupId")
    var groupId: Int = 0

    @SerializedName("endUserName")
    var endUserName: String = ""

    @SerializedName("groupName")
    var groupName: String = ""
}