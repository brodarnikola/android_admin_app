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

import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceData
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceType
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplMaster
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplSlave
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplSlaveP16
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLModemStatus
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusHandler
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusType
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.MPLAppDeviceStatus
import hr.sil.android.smartlockers.adminapp.data.RLockerDataUiModel
import hr.sil.android.smartlockers.adminapp.events.MPLDevicesUpdatedEvent
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.store.model.MasterUnitWithLockers
import hr.sil.android.util.general.delegates.synchronizedDelegate
import hr.sil.android.util.general.extensions.hexToByteArray
import java.util.*

/**
 * @author mfatiga
 */
object MPLDeviceStore {
    var mDevices by synchronizedDelegate(mutableMapOf<String, MPLDevice>())

    private val MAC_ADDRESS_SMALLER_THEN_8_BYTES_OR_15_CHARACTERS = 15
    private val MAC_ADDRESS_7_BYTES_P16 = 7
    private val MAC_ADDRESS_6_BYTES_NORMAL_SLAVE = 12

    val devices: Map<String, MPLDevice>
        get() = mDevices.toMap()

    private var bleData by synchronizedDelegate(mapOf<String, BLEDevice<BLEDeviceData>>())
    fun updateFromBLE(bleDevices: List<BLEDevice<BLEDeviceData>>) {
        bleData = bleDevices.associateBy { it.deviceAddress.toUpperCase() }
        mergeData()
        notifyEvents(bleDevices.map { it.deviceAddress.toUpperCase() })
    }

    private var remoteData by synchronizedDelegate(mapOf<String, MasterUnitWithLockers>())

    fun updateFromRemote(remoteDevices: Collection<MasterUnitWithLockers>, propagateEvent: Boolean) {
        remoteData = remoteDevices.associateBy { it.masterUnit.mac.macCleanToReal() }
        mergeData()
        if (propagateEvent)
            notifyEvents(remoteDevices.map { it.masterUnit.mac.toUpperCase() })
    }

    suspend fun refreshMasterUnit(mac: String): MPLDevice? {

        val masterDetails = WSAdmin.getMasterDetails(mac)
        val lockersDetails = WSAdmin.getLockers(mac)

        log.info("Enabled bar code scanning: ${masterDetails?.barcodeScannerEnabled}, use external bar code scanner: ${masterDetails?.useExternalBarcodeScanner}")

        val mapItem = MPLDevice.create(mac, bleData[mac], MasterUnitWithLockers(masterDetails ?: RMasterUnit(),
            lockersDetails ?: listOf()
        ))

        val masterUnitWithAllRegisteredSlaves = DataCache.getMasterUnits().filter { it.masterUnit.mac == mac.macRealToClean() }.firstOrNull()
        if( masterUnitWithAllRegisteredSlaves != null ) {
            masterUnitWithAllRegisteredSlaves.lockerUnits = mapItem.slaveUnits
            masterUnitWithAllRegisteredSlaves.masterUnit = masterDetails ?: RMasterUnit()
        }

        log.info("MPLDeviceStore size of locker list is: ${mDevices[mac]?.slaveUnits?.size}")
        MPLDeviceStoreRemoteUpdater.forceUpdate()
        return mapItem
    }

    suspend fun forceRefreshMasterUnitInLockerSettings(mac: String): MPLDevice? {
        val item = DataCache.getMasterUnit(mac, true) ?: return null
        val mapItem = MPLDevice.create(mac, bleData[mac], MasterUnitWithLockers(item.masterUnit,  item.lockerUnits))
        mDevices[mac] = mapItem
        return mapItem
    }

    val log = logger()

    suspend fun getBleDataExceptMaster(actions: Collection<String>): List<RLockerDataUiModel> {

        log.info("Executing to get all unregistered slaves with actions: ${actions.joinToString { it + "-" } }")
        //val correctAllRegisteredSlaves = mutableListOf<String>()

        val allRegisteredLockerMacs = DataCache.getRegisteredSlavesInBackend()
            .map { it.macCleanToReal() }
            .toMutableList()
        log.info("Registered slaves on backend size: ${allRegisteredLockerMacs}")

        val correctAllRegisteredSlaves = mergeP16andLockersFromBackend(allRegisteredLockerMacs)
        log.info("Slaves registered on backend:" + correctAllRegisteredSlaves.joinToString("-") { it + " " })

        val bleSlaveLockers = filterAllNonRegisteredSlaves(correctAllRegisteredSlaves)
        log.info("size of bluebtooth unregistered slaves is: " + bleSlaveLockers.size)

        return getAllUnregisteredSlavesInBLEProximity(bleSlaveLockers, actions)
    }

