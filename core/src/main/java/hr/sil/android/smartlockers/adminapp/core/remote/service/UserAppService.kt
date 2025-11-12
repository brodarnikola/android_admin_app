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

import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.rest.core.factory.RestServiceAccessor
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author mfatiga
 */
interface UserAppService {
    companion object : RestServiceAccessor<UserAppService>(UserAppService::class) {
        //auth: Basic
        private const val ENDPOINT_PREFIX = "app/service/rest/"
    }

    @POST(ENDPOINT_PREFIX + "device/register")
    fun registerDevice(@Body deviceInfo: RUserDeviceInfo): Call<RUserDeviceInfo>

    @GET(ENDPOINT_PREFIX + "languages")
    fun getLanguages(): Call<List<RLanguage>>

    @POST(ENDPOINT_PREFIX + "endUser/register")
    fun registerEndUser(@Body endUserInfo: REndUserRegisterRequest): Call<REndUserInfo>

    @GET(ENDPOINT_PREFIX + "endUser/recoverPassword/{email}")
    fun requestPasswordRecovery(@Path("email") email: String): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/resetPassword")
    fun resetPassword(@Body resetPasswordRequest: RResetPasswordRequest): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/updatePassword")
    fun updatePassword(@Body updatePasswordRequest: RUpdatePasswordRequest): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/update")
    fun updateUserProfile(@Body updateUserProfileRequest: RUpdateUserProfileRequest): Call<REndUserInfo>

    @GET(ENDPOINT_PREFIX + "endUser/info")
    fun getUserInfo(): Call<REndUserInfo>

    @GET(ENDPOINT_PREFIX + "masterUnit/{masterId}/generatePin")
    fun getGeneratedPinFromBackendForSendParcel(@Path("masterId") masterId: Int): Call<String>

    @GET(ENDPOINT_PREFIX + "group")
    fun getGroupInfo(): Call<REndUserGroupInfo>

    @GET(ENDPOINT_PREFIX + "groupMembers")
    fun getGroupMembers(): Call<List<REndUserGroupMember>>

    @GET(ENDPOINT_PREFIX + "groupMemberships")
    fun getGroupMemberships(): Call<List<RGroupInfo>>



    @GET(ENDPOINT_PREFIX + "groupMembers/{id}")
    fun getGroupMembershipsById(@Path("id") id: Long): Call<MutableList<RGroupInfo>>


    @POST(ENDPOINT_PREFIX + "group/rename")
    fun updateUserGroup(@Body updateUserGroupRequest: RUpdateUserGroupRequest): Call<REndUserGroupInfo>

    @GET(ENDPOINT_PREFIX + "masterUnits")
    fun getMasterUnits(): Call<List<RMasterUnit>>

    @GET(ENDPOINT_PREFIX + "keys")
    fun getActiveKeys(): Call<List<RLockerKey>>

    @GET(ENDPOINT_PREFIX + "pickAtFriend/activeCreated")
    fun getActivePaFCreatedKeys(): Call<List<RCreatedLockerKey>>

    @GET(ENDPOINT_PREFIX + "pickAtHome/activeCreated")
    fun getActivePaHCreatedKeys(): Call<MutableList<RCreatedLockerKey>>


    @POST(ENDPOINT_PREFIX + "{mac}/encrypt")
    fun encrypt(@Path("mac") mac: String,
                @Body encryptRequest: REncryptRequest): Call<REncryptResponse>

    @GET(ENDPOINT_PREFIX + "masterUnit/{id}/availableSizes")
    fun getAvailableLockerSizes(@Path("id") masterUnitId: Int): Call<List<RAvailableLockerSize>>


    @GET(ENDPOINT_PREFIX + "masterUnits/requestAccess/{mac}")
    fun requestAccess(@Path("mac") mac: String): Call<Void>

    @GET(ENDPOINT_PREFIX + "masterUnits/accessRequests")
    fun getActiveAccessRequests(): Call<List<RAccessRequest>>

    @GET(ENDPOINT_PREFIX + "masterUnit/{mac}")
    fun getLockerInfo(@Path("mac") mac: String): Call<RLockerInfo>


    @POST(ENDPOINT_PREFIX + "masterUnit")
    fun getLockersInfo(@Body userAccess: List<String>): Call<List<RLockerInfo>>


    @POST(ENDPOINT_PREFIX + "group/addUser")
    fun addUserAccess(@Body userAccess: RUserAccess): Call<Void>

    @POST(ENDPOINT_PREFIX + "group/removeUser")
    fun removeUserAccess(@Body userAccess: RUserRemoveAccess): Call<Void>

    @POST(ENDPOINT_PREFIX + "pickAtFriend/create")
    fun createPaF(@Body encryptRequest: RCreatePaf): Call<Void>

    @POST(ENDPOINT_PREFIX + "pickAtFriend/cancel")
    fun deletePaF(@Body encryptRequest: RDeletePaf): Call<Void>


    @GET("www.google.com")
    fun ping(): Call<Void>

    //SPL
    @GET(ENDPOINT_PREFIX + "spl/{mac}/activate")
    fun activateSpl(@Path("mac") mac: String): Call<Void>

    //SPL
    @GET(ENDPOINT_PREFIX + "spl/{mac}/deactivate")
    fun deactivateSpl(@Path("mac") mac: String): Call<Void>

    //SPL
    @POST(ENDPOINT_PREFIX + "spl/modify")
    fun modifySpl(@Body masterUnit: RMasterUnit): Call<RMasterUnit>



}