package hr.sil.android.smartlockers.adminapp.core.remote.model

enum class GroupOwnerStatus (val value: Int){
    NAN (0), ACTIVE (1), INVITED (2), INACTIVE (3), PENDING_VERIFICATION(4)
}