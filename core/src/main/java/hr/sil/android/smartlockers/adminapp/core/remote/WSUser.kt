package hr.sil.android.smartlockers.adminapp.core.remote

import hr.sil.android.smartlockers.adminapp.core.remote.base.WSBase
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.remote.service.UserAppService
import hr.sil.android.rest.core.configuration.ServiceConfig
import retrofit2.Call

/**
 * @author mfatiga
 */
object WSUser : WSBase() {

    suspend fun registerDevice(pushToken: String?, metaData: String = ""): Boolean {
        if (pushToken != null) {
            val request = RUserDeviceInfo().apply {
                this.appKey = ServiceConfig.cfg.appKey
                this.token = pushToken
                this.type = RUserDeviceType.ANDROID
                this.metadata = metaData
            }
            return wrapAwaitIsSuccessful(
                    call = UserAppService.service.registerDevice(request),
                    methodName = "registerDevice()"
            )
        } else {
            return false
        }
    }

    suspend fun getGeneratedPinFromBackendForSendParcel(masterId: Int): String? {
        return wrapAwaitData(
                call = UserAppService.service.getGeneratedPinFromBackendForSendParcel(masterId),
                methodName = "getGeneratedPinFromBackendForSendParcel()"

        )
    }

    suspend fun getLanguages(): List<RLanguage>? {
        return wrapAwaitData(
                call = UserAppService.service.getLanguages(),
                methodName = "getLanguages()",
                defaultNullValue = listOf()
        )
    }

    suspend fun getDeviceInfo(macAddress: String): RLockerInfo? {
        return wrapAwaitData(
                call = UserAppService.service.getLockerInfo(macAddress),
                methodName = "getDeviceInfo()"
        )
    }

    suspend fun getDevicesInfo(macAddress: List<String>): List<RLockerInfo>? {
        log.info("Addresses for info: " + macAddress.joinToString(",") { it })
        return wrapAwaitData(
                call = UserAppService.service.getLockersInfo(macAddress),
                methodName = "getDevicesInfo()"
        )
    }


    suspend fun registerEndUser(
            name: String,
            address: String,
            telephone: String,
            email: String,
            password: String,
            language: RLanguage): REndUserInfo? {

        val request = REndUserRegisterRequest().apply {
            this.name = name
            this.address = address
            this.telephone = telephone
            this.email = email
            this.password = password
            this.languageId = language.id
            this.hasAcceptedTerms = true
        }
        return wrapAwaitData(
                call = UserAppService.service.registerEndUser(request),
                methodName = "registerEndUser()"
        )
    }

