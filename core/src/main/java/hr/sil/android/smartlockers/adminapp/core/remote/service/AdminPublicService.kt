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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author mfatiga
 */
interface AdminPublicService {

    companion object : RestServiceAccessor<AdminPublicService>(AdminPublicService::class) {
        //auth: Basic
        private const val ENDPOINT_PREFIX = "service/rest/adminApp/"
    }


    @POST(ENDPOINT_PREFIX + "device/register")
    fun registerDevice(@Body deviceInfo: RUserDeviceInfo): Call<RUserDeviceInfo>

    @GET(ENDPOINT_PREFIX + "activity")
    fun login(): Call<RAdminUserInfo>


    @GET(ENDPOINT_PREFIX + "getApiKey/{mac}/{challenge}")
    fun getDeviceApiKey(@Path("mac") cleanDeviceBleMac: String,
                        @Path("challenge") challenge: String): Call<REncryptResponse>

    @POST(ENDPOINT_PREFIX + "{mac}/encrypt")
    fun encrypt(@Path("mac") mac: String,
                @Body encryptRequest: REncryptRequest): Call<REncryptResponse>


    @GET(ENDPOINT_PREFIX + "endUser/recoverPassword/{email}{password}")
    fun requestPasswordRecovery(@Path("email") email: String, @Path("password") password: String): Call<Void>



    @POST(ENDPOINT_PREFIX + "endUser/resetPassword")
    fun resetPassword(@Body resetPasswordRequest: RResetPasswordRequest): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/updatePassword")
    fun updatePassword(@Body updatePasswordRequest: RUpdatePasswordRequest): Call<Void>




}


