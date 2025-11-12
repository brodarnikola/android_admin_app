package hr.sil.android.smartlockers.adminapp.core.remote.model


class REmptyGroupMembers : ItemRGroupInfo  {


    private val ITEM_EMPTY__OWNER_GROUP_MEMBERS_OR_GROUP_MEMBERSHIP = 3

    override fun getListItemType(): Int {
        return ITEM_EMPTY__OWNER_GROUP_MEMBERS_OR_GROUP_MEMBERSHIP
    }

    var emptyGroupMembers: String = ""
}