    suspend fun requestPasswordRecovery(email: String): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.requestPasswordRecovery(email),
                methodName = "requestPasswordRecovery()"
        )
    }

    suspend fun resetPassword(email: String, passwordCode: String, password: String): Boolean {
        val request = RResetPasswordRequest().apply {
            this.email = email
            this.passwordCode = passwordCode
            this.password = password
        }
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.resetPassword(request),
                methodName = "resetPassword()"
        )
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val request = RUpdatePasswordRequest().apply {
            this.oldPassword = oldPassword
            this.newPassword = newPassword
        }
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.updatePassword(request),
                methodName = "updatePassword()"
        )
    }


    suspend fun updateUserGroupName(name: String): REndUserGroupInfo? {

        val request = RUpdateUserGroupRequest().apply {
            this.name = name
        }
        return wrapAwaitData(
                call = UserAppService.service.updateUserGroup(request),
                methodName = "updateUserGroupName()"
        )
    }

    suspend fun updateUserProfile(
            name: String,
            address: String,
            telephone: String,
            language: RLanguage,
            isPushNotified: Boolean,
            isEmailNotified: Boolean,
            groupName: String

    ): REndUserInfo? {
        val request = RUpdateUserProfileRequest().apply {
            this.name = name
            this.address = address
            this.telephone = telephone
            this.languageId = language.id
            this.isNotifyPush = isPushNotified
            this.isNotifyEmail = isEmailNotified
            this.groupName = groupName
        }
        return wrapAwaitData(
                call = UserAppService.service.updateUserProfile(request),
                methodName = "updateUserProfile()"
        )
    }

    suspend fun getUserInfo(): REndUserInfo? {
        return wrapAwaitData(
                call = UserAppService.service.getUserInfo(),
                methodName = "getUserInfo()"
        )
    }

    suspend fun getUserGroupInfo(): REndUserGroupInfo? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupInfo(),
                methodName = "getUserGroupInfo()"
        )
    }

    suspend fun getMasterUnits(): List<RMasterUnit>? {
        return wrapAwaitData(
                call = UserAppService.service.getMasterUnits(),
                methodName = "getMasterUnits()",
                defaultNullValue = listOf()
        )
    }

    suspend fun getGroupMembers(): List<REndUserGroupMember>? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupMembers(),
                methodName = "getGroupMembers()",
                defaultNullValue = listOf<REndUserGroupMember>()
        )
    }

    suspend fun getGroupMemberships(): List<RGroupInfo>? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupMemberships(),
                methodName = "getGroupMemberships()",
                defaultNullValue = listOf<RGroupInfo>()
        )
    }


    suspend fun getGroupMembershipsById(groupId: Long): MutableList<RGroupInfo>? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupMembershipsById(groupId),
                methodName = "getGroupMembershipsById()",
                defaultNullValue = mutableListOf<RGroupInfo>()
        )
    }

    suspend fun getActiveKeys(): List<RLockerKey>? {
        return wrapAwaitData(
                call = UserAppService.service.getActiveKeys(),
                methodName = "getActiveKeys()"
        )
    }

    suspend fun getActivePaFCreatedKeys(): List<RCreatedLockerKey>? {
        return wrapAwaitData(
                call = UserAppService.service.getActivePaFCreatedKeys(),
                methodName = "getActivePaFCreatedKeys()"
        )
    }

    suspend fun getActivePaHCreatedKeys(): MutableList<RCreatedLockerKey>? {
        return wrapAwaitData(
                call = UserAppService.service.getActivePaHCreatedKeys(),
                methodName = "getActivePaHCreatedKeys()"
        )
    }

    suspend fun getAvailableLockerSizes(masterUnitId: Int): List<RAvailableLockerSize>? {
        return wrapAwaitData(
                call = UserAppService.service.getAvailableLockerSizes(masterUnitId),
                methodName = "getAvailableLockerSizes()",
                defaultNullValue = listOf()
        )
    }

    suspend fun requestMPlAccess(macAddress: String): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.requestAccess(macAddress),
                methodName = "requestAccess()"

        )
    }
    suspend fun getActiveRequests(): List<RAccessRequest>? {
        return wrapAwaitData(
                call = UserAppService.service.getActiveAccessRequests(),
                methodName = "requestAccess()"

        )
    }

    suspend fun activateSPL(macAddress: String): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.activateSpl(macAddress),
                methodName = "activateSPL()"
        )
    }

    suspend fun deactivateSPL(macAddress: String): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.deactivateSpl(macAddress),
                methodName = "deactivateSPL()"
        )
    }


    suspend fun addUserAccess(userAccess: RUserAccess): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.addUserAccess(userAccess),
                methodName = "addUserAccess()"
        )
    }

    suspend fun removeUserAccess(userAccess: RUserRemoveAccess): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.removeUserAccess(userAccess),
                methodName = "addUserAccess()"
        )
    }

    suspend fun createPaF(keyId: Int, email: String): Boolean {
        val pafKey = RCreatePaf().apply {
            this.keyId = keyId
            this.email = email
        }
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.createPaF(pafKey),
                methodName = "createPaF()"
        )

    }

    suspend fun deletePaF(keyId: Int): Boolean {
        val pafKey = RDeletePaf().apply {
            this.keyId = keyId
        }
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.deletePaF(pafKey),
                methodName = "deletePaF()"
        )

    }

    suspend fun modifyMasterUnit(unit: RMasterUnit): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.modifySpl(masterUnit = unit),
                methodName = "modifyMasterUnit()"
        )

    }

    suspend fun ping(): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.ping(),
                methodName = "serverPing()"
        )
    }


    override fun callEncryptService(mac: String, request: REncryptRequest): Call<REncryptResponse> {
        return UserAppService.service.encrypt(mac, request)
    }
}