    fun getRegisteredP16FromBluebtooth( slaveMac : String) : BLEDevice<BLEDeviceData>? {
        return bleData.values.filter {  (it.deviceAddress == slaveMac && it.data.deviceType == BLEDeviceType.MPL_SLAVE_P16) }.firstOrNull()
    }

    fun getAllRegisteredSlavesFromBackend() : List<BLEDevice<BLEDeviceData>> {
        return bleData.values.filter {  (it.data.deviceType == BLEDeviceType.MPL_SLAVE || it.data.deviceType == BLEDeviceType.MPL_SLAVE_P16) }
    }

    private fun filterAllNonRegisteredSlaves(correctAllRegisteredSlaves: List<String>) : List<BLEDevice<BLEDeviceData>> {
        return bleData.values.filter {  (it.data.deviceType == BLEDeviceType.MPL_SLAVE || it.data.deviceType == BLEDeviceType.MPL_SLAVE_P16)
                && it.deviceAddress !in correctAllRegisteredSlaves }
    }

    private fun mergeP16andLockersFromBackend(allRegisteredLockerMacs: MutableList<String>) : List<String> {
        val correctAllRegisteredSlaves = mutableListOf<String>()
        for( element in 0 until allRegisteredLockerMacs.size){
            val macAddress = allRegisteredLockerMacs[element]
            if( macAddress.macRealToClean().length < MAC_ADDRESS_SMALLER_THEN_8_BYTES_OR_15_CHARACTERS ) {
                when {
                    macAddress.macRealToClean().hexToByteArray().size == MAC_ADDRESS_7_BYTES_P16 ->
                        correctAllRegisteredSlaves.add(
                            macAddress.macRealToClean().take(MAC_ADDRESS_6_BYTES_NORMAL_SLAVE)
                                .macCleanToReal()
                        )
                    else -> correctAllRegisteredSlaves.add(macAddress)
                }
            }
        }
        return correctAllRegisteredSlaves
    }

    private fun mergeData() {
        val allKeys = (remoteData.keys + bleData.keys).distinct()
        mDevices = allKeys
                .associate { it to MPLDevice.create(it, bleData[it], remoteData[it]) }
            .filter { it.value.mplMasterDeviceStatus != MPLDeviceStatus.FACTORY_RESET_PENDING }
            .toList()
            .filter { it.second.mplMasterDeviceStatus != MPLDeviceStatus.FACTORY_RESET_PENDING }
            .sortedBy { it.second.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED && it.second.isInProximity && it.second.masterUnitId != -1  }
            .sortedBy { (it.second.mplMasterDeviceStatus == MPLDeviceStatus.UNREGISTERED || it.second.mplMasterDeviceStatus == MPLDeviceStatus.REGISTRATION_PENDING) && it.second.isInProximity }
            .sortedBy { it.second.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED && !it.second.isInProximity }
            .sortedBy { it.second.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED && it.second.isInProximity && it.second.masterUnitId == -1 }

            .toMap().toMutableMap()
        log.debug("Device list size: ${mDevices.size}")
    }

