package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

class RGroupDisplayMembersAdmin : ItemRGroupInfo  {


    private val ITEM_ADMIN_NAME_ACCESS_SCREEN = 1

    override fun getListItemType(): Int {
        return ITEM_ADMIN_NAME_ACCESS_SCREEN
    }

    @SerializedName("group___owner___name")
    var groupOwnerName: String = ""

    @SerializedName("role")
    var role: String = ""


}