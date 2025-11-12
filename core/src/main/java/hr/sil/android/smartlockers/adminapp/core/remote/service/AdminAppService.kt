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

package hr.sil.android.smartlockers.adminapp.core.remote.service

import hr.sil.android.smartlockers.adminapp.core.model.RUpdateAdminInfo

import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.rest.core.factory.RestServiceAccessor
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * @author mfatiga
 */
interface AdminAppService {

    companion object : RestServiceAccessor<AdminAppService>(AdminAppService::class) {
        //auth: Basic
        private const val ENDPOINT_PREFIX = "service/rest/adminApp/"
    }


    @GET(ENDPOINT_PREFIX + "activity")
    fun login(): Call<RAdminUserInfo>

    @GET(ENDPOINT_PREFIX + "account/info")
    fun getAccountInfo(): Call<RAdminUserInfo>

    @GET(ENDPOINT_PREFIX + "getConfigurationData")
    fun getGlobalConfigurationData(): Call<RGlobalConfigurationData>

    @GET(ENDPOINT_PREFIX + "getApiKey/{mac}/{challenge}")
    fun getDeviceApiKey(@Path("mac") cleanDeviceBleMac: String,
                        @Path("challenge") challenge: String): Call<REncryptResponse>

    @POST(ENDPOINT_PREFIX + "{mac}/encrypt")
    fun encrypt(@Path("mac") mac: String,
                @Body encryptRequest: REncryptRequest): Call<REncryptResponse>


    @GET(ENDPOINT_PREFIX + "master/{mac}")
    fun getMasterDetails(@Path("mac") cleanDeviceBleMac: String): Call<RMasterUnit>

    @GET(ENDPOINT_PREFIX + "master/{mac}/lockers")
    fun getLockerForMaster(@Path("mac") cleanDeviceBleMac: String): Call<List<RLockerUnit>>

    @GET(ENDPOINT_PREFIX + "epd/{id}/preview")
    fun getEpdPreview(@Path("id") id: Long): Call<ResponseBody>

    @GET(ENDPOINT_PREFIX + "masterUnits")
    fun getMasterUnits(): Call<List<RMasterUnit>>

    @GET(ENDPOINT_PREFIX + "locker/{mac}")
    fun getLockerDetails(@Path("mac") cleanDeviceBleMac: String): Call<RLockerUnit>

    @GET(ENDPOINT_PREFIX + "registeredLockerMacs")
    fun getLockerMacAddresses(): Call<List<String>>

    @POST(ENDPOINT_PREFIX + "master/{mac}/modify")
    fun modifyMaster(@Path("mac") macAddress: String,
                     @Body masterDetails: RMasterUnitRequest): Call<RMasterUnit>


    @GET(ENDPOINT_PREFIX + "masterAccess/requests")
    fun getMasterAccessList(): Call<List<RMasterUnitAccessRequests>>

    @GET(ENDPOINT_PREFIX + "messageLog/{limit}/{offset}")
    fun getMessageLog(@Path("limit") limit: Int,
                      @Path("offset") offset: Int): Call<RMessageLog>

    @GET(ENDPOINT_PREFIX + "messageLog/{limit}/{offset}")
    fun getMessageLogForDataCache(@Path("limit") limit: Int,
                                  @Path("offset") offset: Int): Call<List<RMessageDataLog>>

    @GET(ENDPOINT_PREFIX + "messageLog/{id}/body")
    fun getMessageDataForThisId(@Path("id") id: Int): Call<String>

    @GET(ENDPOINT_PREFIX + "messageLog/delete/{id}")
    fun deleteMessageItem(  @Path("id") id: Int): Call<Void>

    @GET(ENDPOINT_PREFIX + "messageLog/delete")
    fun deleteAllMessages(): Call<Void>

    @POST(ENDPOINT_PREFIX + "master/freeLockers")
    fun deleteAllKeysOnLocker(@Body deleteKeysOnMasterUnit: RDeleteKeyOnLocker): Call<Void>