    private suspend fun getAllUnregisteredSlavesInBLEProximity(bleSlaveLockers: List<BLEDevice<BLEDeviceData>>, actions: Collection<String>) : List<RLockerDataUiModel> {
        return bleSlaveLockers.map { it ->

            val instanceKey = it.deviceAddress + ActionStatusType.PERIPHERAL_REGISTRATION
            val cachedAction = ActionStatusHandler.actionStatusDb.get(instanceKey)
            if (mDevices.containsKey(it.deviceAddress) && cachedAction != null) {
                log.debug("Registered - DELETE_PENDING:" + it.deviceAddress.macCleanToReal())

                val lockerSingleOrP16Version = if( it.data.properties is BLEAdvV2Base )
                    (it.data.properties as BLEAdvV2Base).firmwareVersion.value.toString() else ""
                log.info("Locker version is 222: ${lockerSingleOrP16Version}")
                log.info("Locker door status is 333: ${it.data.properties}")
                val bleProps = it.data.properties
                val doorStatusList = mutableListOf<Int>()
                when (bleProps) {
                    is BLEAdvMplSlave -> {
                        if( bleProps.doorStatus.value == 0 )
                            doorStatusList.add( 0 )
                        else
                            doorStatusList.add( 1 )
                    }
                    is BLEAdvMplSlaveP16 -> {
                        for( doorStatus in bleProps.doorStatus.value ?: arrayOf() ) {
                            if( doorStatus == false )
                                doorStatusList.add( 0 )
                            else
                                doorStatusList.add( 1 )
                        }
                    }
                }
                if( it.data.deviceType == BLEDeviceType.MPL_SLAVE ) {
                    RLockerDataUiModel(mac = it.deviceAddress, status = MPLAppDeviceStatus.INSERT_PENDING, isLockerInProximity = true, deviceType = RLockerType.NORMAL, keyPurpose = RLockerKeyPurpose.UNKNOWN,
                        createdByName = "", createdOnDate = "", deletingKeyInProgress = false, lockerSingleOrP16Version = lockerSingleOrP16Version, cleaningNeeded = RActionRequired.NULL, doorStatus = doorStatusList)
                }
                else {
                    RLockerDataUiModel(mac = it.deviceAddress, status = MPLAppDeviceStatus.INSERT_PENDING, isLockerInProximity = true, deviceType = RLockerType.P16, keyPurpose = RLockerKeyPurpose.UNKNOWN,
                        createdByName = "", createdOnDate = "", deletingKeyInProgress =  false, lockerSingleOrP16Version = lockerSingleOrP16Version, cleaningNeeded = RActionRequired.NULL, doorStatus = doorStatusList )
                }
            } else {

                val key = it.deviceAddress + ActionStatusType.PERIPHERAL_DEREGISTRATION
                if (actions.contains(key)) {
                    ActionStatusHandler.actionStatusDb.del(key)
                }

                val lockerSingleOrP16Version = if( it.data.properties is BLEAdvV2Base )
                    (it.data.properties as BLEAdvV2Base).firmwareVersion.value.toString() else ""
                log.info("Locker version is 1111: ${lockerSingleOrP16Version}")

                val bleProps = it.data.properties
                val doorStatusList = mutableListOf<Int>()
                when (bleProps) {
                    is BLEAdvMplSlave -> {
                        if( bleProps.doorStatus.value == 0 )
                            doorStatusList.add( 0 )
                        else
                            doorStatusList.add( 1 )
                    }
                    is BLEAdvMplSlaveP16 -> {
                        for( doorStatus in bleProps.doorStatus.value ?: arrayOf() ) {
                            if( doorStatus == false )
                                doorStatusList.add( 0 )
                            else
                                doorStatusList.add( 1 )
                        }
                    }
                }
                if( it.data.deviceType == BLEDeviceType.MPL_SLAVE )
                    RLockerDataUiModel(mac = it.deviceAddress, status = MPLAppDeviceStatus.UNREGISTERED, isLockerInProximity = true, deviceType = RLockerType.NORMAL,
                        keyPurpose = RLockerKeyPurpose.UNKNOWN, createdByName = "", createdOnDate = "", deletingKeyInProgress = false,
                        lockerSingleOrP16Version = lockerSingleOrP16Version, cleaningNeeded = RActionRequired.NULL, doorStatus = doorStatusList)
                else
                    RLockerDataUiModel(mac = it.deviceAddress, status = MPLAppDeviceStatus.UNREGISTERED, isLockerInProximity = true, deviceType = RLockerType.P16,
                        keyPurpose = RLockerKeyPurpose.UNKNOWN, createdByName = "", createdOnDate = "", deletingKeyInProgress = false,
                        lockerSingleOrP16Version = lockerSingleOrP16Version, cleaningNeeded = RActionRequired.NULL, doorStatus = doorStatusList)
            }
        }.toList()
    }

    private fun notifyEvents(macList: List<String>) {
        App.ref.eventBus.post(MPLDevicesUpdatedEvent(macList))
    }

    fun clear() {
        bleData = mapOf()
        mDevices = mutableMapOf()
        remoteData = mapOf()
    }
}