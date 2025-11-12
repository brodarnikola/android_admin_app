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

package hr.sil.android.smartlockers.adminapp.cache

import hr.sil.android.datacache.AutoCache
import hr.sil.android.datacache.TwoLevelCache
import hr.sil.android.datacache.updatable.CacheSource
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.cache.dto.CRegistration
import hr.sil.android.smartlockers.adminapp.cache.dto.KeyStatus
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.RMasterUnitWithAllRegisteredSlaves
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * @author szuzul
 */
object DataCache {

    val log = logger()

    private val registeredSlavesInBackend by lazy {
        AutoCache.Builder(
            TwoLevelCache
                .Builder(String::class, { this }).classGroup("slaves")
                .memoryLruMaxSize(1000)
                .build(App.ref))
            .enableNetworkChecking(App.ref)
            .setFullSource(CacheSource.ForCache.Suspendable(5, TimeUnit.MINUTES) { _ ->
                WSAdmin.getLockerMacAddresses() ?: listOf()
            })
            .build()
    }

    private val masterUnitsCache by lazy {
        AutoCache.Builder(
            TwoLevelCache
                .Builder(RMasterUnitWithAllRegisteredSlaves::class, { masterUnit.mac })
                .memoryLruMaxSize(2000)
                .build(App.ref))
            .enableNetworkChecking(App.ref)
            .setSingleElementSource(CacheSource.ForKey.Suspendable(10, TimeUnit.MINUTES) { masterMac, _ ->
                val masterUnitDetails = WSAdmin.getMasterDetails(masterMac)
                    ?: RMasterUnit()

                RMasterUnitWithAllRegisteredSlaves().apply {
                    masterUnit = masterUnitDetails
                    lockerUnits = WSAdmin.getLockers(masterMac)?.toList() ?: listOf()
                    log.info("Size of locker units is: ${lockerUnits.size}")
                }
            })
            .setFullSource(CacheSource.ForCache.Suspendable(2, TimeUnit.HOURS) { _ ->
                val masterUnitWithSlaves = WSAdmin.getMasterUnits() ?: listOf()

                masterUnitWithSlaves.map {
                    RMasterUnitWithAllRegisteredSlaves().apply {
                        masterUnit = it
                        lockerUnits = listOf<RLockerUnit>()
                    }
                }
            })
            .build()
    }

    private val alarmsMessageCache by lazy {
        AutoCache.Builder(
            TwoLevelCache
                .Builder(RMessageDataLog::class, { id })
                .memoryLruMaxSize(100)
                .build(App.ref))
            .enableNetworkChecking(App.ref)
            .setFullSource(CacheSource.ForCache.Suspendable(5, TimeUnit.MINUTES) { alarmMessageLog ->
                WSAdmin.getMessageLogForDataCache()
            })
            .build()
    }

    private val ePaperType by lazy {
        AutoCache.Builder(
            TwoLevelCache
                .Builder(REpaperType::class, REpaperType::id)
                .memoryLruMaxSize(1000)
                .build(App.ref))
            .enableNetworkChecking(App.ref)
            .setFullSource(CacheSource.ForCache.Suspendable(1, TimeUnit.HOURS) { _ ->
                WSAdmin.getEPaperType()
            })
            .build()
    }

    private val languagesCache by lazy {
        AutoCache.Builder(
            TwoLevelCache
                .Builder(RLanguage::class, RLanguage::id)
                .memoryLruMaxSize(10)
                .build(App.ref))
            .enableNetworkChecking(App.ref)
            .setFullSource(CacheSource.ForCache.Suspendable(1, TimeUnit.HOURS) { _ ->
                WSAdmin.getLanguages()?.data
            })
            .build()
    }

    fun clearCaches() {
        languagesCache.clear()
        masterUnitsCache.clear()
        alarmsMessageCache.clear()
        registeredSlavesInBackend.clear()
    }

    suspend fun preloadCaches() {
        getLanguages(true)
        getMasterUnits(true)
        getRegisteredSlavesInBackend(true)
    }

    private val registrationStatusDb by lazy {
        AutoCache.Builder(TwoLevelCache
            .Builder(CRegistration::class, CRegistration::masterUnitMac)
            .memoryLruMaxSize(20)
            .build(App.ref)).setSingleElementSource(CacheSource.ForKey.Suspendable(10, TimeUnit.MINUTES) { mac, it ->
            null
        }).build()
    }

    private val keyStatusDb by lazy {
        AutoCache.Builder(TwoLevelCache
            .Builder(KeyStatus::class, KeyStatus::slaveMacAddress)
            .memoryLruMaxSize(20)
            .build(App.ref)).setSingleElementSource(CacheSource.ForKey.Suspendable(10, TimeUnit.MINUTES) { mac, it ->
            null
        }).build()
    }


    suspend fun getMasterUnits(awaitUpdate: Boolean = false): Collection<RMasterUnitWithAllRegisteredSlaves> =
        masterUnitsCache.getAll(awaitUpdate)

    suspend fun getMasterUnit(masterMac: String, awaitUpdate: Boolean = false): RMasterUnitWithAllRegisteredSlaves? =
        masterUnitsCache.get(masterMac, awaitUpdate)

    suspend fun getLanguages(awaitUpdate: Boolean = false): Collection<RLanguage> =
        languagesCache.getAll(awaitUpdate).filter { it.code == "EN" || it.code == "DE" || it.code=="FR"}

    suspend fun getAlarmMessageLog(awaitUpdate: Boolean = false): Collection<RMessageDataLog> =
        alarmsMessageCache.getAll(awaitUpdate)

    suspend fun getRegisteredSlavesInBackend(awaitUpdate: Boolean = false): Collection<String> =
        registeredSlavesInBackend.getAll(awaitUpdate)

    suspend fun getEPaperTypes(awaitUpdate: Boolean = false): Collection<REpaperType> =
        ePaperType.getAll(awaitUpdate)

    fun getRegistrationStatusDB(awaitUpdate: Boolean = false): Collection<CRegistration> {
        return runBlocking {
            registrationStatusDb.getAll(awaitUpdate)
        }
    }

    fun setRegistrationStatus(registrationItem: CRegistration) {
        registrationStatusDb.put(registrationItem)
    }

    fun removeRegistrationStatus(masterUnitMac: String) {
        registrationStatusDb.del(masterUnitMac)
    }



    fun setKeyStatus(keyItem: KeyStatus) {
        keyStatusDb.put(keyItem)
    }

    fun getKeyStatus(awaitUpdate: Boolean = false): Collection<KeyStatus> {
        return runBlocking {
            keyStatusDb.getAll(awaitUpdate)
        }
    }

    fun removeKeyStatus(slaveMacAddress: String) {
        keyStatusDb.del(slaveMacAddress)
    }



}


