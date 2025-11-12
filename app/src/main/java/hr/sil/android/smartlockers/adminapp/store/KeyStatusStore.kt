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

package hr.sil.android.smartlockers.adminapp.store

import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.util.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author mfatiga
 */
object KeyStatusStore {
    private val log = logger()

    private const val UPDATE_PERIOD = 10000L

    private var isLockerAvailableGlobalVariable: Boolean = false

    fun run(slaveMacAddress: String, masterMacAddres: String, isLockerAvailable: Boolean) {
        isLockerAvailableGlobalVariable = isLockerAvailable
        log.info("Key purpose master mac address is: ${masterMacAddres}")

        GlobalScope.launch(Dispatchers.Default) {

            while (!isLockerAvailableGlobalVariable) {
                try {
                    handleUpdate(slaveMacAddress, masterMacAddres)
                } catch (ex: Exception) {
                    log.error("Periodic remote-update failed...", ex)
                }
                delay(UPDATE_PERIOD)
            }
        }
    }

    private suspend fun handleUpdate(
        slaveMacAddress: String,
        masterMacAddres: String
    ) {

        val result = WSAdmin.getLockerDetails(slaveMacAddress)
        isLockerAvailableGlobalVariable = result?.isAvailable ?: false
        if (isLockerAvailableGlobalVariable) {
            log.info("Deleting key, now it will go for new data(locker, keys and so on), for this master unit: ${slaveMacAddress}")
            MPLDeviceStore.forceRefreshMasterUnitInLockerSettings(masterMacAddres)
            MPLDeviceStoreRemoteUpdater.forceUpdate()
            DataCache.removeKeyStatus(slaveMacAddress)
            DataCache.getKeyStatus(true)
            log.info("It downloaded new data(lockers, keys and so on) for this master unit. Database size on mobile phone, size of: ${DataCache.getKeyStatus().size}, data from ${DataCache.getKeyStatus().joinToString { "-" }}")
        }
    }
}