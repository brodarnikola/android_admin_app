package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

class RGroupDisplayMembersChild : ItemRGroupInfo  {


    private val ITEM_CHILD_ACCESS_SCREEN = 2

    override fun getListItemType(): Int {
        return ITEM_CHILD_ACCESS_SCREEN
    }

    @SerializedName("group___id")
    var groupId: Int = 0

    @SerializedName("group___name")
    var groupName: String = ""

    @SerializedName("group___owner___id")
    var groupOwnerId: Long = 0

    @SerializedName("group___owner___name")
    var groupOwnerName: String = ""

    @SerializedName("group___owner___email")
    var groupOwnerEmail: String = ""

    var role: String = ""

    @SerializedName("endUser___id")
    var endUserId: Int = 0

    @SerializedName("endUser___name")
    var endUserName: String = ""


    @SerializedName("endUser___email")
    var endUserEmail: String = ""

    @SerializedName("master___id")
    var master_id: Int = 0

}