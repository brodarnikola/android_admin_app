/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.smartlockers.adminapp.core.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import hr.sil.android.smartlockers.adminapp.core.model.RUpdateAdminInfo
import hr.sil.android.smartlockers.adminapp.core.remote.base.WSBase
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.remote.service.AdminAppService
import hr.sil.android.smartlockers.adminapp.core.remote.service.WebAppService
import hr.sil.android.rest.core.configuration.ServiceConfig
import hr.sil.android.smartlockers.adminapp.core.remote.service.AdminPublicService
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.util.general.extensions.toHexString
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * @author mfatiga
 */
object WSAdmin : WSBase() {



    suspend fun registerDevice(pushToken: String?, metaData: String = ""): Boolean {
        if (pushToken != null) {
            log.info("AppKey: " + ServiceConfig.cfg.appKey)
            val request = RUserDeviceInfo().apply {
                this.appKey = ServiceConfig.cfg.appKey
                this.token = pushToken
                this.type = RUserDeviceType.ANDROID
                this.metadata = metaData
            }
            log.info("Trying to register device...")
            return wrapAwaitIsSuccessful(
                call = AdminPublicService.service.registerDevice(request),
                methodName = "registerDevice()"
            )
        } else {
            log.info("Push Token is null ")
            return false
        }
    }

    suspend fun login(): RAdminUserInfo? {
        return wrapAwaitData(
            call = AdminAppService.service.login(),
            methodName = "activity()"
        )
    }

    suspend fun getAccountInfo(): RAdminUserInfo? {
        return wrapAwaitData(
            call = AdminAppService.service.getAccountInfo(),
            methodName = "getAccountInfo()"
        )
    }

    suspend fun modifyMasterUnit(mac: String, request: RMasterUnitRequest): RMasterUnit? {
        return wrapAwaitData(
            call = AdminAppService.service.modifyMaster(mac, request),
            methodName = "modifyMaster()"
        )
    }
    suspend fun updateNameAndGroupName(groupUpdate: RGroupNameUpdate): Void?{
        return wrapAwaitData(
            call = AdminAppService.service.updateNameAndGroupName(groupUpdate),
            methodName = "updateNameAndGroupName()"
        )
    }

    suspend fun getGlobalConfigurationData(): RGlobalConfigurationData? {
        return wrapAwaitData(
            call = AdminAppService.service.getGlobalConfigurationData(),
            methodName = "getGlobalConfigurationData()"
        )
    }

    suspend fun getDeviceApiKey(challenge: ByteArray, masterBleMacAddress: String): ByteArray? {
        val result = wrapAwaitData(
            call = AdminAppService.service.getDeviceApiKey(
                masterBleMacAddress.macRealToClean(),
                challenge.toHexString()),
            methodName = "getDeviceApiKey()"
        )

        val b64 = result?.data
        return if (b64 != null) {
            Base64.decode(b64, Base64.DEFAULT)
        } else null
    }

    suspend fun getLockers(masterBleMacAddress: String): List<RLockerUnit>? {
        log.info("Calling backend method getLockerForMaster")
        return wrapAwaitData(
            call = AdminAppService.service.getLockerForMaster(masterBleMacAddress.macRealToClean()),
            methodName = "getLockerForMaster()"
        )
    }
    suspend fun getEpdPreview(lockerId: Long): Bitmap? {
        log.info("Calling backend method getEpdPreview")
        val responseBody =wrapAwaitData(
            call = AdminAppService.service.getEpdPreview(lockerId),
            methodName = "getEpdPreview()"
        )


        val bytes = responseBody?.bytes()
        log.info("Getting bitmap response ${bytes?.size}")

        val bitmap =if (bytes != null) BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null

        return bitmap
    }

    suspend fun getLockerDetails(lockerUnitMac: String): RLockerUnit? {
        return wrapAwaitData(
            call = AdminAppService.service.getLockerDetails(lockerUnitMac.macRealToClean()),
            methodName = "getLockerDetails()"
        )
    }