    @POST(ENDPOINT_PREFIX + "locker/freeLocker")
    fun deleteKeyOnLocker(@Body deleteKeyOnLocker: RDeleteKeyOnLocker): Call<Void>

    // cloud linux, smart rest api calls
    @PUT(ENDPOINT_PREFIX + "smart/{lockerMac}/force/open")
    fun forceOpenCloudLocker(@Path ("lockerMac") slaveMacAddress: String) : Call<Boolean>

    @PUT(ENDPOINT_PREFIX + "smart/keys/master/{masterMac}/invalidate")
    fun deleteAllKeysOnLinuxDevice(@Path ("masterMac") slaveMacAddress: String) : Call<List<RLinuxDeleteAllKeysResponse>>

    @PUT(ENDPOINT_PREFIX + "smart/locker/update")
    fun updateCloudLockerData(@Body updateCloudLockerData: RLinuxSizeReducedMobilityRequest) : Call<RLockerUnit>

    @PUT(ENDPOINT_PREFIX + "smart/keys/master/{masterMac}/pickupUncolected")
    fun deleteAllExpiredKeysOnLinuxDevice(@Path ("masterMac") slaveMacAddress: String) : Call<List<RLinuxDeleteAllKeysResponse>>

    @PUT(ENDPOINT_PREFIX + "smart/keys/locker/{lockerMac}/invalidate")
    fun invalitedKeyCloudLocker(@Path ("lockerMac") slaveMacAddress: String) : Call<Boolean>

    @PUT(ENDPOINT_PREFIX + "smart/master/update")
    fun modifyLinuxDevice(@Body linuxMasterDetails: RLinuxSettingsRequest) : Call<RMasterUnit>

    @GET(ENDPOINT_PREFIX + "masterAccess/grant/{accessRequestId}/{buttonIndex}")
    fun grantAccessToMaster(@Path("accessRequestId") accessRequestId: Int,
                            @Path("buttonIndex") buttonIndex: Int): Call<Void>

    @GET(ENDPOINT_PREFIX + "masterAccess/reject/{accessRequestId}")
    fun rejectAccessToMaster(@Path("accessRequestId") accessRequestId: Int): Call<Void>

    @GET(ENDPOINT_PREFIX + "master/{mac}/assignedGroups")
    fun getAssignedGroupsToEpaper(@Path("mac") mac: String): Call<List<RAssignedGroup>>

    @GET(ENDPOINT_PREFIX + "networkConfigurations")
    fun getNetworkConfigurations(): Call<List<RNetworkConfiguration>>

    @GET(ENDPOINT_PREFIX + "master/{mac}/unassign/{buttonIndex}")
    fun unAssignMasterFromEpaper(@Path("mac") mac: String,
                                 @Path("buttonIndex") buttonIndex: Int): Call<Void>


    @GET(ENDPOINT_PREFIX + "master/{mac}/assignGroup/{groupId}/{buttonIndex}")
    fun assignGroupToEpaper(@Path("mac") mac: String,
                            @Path("buttonIndex") buttonIndex: Int,
                            @Path("groupId") groupId: Int): Call<Void>

    @POST(ENDPOINT_PREFIX + "account/modify")
    fun updateUserProfile(@Body updateUserProfileRequest: RUpdateAdminInfo): Call<RAdminUserInfo>

    @GET(ENDPOINT_PREFIX + "epd/epdTypes")
    fun getEPaperType(): Call<List<REpaperType>>

    @POST (ENDPOINT_PREFIX + "/endUser/updateNameAndGroupName")
    fun updateNameAndGroupName(@Body groupName: RGroupNameUpdate): Call<Void>


    @POST(ENDPOINT_PREFIX + "endUser/hardDelete")
    fun deleteEndUserFromSystem(@Body userData: RDeleteUser): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/deactivate")
    fun deactivateEndUserFromSystem(@Body userData: RDeactiateActivateUser): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/activate")
    fun activateEndUserFromSystem(@Body userData: RDeactiateActivateUser): Call<Void>


}


