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

package hr.sil.android.smartlockers.adminapp.store.model

import android.content.Context
import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceData
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.*
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLModemStatus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.core.ble.comm.MPLAdminBLECommunicator
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.util.general.extensions.lerpInDomain

/**
 * @author mfatiga
 */
class MPLDevice private constructor(
    val macAddress: String,
    // type
    val type: MPLDeviceType,
    val masterUnitType: RMasterUnitType,
    val installationType: InstalationType?,
    // from BLE
    val mplMasterDeviceStatus: MPLDeviceStatus,
    val mplMasterModemStatus: MPLModemStatus,
    val mplMasterModemQueueSize: Int,
    val modemRssi: Int,
    val bleRssi: Int?,
    val bleTxPower: Int?,
    val bleDistance: Double?,
    val batteryVoltage: Double?,
    val firmwareVersion: String,
    var slaveUnits: List<RLockerUnit>,
    val masterUnitId: Int?,
    val masterUnitMac: String?,
    val masterUnitName: String?,
    val masterUnitAddress: String,
    val masterUnitStreet: String,
    val masterUnitHouseNumber: String,
    val masterUnitPostcode: String,
    val masterUnitTown: String,
    val masterUnitCountry: String,
    val isInProximity: Boolean,
    val stmOrAppVersion: String?,
    val accessTypes: List<RMasterUnitAccessType>,
    val alarmsMessageLog: List<RMessageDataLog>,
    val allowPinSave: Boolean?,
    val powerSaving: Boolean?,
    val modemSleepTime: Long?,
    val keypadType: ParcelLockerKeyboardType,
    val latitude: Double,
    val longitude: Double,
    val networkConfigurationId: Int,
    val ePaperTypeId: Int,
    val alertsEnabled: Boolean,
    val barcodeScannerEnabled: Boolean,
    val useExternalBarcodeScanner: Boolean,
    val doorBellSupport: Boolean,
    var customerDeliveryExpirationDays: Int,
    var doorStatus: Int,
    val pahEnabled: Boolean = false,
    val endUserIdenfiticationEnabled: Boolean = false,
    val publicDevice: Boolean = false
) {
    companion object {
        val log = logger()

        fun create(macAddress: String, bleData: BLEDevice<BLEDeviceData>?, remoteData: MasterUnitWithLockers?): MPLDevice {
            // remote
            val id = remoteData?.masterUnit?.id ?: -1
            val installationType = remoteData?.masterUnit?.installationType
            val masterUnit = remoteData?.masterUnit
            val alarmsMessageLog = remoteData?.alarmsMessageLog
            var lockerUnits = remoteData?.lockerUnits ?: listOf() //listOf<RLockerUnit>()
            var accessTypes = listOf<RMasterUnitAccessType>()
            val customerDeliveryExpirationDays = remoteData?.masterUnit?.customerDeliveryExpirationDays
            val pahEnabled = remoteData?.masterUnit?.pahEnabled
            val endUserIdenfiticationEnabled = remoteData?.masterUnit?.endUserIdenfiticationEnabled
            val publicDevice = remoteData?.masterUnit?.publicDevice
            // ble

            var mplDeviceType = MPLDeviceType.UNKNOWN
            var mplMasterDeviceStatus = if (id != -1) MPLDeviceStatus.REGISTERED else MPLDeviceStatus.UNKNOWN
            var bleMasterUnitType: RMasterUnitType = RMasterUnitType.UNKNOWN
            var mplMasterModemStatus = MPLModemStatus.UNKNOWN
            var mplMasterModemQueueSize = 0
            var modemRssi = 0
            val bleProps = bleData?.data?.properties
            var keypadType = ParcelLockerKeyboardType.SPL_PLUS
            var doorStatus = 5

            var stmVer = ""
            var batteryVoltage: Double? = null
            val pinManagementEnabled = remoteData?.masterUnit?.allowPinSave
            val powerSavingEnabled = remoteData?.masterUnit?.powerType == RPowerTypeEnum.BATTERY
            val modemSleepTime = remoteData?.masterUnit?.modemSleepTime
            when (bleProps) {
                is BLEAdvMplMaster -> {
                    batteryVoltage = getBatteryVoltage(bleProps)
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    mplMasterModemStatus = bleProps.modemStatus.value ?: MPLModemStatus.UNKNOWN
                    mplMasterModemQueueSize = bleProps.modemQueue.value ?: 0
                    lockerUnits = remoteData?.lockerUnits ?: listOf()
                    mplDeviceType = MPLDeviceType.MASTER
                    modemRssi = (bleProps as? BLEAdvMplMaster?)?.modemRSSI?.value ?: 0
                    val stmVersion = (bleProps as? BLEAdvMplMaster?)?.stmFirmwareVersion?.value
                    stmVer = stmVersion.toString()
                    accessTypes = remoteData?.masterUnit?.accessTypes ?: listOf()
                    bleMasterUnitType = RMasterUnitType.MPL
                }
                is BLEAdvMplSlave -> {
                    batteryVoltage = bleProps.batteryVoltage.value
                    mplDeviceType = MPLDeviceType.SLAVE
                    doorStatus = bleProps.doorStatus.value ?: -1
                }
                is BLEAdvMplTablet -> {
                    mplDeviceType = MPLDeviceType.TABLET
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    bleMasterUnitType = RMasterUnitType.MPL
                    lockerUnits = remoteData?.lockerUnits ?: listOf()
                    val appVersion = bleProps.applicationVersion.value
                    stmVer = appVersion.toString()
                    accessTypes = remoteData?.masterUnit?.accessTypes ?: listOf()
                }

                is BLEAdvSpl -> {
                    mplDeviceType = MPLDeviceType.SPL
                    batteryVoltage = getBatteryVoltage(bleProps)
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    mplMasterModemStatus = bleProps.modemStatus.value ?: MPLModemStatus.UNKNOWN
                    mplMasterModemQueueSize = bleProps.modemQueue.value ?: 0
                    lockerUnits = listOf()
                    modemRssi = (bleProps as? BLEAdvSpl?)?.modemRSSI?.value ?: 0
                    val stmVersion = (bleProps as? BLEAdvSpl?)?.stmFirmwareVersion?.value
                    stmVer = stmVersion.toString()
                    accessTypes = remoteData?.masterUnit?.accessTypes ?: listOf()
                    bleMasterUnitType = RMasterUnitType.SPL
                }

                is BLEAdvSplPlus -> {
                    mplDeviceType = MPLDeviceType.SPL_PLUS
                    batteryVoltage = getBatteryVoltage(bleProps)
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    mplMasterModemStatus = bleProps.modemStatus.value ?: MPLModemStatus.UNKNOWN
                    mplMasterModemQueueSize = bleProps.modemQueue.value ?: 0
                    lockerUnits = remoteData?.lockerUnits ?: listOf()
                    modemRssi = (bleProps as? BLEAdvSplPlus?)?.modemRSSI?.value ?: 0
                    val stmVersion = (bleProps as? BLEAdvSplPlus?)?.stmFirmwareVersion?.value
                    stmVer = stmVersion.toString()
                    accessTypes = remoteData?.masterUnit?.accessTypes ?: listOf()
                    bleMasterUnitType = RMasterUnitType.SPL_PLUS
                    keypadType = bleProps.keyboardType.value ?: ParcelLockerKeyboardType.SPL_PLUS
                }
            }

            if( bleProps == null )
                stmVer = remoteData?.masterUnit?.stmFirmwareVersion.toString()

            return MPLDevice(
                macAddress = macAddress,
                type = mplDeviceType,
                installationType = installationType,

                alarmsMessageLog = alarmsMessageLog ?: listOf(),
                // from BLE
                mplMasterDeviceStatus = mplMasterDeviceStatus,
                mplMasterModemStatus = mplMasterModemStatus,
                mplMasterModemQueueSize = mplMasterModemQueueSize,
                bleRssi = bleData?.rssi,
                bleTxPower = bleData?.data?.txPower,
                bleDistance = bleData?.data?.distance,
                batteryVoltage = batteryVoltage,
                firmwareVersion = (bleData?.data?.properties as? BLEAdvV2Base?)?.firmwareVersion?.value.toString(),
                // from remote
                masterUnitType = masterUnit?.type ?: bleMasterUnitType,
                slaveUnits = lockerUnits,
                masterUnitId = masterUnit?.id ?: -1,
                masterUnitMac = masterUnit?.mac ?: "",
                masterUnitName = masterUnit?.name ?: "",
                masterUnitAddress = masterUnit?.address ?: "",
                masterUnitStreet = masterUnit?.addressStreet ?: "",
                masterUnitHouseNumber = masterUnit?.addressHouseNumber ?: "",
                masterUnitPostcode = masterUnit?.addressPostcode ?: "",
                masterUnitTown = masterUnit?.addressTown ?: "",
                masterUnitCountry = masterUnit?.addressCountry ?: "",
                isInProximity = bleData != null || masterUnit?.installationType == InstalationType.LINUX,
                modemRssi = modemRssi,
                stmOrAppVersion = stmVer,
                accessTypes = accessTypes,
                allowPinSave = pinManagementEnabled,
                powerSaving = powerSavingEnabled,
                modemSleepTime = modemSleepTime,
                keypadType = keypadType,
                latitude = remoteData?.masterUnit?.latitude ?: 0.0,
                longitude = remoteData?.masterUnit?.longitude ?: 0.0,
                networkConfigurationId = remoteData?.masterUnit?.networkConfigurationId ?: 0,
                ePaperTypeId = remoteData?.masterUnit?.ePaperTypeId ?: 0,
                alertsEnabled = remoteData?.masterUnit?.alertsEnabled ?: false,
                barcodeScannerEnabled = remoteData?.masterUnit?.barcodeScannerEnabled ?: false,
                useExternalBarcodeScanner = remoteData?.masterUnit?.useExternalBarcodeScanner ?: false,
                doorBellSupport = remoteData?.masterUnit?.supportsDoorbell ?: false,
                customerDeliveryExpirationDays = customerDeliveryExpirationDays ?: 25,
                doorStatus = doorStatus,
                pahEnabled = pahEnabled ?: true,
                endUserIdenfiticationEnabled = endUserIdenfiticationEnabled ?: true,
                publicDevice = publicDevice ?: true
            )
        }

        private fun getBatteryVoltage(bleProps: BLEAdvProperties): Double {
            when (bleProps) {
                is BLEAdvMplMaster -> {
                    val raw = bleProps.batteryRaw.value?.toDouble()?.lerpInDomain(0.0, 255.0, 0.0, 65535.0)
                        ?: 0.0
                    return 0.0003003 * raw - 0.2933806
                }
                is BLEAdvSpl -> {
                    val raw = bleProps.batteryRaw.value?.toDouble()?.lerpInDomain(0.0, 255.0, 0.0, 65535.0)
                        ?: 0.0
                    return 0.0003003 * raw - 0.2933806
                }
                is BLEAdvSplPlus -> {
                    val raw = bleProps.batteryRaw.value?.toDouble()?.lerpInDomain(0.0, 255.0, 0.0, 65535.0)
                        ?: 0.0
                    return 0.0003003 * raw - 0.2933806
                }
                else -> return 0.0
            }

        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as MPLDevice

        return (macAddress == other.macAddress &&

                // type
                type == other.type &&

                // from BLE
                mplMasterDeviceStatus == other.mplMasterDeviceStatus &&
                mplMasterModemStatus == other.mplMasterModemStatus &&
                mplMasterModemQueueSize == other.mplMasterModemQueueSize &&

                batteryVoltage == other.batteryVoltage &&
                firmwareVersion == other.firmwareVersion &&
                masterUnitId == other.masterUnitId &&
                masterUnitMac == other.masterUnitMac &&
                masterUnitName == other.masterUnitName &&
                masterUnitAddress == other.masterUnitAddress &&
                isInProximity == other.isInProximity &&
                stmOrAppVersion == other.stmOrAppVersion
                )


    }


    fun isDeviceAccessible(): Boolean {
        if (isInProximity) {
            return mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED
        } else {
            return accessTypes.filter {
                it.equals(RMasterUnitAccessType.BY_GROUP_OWNERSHIP) || it.equals(RMasterUnitAccessType.BY_ACTIVE_PAF_KEY)
                        || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN) || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_USER)
            }.isNotEmpty()
        }
    }

    fun hasRightsToShareAccess(): Boolean {

        if (accessTypes.filter { it.equals(RMasterUnitAccessType.BY_GROUP_OWNERSHIP) || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN) }.isNotEmpty()) {
            return true
        }
        return false
    }

    fun hasUserRightsOnSendParcelLocker(): Boolean {

        if (accessTypes.filter {
                it.equals(RMasterUnitAccessType.BY_GROUP_OWNERSHIP) || it.equals(RMasterUnitAccessType.BY_ACTIVE_PAF_KEY)
                        || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN)
            }.isNotEmpty()) {
            return true
        }
        return false
    }

    fun hasUserRightsOnEditParcelLocker(): Boolean {

        if (accessTypes.filter {
                it.equals(RMasterUnitAccessType.BY_GROUP_OWNERSHIP) || it.equals(RMasterUnitAccessType.BY_ACTIVE_PAF_KEY)
                        || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN)
            }.isNotEmpty()) {
            return true
        }
        return false
    }

    fun hasUserOnlyRightsToCollectParcel(): Boolean {

        if (accessTypes.filter {
                it.equals(RMasterUnitAccessType.BY_GROUP_OWNERSHIP) || it.equals(RMasterUnitAccessType.BY_ACTIVE_PAF_KEY)
                        || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN)
            }.isNotEmpty()) {
            return false
        }
        return true
    }

    fun hasUserRightsOnLocker(): Boolean {

        if (accessTypes.filter {
                it.equals(RMasterUnitAccessType.BY_GROUP_OWNERSHIP) || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN)
                        || it.equals(RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_USER) || it.equals(RMasterUnitAccessType.BY_ACTIVE_PAF_KEY)
            }.isNotEmpty()) {
            return true
        }
        return false
    }


    override fun hashCode(): Int {
        return macAddress.hashCode()
    }

    // util
    fun createBLECommunicator(context: Context): MPLAdminBLECommunicator {
        return MPLAdminBLECommunicator(context, macAddress, App.ref)
    }
}