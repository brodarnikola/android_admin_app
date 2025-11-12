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

import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.util.logger
//import hr.sil.android.smartlockers.adminapp.events.DevicesUpdateEvent
import hr.sil.android.smartlockers.adminapp.store.model.MasterUnitWithLockers
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
object MPLDeviceStoreRemoteUpdater {
    private val log = logger()

    private const val UPDATE_PERIOD = 10000L

    private val mainLoaderRunning = AtomicBoolean(false)
    private val lockerLoaderRunning = AtomicBoolean(false)

    fun run() {
        if (mainLoaderRunning.compareAndSet(false, true)) {
            GlobalScope.launch(Dispatchers.Default) {
                while (true) {
                    try {
                        handleUpdate()
                    } catch (ex: Exception) {
                        log.error("Periodic remote-update failed...", ex)
                    }
                    delay(UPDATE_PERIOD)
                }
            }
        }
    }

    suspend fun forceUpdate(propagateEvent: Boolean = true) {
        handleUpdate(propagateEvent)
    }

    private val inHandleUpdate = AtomicBoolean(false)
    private suspend fun handleUpdate(propagateEvent: Boolean = true) {
        if (inHandleUpdate.compareAndSet(false, true)) {
            if (UserUtil.isUserLoggedIn()) {
                doUpdate(propagateEvent)
            }
            inHandleUpdate.set(false)
        }
    }

    private suspend fun doUpdate(propagateEvent: Boolean = true) {
        //val allActiveKeys = DataCache.getAlarmMessageLog(false)

        val masterUnitWithLockers =
            DataCache.getMasterUnits().map { masterUnitWithSlaves ->
                MasterUnitWithLockers(
                    masterUnit = masterUnitWithSlaves.masterUnit,
                    alarmsMessageLog = listOf() /*allActiveKeys.filter { lockerKey ->
                        lockerKey.master___mac == masterUnitWithSlaves.masterUnit.mac
                    },*/,
                    lockerUnits = masterUnitWithSlaves.lockerUnits
                )
            }
        MPLDeviceStore.updateFromRemote(masterUnitWithLockers, propagateEvent)
        //App.ref.eventBus.post(DevicesUpdateEvent(masterUnitWithLockers.map { it.masterUnit.mac }))
    }

    fun stopUpdateDevice() {
        lockerLoaderRunning.getAndSet(false)
    }
}