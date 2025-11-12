package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

class RGroupDisplayMembersHeader : ItemRGroupInfo {

    private val ITEM_HEADER_ACCESS_SCREEN = 0

    override fun getListItemType(): Int {

        return ITEM_HEADER_ACCESS_SCREEN
    }

    @SerializedName("group___owner___name")
    var groupOwnerName: String = ""
}