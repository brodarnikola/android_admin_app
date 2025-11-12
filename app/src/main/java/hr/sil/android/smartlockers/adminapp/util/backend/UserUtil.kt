/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
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

package hr.sil.android.smartlockers.adminapp.util.backend

import com.google.firebase.messaging.FirebaseMessaging
import hr.sil.android.rest.core.util.UserHashUtil
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.model.RUpdateAdminInfo
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RAdminUserInfo
import hr.sil.android.smartlockers.adminapp.core.util.DeviceInfo
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.preferences.PreferenceStore
import hr.sil.android.smartlockers.adminapp.remote.WSConfig
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.util.awaitForResult

/**
 * @author mfatiga
 */
object UserUtil {
    private val log = logger()
    fun isUserLoggedIn() = (user != null)

    var user: RAdminUserInfo? = null
        private set

    fun getUserString(default: String = "--"): String {
        val loggedInUserName = user?.name ?: ""
        val loggedInUserEmail = user?.email ?: ""
        val result = when {
            loggedInUserName.isNotBlank() -> loggedInUserName
            loggedInUserEmail.isNotBlank() -> loggedInUserEmail
            else -> null
        }
        return result ?: default
    }

    private fun updateUserHash(username: String?, password: String?) {
        if (username != null && password != null && username.isNotEmpty() && password.isNotEmpty()) {
            PreferenceStore.userHash = UserHashUtil.createUserHash(username, password)
        } else {
            PreferenceStore.userHash = ""
        }
        WSConfig.updateAuthorizationKeys()
    }

    suspend fun login(username: String, password: String): Boolean {
        WSAdmin.registerDevice(FirebaseMessaging.getInstance().token.awaitForResult(), DeviceInfo.getJsonInstance())
        updateUserHash(username, password)
        return login(username)
    }

    suspend fun login(username: String?): Boolean {
        return if (!PreferenceStore.userHash.isNullOrBlank()) {
            val responseUser = WSAdmin.getAccountInfo()
            if (responseUser != null) {
                user = responseUser

                log.info("Logged user role is: ${responseUser.role}")

                SettingsHelper.usernameLogin = username
                log.info("User is logged in updating device and token...")

                DataCache.preloadCaches()
                val languagesList = DataCache.getLanguages()
                val languageData = languagesList.find { it.id == responseUser.languageId }
                if (languageData != null) {
                    SettingsHelper.languageName = languageData.code
                }

                MPLDeviceStoreRemoteUpdater.forceUpdate()
                return true
            } else {
                log.info("Response User is null or blank")
                updateUserHash(null, null)
                user = null
                false
            }
        } else {
            log.info("User hash is null or blank")
            updateUserHash(null, null)
            user = null
            false
        }
    }

    fun logout() {
        updateUserHash(null, null)
        user = null

        MPLDeviceStore.clear()
        DataCache.clearCaches()
    }

    suspend fun passwordUpdate(newPassword: String, oldPassword:String): Boolean {
        val isPasswordUpdated = WSAdmin.updatePassword(oldPassword, newPassword)

        if (!isPasswordUpdated ) {
            log.error("Error while updating the user password")
            return false
        } else {
            return true
        }
    }

    suspend fun passwordRecovery(email: String): Boolean {
        return WSAdmin.requestPasswordRecovery(email)
    }

    suspend fun passwordReset(email: String, passwordCode: String, password: String): Boolean {
        return WSAdmin.resetPassword(email, passwordCode, password)
    }

    suspend fun userUpdate(user: RUpdateAdminInfo): Boolean {
        if (WSAdmin.updateUserProfile(user) == null) {
            log.error("Error while updating the user")
            return false
        }else
            return true
    }
}