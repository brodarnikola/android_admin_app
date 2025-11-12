package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.core.remote.model.GroupActionStatus
import hr.sil.android.smartlockers.adminapp.core.remote.model.GroupOwnerStatus
import hr.sil.android.smartlockers.adminapp.core.remote.model.RAssignedGroup
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnitAccessRequests

class RMplUserAccess() {
    var status: MPLAppDeviceStatus = MPLAppDeviceStatus.UNREGISTERED
    var groupName: String = ""
    var name: String = ""
    var email: String = ""
    var phoneNumber: String = ""
    var index: Int = 0
    var accessId: Int = 0
    var groupId: Int = 0
    var endUserId: Int = 0
    var groupOwnerStatus = GroupOwnerStatus.NAN
    var ownerActionStatus: GroupActionStatus? = null

    var isRequestsAccessAllowed: Boolean = true

    var isSelected: Boolean = false

    var isVendor: Boolean = false

    constructor(masterUnitRequest: RMasterUnitAccessRequests) : this() {
        groupName = masterUnitRequest.groupName
        name = masterUnitRequest.groupOwnerName
        email = masterUnitRequest.groupOwnerEmail
        phoneNumber = masterUnitRequest.groupOwnerPhone
        accessId = masterUnitRequest.id
        status = MPLAppDeviceStatus.NEW
        groupId = masterUnitRequest.groupId
        endUserId = masterUnitRequest.endUserId
        isRequestsAccessAllowed = masterUnitRequest.isRequestsAccessAllowed
    }

    constructor(assignedGroup: RAssignedGroup) : this() {
        index = assignedGroup.buttonIndex
        groupName = assignedGroup.groupName
        name = assignedGroup.groupOwnerName
        email = assignedGroup.email
        phoneNumber = assignedGroup.telephone
        status = MPLAppDeviceStatus.REGISTERED
        groupId = assignedGroup.groupId
        endUserId = assignedGroup.endUserId
        groupOwnerStatus = assignedGroup.groupOwnerStatus
        ownerActionStatus = assignedGroup.groupOwnerRequestedAction
        isVendor = assignedGroup.isVendor
    }

}