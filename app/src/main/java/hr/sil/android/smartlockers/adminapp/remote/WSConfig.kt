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

package hr.sil.android.smartlockers.adminapp.remote

import android.content.Context
import hr.sil.android.rest.core.configuration.ServiceConfig
import hr.sil.android.rest.core.configuration.parameters.model.Authorization
import hr.sil.android.smartlockers.adminapp.BuildConfig
import hr.sil.android.smartlockers.adminapp.core.remote.service.AdminAppService
import hr.sil.android.smartlockers.adminapp.core.remote.service.AdminPublicService
import hr.sil.android.smartlockers.adminapp.core.remote.service.WebAppService
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.preferences.PreferenceStore

/**
 * @author mfatiga
 */
object WSConfig {
    private val log = logger()

    fun initialize(applicationContext: Context) {
        log.info("Initializing web service configuration...")
        ServiceConfig.initialize(applicationContext)
        log.info("Web service configuration initialized, APP_KEY: ${ServiceConfig.cfg.appKey}")

        log.info("Configuring WSUser clients...")
        AdminAppService.config.setBaseURL(BuildConfig.API_BASE_URL, BuildConfig.API_CONTEXT)
        WebAppService.config.setBaseURL(BuildConfig.API_BASE_URL, BuildConfig.API_CONTEXT)
        AdminPublicService.config.setBaseURL(BuildConfig.API_BASE_URL, BuildConfig.API_CONTEXT)

        updateAuthorizationKeys()
    }

    fun updateAuthorizationKeys() {
        log.info("Updating authorization keys...")
        val authKey = PreferenceStore.userHash ?: ""
        log.info("Auth Key: " +authKey)
        AdminAppService.config.setAuthorization(Authorization.Basic(authKey))
        WebAppService.config.setAuthorization(Authorization.Basic(authKey))
    }
}