    suspend fun getMasterDetails(masterMac: String): RMasterUnit? {
        log.info("Calling backend method getMasterDetails for only one master")
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.getMasterDetails(masterMac.macRealToClean()),
            methodName = "getLockerDetails()"
        )
    }

    suspend fun getLockerMacAddresses(): List<String>? {
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.getLockerMacAddresses(),
            methodName = "getLockerMacAddresses()"
        )
    }

    suspend fun getMasterUnits(): List<RMasterUnit>? {
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.getMasterUnits(),
            methodName = "getMasterUnits()",
            defaultNullValue = listOf()
        )
    }

    override fun callEncryptService(mac: String, request: REncryptRequest): Call<REncryptResponse> {
        return AdminAppService.service.encrypt(mac, request)
    }

    suspend fun getLanguages(): RWebLanguage? {
        return WSAdmin.wrapAwaitData(
            call = WebAppService.service.getLanguages(RPagination()),
            methodName = "getLanguages()"
        )
    }

    suspend fun getEPaperType(): List<REpaperType>? {
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.getEPaperType(),
            methodName = "getEPaperType()"
        )
    }

    suspend fun getMasterAccessRequests(): List<RMasterUnitAccessRequests>? {
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.getMasterAccessList(),
            methodName = "getMasterAccessList()"
        )
    }


    suspend fun getAssignedGroupsToEpaper(mac: String): List<RAssignedGroup>? {
        return wrapAwaitData(
            call = AdminAppService.service.getAssignedGroupsToEpaper(mac.macRealToClean()),
            methodName = "getAvailableEPaperPlaces()"
        )
    }

    suspend fun grantAccessToMaster(accessRequestId: Int, index: Int): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminAppService.service.grantAccessToMaster(accessRequestId, index),
            methodName = "grantAccessToMaster()"
        )
    }

    suspend fun assignGroupToEpaper(mac: String, index: Int, groupId: Int): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminAppService.service.assignGroupToEpaper(mac, index, groupId),
            methodName = "assignGroupToEpaper()"
        )
    }

    suspend fun unAssignMasterFromEpaper(mac: String, index: Int): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminAppService.service.unAssignMasterFromEpaper(mac, index),
            methodName = "unAssignMasterFromEpaper()"
        )
    }

    suspend fun rejectAccessToMaster(accessRequestId: Int): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminAppService.service.rejectAccessToMaster(accessRequestId),
            methodName = "rejectAccessToMaster()"
        )
    }

    suspend fun updateUserData(userData: RGroupNameUpdate): Boolean {
        return WSAdmin.wrapAwaitIsSuccessful(
            call = AdminAppService.service.updateNameAndGroupName(userData),
            methodName = "updateUserData()"
        )
    }

    suspend fun deleteUserFromSystem(userData: RDeleteUser): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminAppService.service.deleteEndUserFromSystem(userData),
            methodName = "updateUserData()"
        )
    }

    suspend fun activateUserFromSystem(userData: RDeactiateActivateUser): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminAppService.service.activateEndUserFromSystem(userData),
            methodName = "activateUserFromSystem()"
        )
    }

    suspend fun deactivateUserFromSystem(userData: RDeactiateActivateUser): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminAppService.service.deactivateEndUserFromSystem(userData),
            methodName = "deactivateUserFromSystem()"
        )
    }

    suspend fun getNetworkConfigurations(): List<RNetworkConfiguration>? {
        return wrapAwaitData(
            call = AdminAppService.service.getNetworkConfigurations(),
            methodName = "getNetworkConfigurations()"
        )
    }


    suspend fun updateUserProfile(
        user: RUpdateAdminInfo
    ): RAdminUserInfo? {

        return wrapAwaitData(
            call = AdminAppService.service.updateUserProfile(user),
            methodName = "updateUserProfile()"
        )
    }

    suspend fun requestPasswordRecovery(email: String): Boolean {
        return wrapAwaitIsSuccessful(
            call = AdminPublicService.service.requestPasswordRecovery(email, "sil2017"),
            methodName = "requestPasswordRecovery()"
        )
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val request = RUpdatePasswordRequest().apply {
            this.oldPassword = oldPassword
            this.newPassword = newPassword
        }
        return wrapAwaitIsSuccessful(
            call = AdminPublicService.service.updatePassword(request),
            methodName = "updatePassword()"
        )
    }

    suspend fun resetPassword(email: String, passwordCode: String, password: String): Boolean {
        val request = RResetPasswordRequest().apply {
            this.email = email
            this.passwordCode = passwordCode
            this.password = password
        }
        return wrapAwaitIsSuccessful(
            call = AdminPublicService.service.resetPassword(request),
            methodName = "resetPassword()"
        )
    }

    suspend fun forceOpen(slaveMacAddress: String): Boolean {
        return WSAdmin.wrapAwaitIsSuccessful(
            call = AdminAppService.service.forceOpenCloudLocker(slaveMacAddress.macRealToClean()),
            methodName = "forceOpenCloudLocker()"
        )
    }

    suspend fun deleteAllKeysOnLinuxDevice(slaveMacAddress: String): List<RLinuxDeleteAllKeysResponse>? {
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.deleteAllKeysOnLinuxDevice(slaveMacAddress.macRealToClean()),
            methodName = "deleteallKeysOnLinuxDevice()"
        )
    }

    suspend fun updateCLoudLockerData(updateLockerData: RLinuxSizeReducedMobilityRequest): RLockerUnit? {
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.updateCloudLockerData(updateLockerData),
            methodName = "updateCloudLockerData()"
        )
    }

    suspend fun deleteAllExpiredKeysOnLinuxDevice(slaveMacAddress: String): List<RLinuxDeleteAllKeysResponse>? {
        return WSAdmin.wrapAwaitData(
            call = AdminAppService.service.deleteAllExpiredKeysOnLinuxDevice(slaveMacAddress.macRealToClean()),
            methodName = "deleteallExpiredKeysOnLinuxDevice()"
        )
    }

    suspend fun invalidateKeyCloudLocker(slaveMacAddress: String): Boolean {
        return WSAdmin.wrapAwaitIsSuccessful(
            call = AdminAppService.service.invalitedKeyCloudLocker(slaveMacAddress.macRealToClean()),
            methodName = "invalidateKeyCloudLocker()"
        )
    }

    suspend fun modifyLinuxDevice(request: RLinuxSettingsRequest): RMasterUnit? {
        return wrapAwaitData(
            call = AdminAppService.service.modifyLinuxDevice(request),
            methodName = "modifyLinuxDevice()"
        )
    }

    suspend fun deleteKeyOnLocker(slaveMacAddress: String): Boolean? {

        val requestDeleteKey = RDeleteKeyOnLocker()
        requestDeleteKey.mac = slaveMacAddress.macRealToClean()
        return WSAdmin.wrapAwaitIsSuccessful(
            call = AdminAppService.service.deleteKeyOnLocker(requestDeleteKey),
            methodName = "deleteKeyOnLocker()"
        )
    }

    suspend fun deleteAllKeysOnLocker(masterMacAddress: String): Boolean? {

        val requestDeleteKey = RDeleteKeyOnLocker()
        requestDeleteKey.mac = masterMacAddress.macRealToClean()
        return WSAdmin.wrapAwaitIsSuccessful(
            call = AdminAppService.service.deleteAllKeysOnLocker(requestDeleteKey),
            methodName = "deleteKeyOnLocker()"
        )
    }

    suspend fun getMessageLog(): RMessageLog? {
        val limit = 100
        val offset = 0
        return wrapAwaitData(
            call = AdminAppService.service.getMessageLog(limit, offset),
            methodName = "getMessageLog()"
        )
    }

    suspend fun getMessageLogForDataCache(): List<RMessageDataLog>? {
        val limit = 100
        val offset = 0
        return wrapAwaitData(
            call = AdminAppService.service.getMessageLogForDataCache(limit, offset),
            methodName = "getMessageLog()"
        )
    }

    suspend fun getMessageDataForThisID(id: Int): String? {
        return wrapAwaitData(
            call = AdminAppService.service.getMessageDataForThisId(id),
            methodName = "getMessageLog()"
        )
    }


    suspend fun deleteMessageItem(itemId: Int): Void? {
        return wrapAwaitData(
            call = AdminAppService.service.deleteMessageItem(itemId),
            methodName = "deleteMessageById()"
        )
    }

    suspend fun deleteAll(): Void? {
        return wrapAwaitData(
            call = AdminAppService.service.deleteAllMessages(),
            methodName = "deleteAllMessages()"
        )
    }

}