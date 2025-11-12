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

package hr.sil.android.smartlockers.adminapp.core.ble.comm

import android.content.Context
import hr.sil.android.blecommunicator.impl.characteristics.streaming.StreamingCommand
import hr.sil.android.smartlockers.adminapp.core.ble.comm.model.MPLGenericCommand
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.BLEScannerStateHolder
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.core.util.macRealToBytes
import hr.sil.android.util.general.extensions.hexToByteArray
import hr.sil.android.util.general.extensions.toByteArray
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * @author mfatiga
 */
class MPLAdminBLECommunicator(
    ctx: Context,
    deviceAddress: String,
    bleScannerStateHolder: BLEScannerStateHolder
) : BaseMPLCommunicator(
    ctx,
    deviceAddress,
    bleScannerStateHolder,
    WSAdmin
) {
    companion object {
        private val cmdCfgRegisterMaster = StreamingCommand(0x04, 0xF0)
        private val cmdCfgKeypadLayout= StreamingCommand(0x04, 0xFB)
        private val cmdCfgEPaperType= StreamingCommand(0x04, 0xAE)
        private val cmdCfgNetApnUrl = StreamingCommand(0x04, 0xF1)
        private val cmdCfgNetApnUser = StreamingCommand(0x04, 0xF2)
        private val cmdCfgNetApnPass = StreamingCommand(0x04, 0xF3)
        private val cmdCfgNetSimPin = StreamingCommand(0x04, 0xF4)
        private val cmdCfgNetBackendUrl = StreamingCommand(0x04, 0xF5)
        private val cmdCfgNetBackendApiKey = StreamingCommand(0x04, 0xF6)
        private val cmdCfgNetEnableRadioAccessTech = StreamingCommand(0x04, 0xF7)
        private val cmdCfgNetPlmn = StreamingCommand(0x04, 0xAB)
        private val cmdCfgSetBand = StreamingCommand(0x04, 0xF9)
        private val cmdDeleteDevice = StreamingCommand(0x04, 0xFC)
        private val cmdDeleteAllKeysOnMasterUnit = StreamingCommand(0x04, 0xFE)

        private val cmdDeleteKeyOnLocker = StreamingCommand(0x04, 0xFD)

        private val cmdDeleteAllExpiredKeysOnMasterUnit = StreamingCommand(0x04, 0xEE)

        private val cmdBarCodeConfiguration = StreamingCommand(0x04, 0xBC)

        //TODO adapt for 7 byte mac address.
        private val cmdRegisterSlave = StreamingCommand(0x04, 0x01)
        private val cmdRegisterSlaveP16 = StreamingCommand(0x04, 0x11)
        private val cmdRegisterSlaveP16ForCPL = StreamingCommand(0x04, 0x31)
        private val cmdDeregisterSlave = StreamingCommand(0x04, 0x02)

        private val cmdPowerSavingModeMPLSPL = StreamingCommand(0x04, 0xF8)
        private val cmdSystemRebootDevice = StreamingCommand(0x04, 0xAA)

        private val cmdLockerIsDirty = StreamingCommand(0x04, 0xC0)
    }

    private val MAC_ADDRESS_7_BYTE_LENGTH = 14
    private val MAC_ADDRESS_6_BYTE_LENGTH = 12
    private val MAC_ADDRESS_LAST_BYTE_LENGTH = 2

    // core access
    private suspend fun writeEncryptData(cmd: StreamingCommand, data: ByteArray): Boolean {
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmd, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    private suspend fun writeNetApnUrl(apnUrl: String): Boolean {
        return writeEncryptData(cmdCfgNetApnUrl, apnUrl.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetApnUser(apnUser: String): Boolean {
        return writeEncryptData(cmdCfgNetApnUser, apnUser.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetApnPass(apnPass: String): Boolean {
        return writeEncryptData(cmdCfgNetApnPass, apnPass.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetSimPin(simPin: String): Boolean {
        return writeEncryptData(cmdCfgNetSimPin, simPin.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetBackendUrl(backendUrl: String): Boolean {
        return writeEncryptData(cmdCfgNetBackendUrl, backendUrl.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetRadioAccessTechnology(enableHttps: Int): Boolean {
        return writeEncryptData(cmdCfgNetEnableRadioAccessTech, (enableHttps).toByteArray(1))
    }

    private suspend fun writeGlobalConfiguration(networkConfiguration: RNetworkConfiguration, type: MPLDeviceType?): Boolean {
        val configuration = WSAdmin.getGlobalConfigurationData() ?: return false

        val backendBaseUrl = if (type == MPLDeviceType.TABLET) configuration.backendBaseUrl else configuration.backendBaseCoapUrl
        val backendRadioAccessTechnology = configuration.backendRadioAccessTechnology

        if (backendBaseUrl != null) if (!writeNetBackendUrl(backendBaseUrl)) return false

        if (backendRadioAccessTechnology != null) if (!writeNetRadioAccessTechnology(networkConfiguration.modemRadioAccess.type)) return false
        log.info("Saving data: url: $backendBaseUrl RAT: ${networkConfiguration.modemRadioAccess.type} ")
        return true
    }

    private suspend fun writeNetBackendApiKey(): Boolean {
        val challenge = readChallenge()
        val encrypted = if (challenge != null) WSAdmin.getDeviceApiKey(challenge, deviceAddress) else null
        return if (encrypted != null) {
            if (streaming.writeArray(cmdCfgNetBackendApiKey, encrypted).status) {
                streaming.writeEmpty()
                true
            } else false
        } else {
            false
        }
    }

    private suspend fun writeMasterRegistration(customerId: Int): Boolean {
        val data = customerId.toByteArray(4)
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdCfgRegisterMaster, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    private suspend fun writeNetPlmn(plmn: String?): Boolean {
        val bytes = if (plmn.isNullOrBlank()) byteArrayOf(0xDE.toByte()) else plmn.toByteArray(StandardCharsets.US_ASCII)
        return writeEncryptData(cmdCfgNetPlmn, bytes)
    }


    suspend fun setBand(bandValue: Int?): Boolean {
        val band = if (bandValue == null) 11 else {
            log.info("Set band value to $bandValue")
            bandValue
        }
        return writeEncryptData(cmdCfgSetBand, band.toU8Bytes())
    }

    fun Int.toU8Bytes(): ByteArray {
        val bytes = ByteArray(4)
        val buf = ByteBuffer.allocate(4).putInt(this and 0x0000FFFF)
        buf.position(0)
        buf.get(bytes)
        return bytes.drop(3).toByteArray()
    }

    suspend fun writeNumOfSeconds(seconds: Long): Boolean {
        return writeEncryptData(cmdPowerSavingModeMPLSPL, seconds.toU32Bytes())
    }

    fun Long.toU32Bytes(): ByteArray {
        val bytes = ByteArray(8)
        val buf = ByteBuffer.allocate(8).putLong(this and 0xFFFFFFFFL)
        buf.position(0)
        buf.get(bytes)
        return bytes.drop(4).toByteArray()
    }

    suspend fun deleteExpiredKeysOnMasterUnit(slaveMacAddress: String): Boolean {

        val data: ByteArray = when {
            // this is for mpl with new lockers with p16
            slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                val first6ByteMacAddress = slaveMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH)
                val lastByteMacAddress = slaveMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH)
                first6ByteMacAddress.macCleanToReal().macRealToBytes().reversedArray() + lastByteMacAddress.hexToByteArray()
            }
            // this is for mpl with old lockers
            else -> slaveMacAddress.macCleanToReal().macRealToBytes().reversedArray()
        }
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDeleteAllExpiredKeysOnMasterUnit, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun forceOpenCombined(slaveMacAddress: String): Boolean {

        val data: ByteArray = when {
            // this is for mpl with new lockers with p16
            slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                val first6ByteMacAddress = slaveMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH)
                val lastByteMacAddress = slaveMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH)
                first6ByteMacAddress.macCleanToReal().macRealToBytes().reversedArray() + lastByteMacAddress.hexToByteArray()
            }
            // this is for mpl with old lockers
            else -> slaveMacAddress.macCleanToReal().macRealToBytes().reversedArray()
        }
        if( slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH ) {
            return sendGenericCommand(MPLGenericCommand.FORCE_OPEN_DOOR_P16, data)
        }
        else
            return sendGenericCommand(MPLGenericCommand.FORCE_OPEN_DOOR, data)
    }

    suspend fun deleteKeyOnLockerCombined(slaveMacAddress: String): Boolean {

        val data: ByteArray = when {
            // this is for mpl with new lockers with p16
            slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                val first6ByteMacAddress = slaveMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH)
                val lastByteMacAddress = slaveMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH)
                first6ByteMacAddress.macCleanToReal().macRealToBytes().reversedArray() + lastByteMacAddress.hexToByteArray()
            }
            // this is for mpl with old lockers
            else -> slaveMacAddress.macCleanToReal().macRealToBytes().reversedArray()
        }
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDeleteKeyOnLocker, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun writeSystemReboot(): Boolean {
        val data = byteArrayOf(0xB0.toByte(), 0x01.toByte())
        log.info("Data size is: ${data.joinToString { "-" }}")
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdSystemRebootDevice, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun writeNetworkConfiguration(networkConfiguration: RNetworkConfiguration,
                                          simPin: String?, ePaperType: EPaperType?, type: MPLDeviceType?): Boolean {
        val apnUrl = networkConfiguration.apnUrl
        val apnUser = networkConfiguration.apnUser
        val apnPass = networkConfiguration.apnPass
        val plmn = networkConfiguration.plmn

        if (!apnUrl.isNullOrBlank()) if (!writeNetApnUrl(apnUrl)) return false
        if (apnUser != null) if (!writeNetApnUser(apnUser)) return false
        if (apnPass != null) if (!writeNetApnPass(apnPass)) return false
        if (simPin != null) if (!writeNetSimPin(simPin)) return false

        if (!writeNetPlmn(plmn)) return false
        if (!setBand(networkConfiguration.band)) return false

        if( type == MPLDeviceType.MASTER && ePaperType != null && !writeEpaperType(ePaperType))  return false

        log.info("apnUrl  ${apnUrl}")
        log.info("apnUser  ${apnUser}")
        log.info("apnPass  ${apnPass}")
        log.info("Network configuration ${networkConfiguration.modemRadioAccess.type}")
        log.info("plmn configuration ${plmn}")
        log.info("band configuration ${networkConfiguration.band}")
        writeNetRadioAccessTechnology(networkConfiguration.modemRadioAccess.type)

        return true
    }

    suspend fun registerMaster(customerId: Int, networkConfiguration: RNetworkConfiguration,
                               simPin: String?,  type: MPLDeviceType?, keypadType: String,
                               ePaperType: EPaperType): Boolean {
        //reset registration
        if (!writeMasterRegistration(0)) return false

        if( type == MPLDeviceType.SPL_PLUS && !writeKeypadLayout(keypadType) ) return false

        //write network configuration
        if (!writeNetworkConfiguration(networkConfiguration, simPin, ePaperType, type)) return false

        //write global configuration
        if (!writeGlobalConfiguration(networkConfiguration, type)) return false

        //write api-key
        if (!writeNetBackendApiKey()) return false

        //run registration
        if (!writeMasterRegistration(customerId)) return false

        //success
        return true
    }

    suspend fun writeKeypadLayout(keypadType: String): Boolean {

        val data = when (keypadType) {
            "SPL" ->  byteArrayOf(KeypadEnum.SPL_KEYPAD.code)
            "MPL" ->  byteArrayOf(KeypadEnum.MPL_KEYPAD.code)
            else -> byteArrayOf(KeypadEnum.SPL_PLUS_KEYPAD.code)
        }

        log.info("keypad type is: " + keypadType + " data is: " + data + " data zero index: " + data.get(0))

        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdCfgKeypadLayout, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun writeEpaperType(ePaperType: EPaperType?): Boolean {

        val data = when (ePaperType) {
            EPaperType.NO_EPAPER  ->  byteArrayOf(EpaperTypeByteCode.NO_EPAPER.code)
            EPaperType.LOW_RESOLUTION  ->  byteArrayOf(EpaperTypeByteCode.LOW_RESOLUTION.code)
            else -> byteArrayOf(EpaperTypeByteCode.HIGH_RESOLUTION.code)
        }

        log.info("ePaper type is: " + ePaperType?.name + " data is: " + data + " data zero index: " + data.get(0))

        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdCfgEPaperType, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun writeConfiguration(enableCourierPinIdentificationByte: Byte, enableBarCodeScanningByte: Byte ): Boolean {
        log.info("Enabled courier pin identificaiton byte is: ${enableCourierPinIdentificationByte}, Enabled external barcode scanning is: ${enableBarCodeScanningByte}")
        val data = byteArrayOf(enableCourierPinIdentificationByte) + byteArrayOf(enableBarCodeScanningByte) //byteArrayOf(enabledBarcodeScanning, externalBarCodeScanning)
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdBarCodeConfiguration, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun registerSlaveSplPLus(slaveMacAddress: String, size: ByteArray ): Boolean {
        val data = slaveMacAddress.macRealToBytes().reversedArray() + size
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdRegisterSlaveP16, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun registerSlaveP16Locker(slaveMacAddress: String, size: ByteArray ): Boolean {
        val data = slaveMacAddress.macRealToBytes().reversedArray() + size
        log.info("Connection is done data is: ${data}")
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdRegisterSlaveP16, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun registerSlaveP16LockerForCPL(slaveMacAddress: String, size: ByteArray ): Boolean {
        val data = slaveMacAddress.macRealToBytes().reversedArray() + size
        log.info("Connection is done data is: ${data.joinToString { "\n" + it }}")
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdRegisterSlaveP16ForCPL, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun registerSlave(slaveMacAddress: String, size: RLockerSize): Boolean {
        val sizeCode = size.code ?: return false
        val data = slaveMacAddress.macRealToBytes().reversedArray() + byteArrayOf(sizeCode)
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdRegisterSlave, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun registerSlaveCPLBasel(slaveMacAddress: String, size: RLockerSize, reducedMobility: Byte): Boolean {
        val sizeCode = size.code ?: return false
        val data = slaveMacAddress.macRealToBytes().reversedArray() + byteArrayOf(sizeCode) + byteArrayOf(reducedMobility)
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdRegisterSlave, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun deregisterSlave(slaveMacAddress: String): Boolean {
        val data = slaveMacAddress.macRealToBytes().reversedArray()
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDeregisterSlave, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun lockerCleanOrDirty(byteArrayCleaningNeeded: ByteArray): Boolean {

        val encrypted = wrapEncryptData(byteArrayCleaningNeeded)
        if (encrypted != null) {
            if (streaming.writeArray(cmdLockerIsDirty, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun updateEpaper(forceDownload: Boolean): Boolean {
        val forceDownloadByte = if (forceDownload) 0x01.toByte() else 0x00.toByte()
        return sendGenericCommand(MPLGenericCommand.UPDATE_EPAPER, byteArrayOf(forceDownloadByte))
    }

    //TODO extend for P16
    suspend fun forceOpenDoor(slaveMacAddress: String): Boolean {
        var data: ByteArray
        when {
            // this is for mpl with new lockers with p16
            slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                val first6ByteMacAddress = slaveMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH)
                val lastByteMacAddress = slaveMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH)
                data = first6ByteMacAddress.macRealToBytes().reversedArray() + lastByteMacAddress.hexToByteArray()
            }
            // this is for mpl with old lockers
            else -> data = slaveMacAddress.macRealToBytes().reversedArray()
        }
        if( slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH ) {
            return sendGenericCommand(MPLGenericCommand.FORCE_OPEN_DOOR_P16, data)
        }
        else
            return sendGenericCommand(MPLGenericCommand.FORCE_OPEN_DOOR, data)
    }
    //TODO extend for P16
    suspend fun deleteDevice (): Boolean {
        val data = byteArrayOf(0x01.toByte())
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDeleteDevice, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun deleteAllKeysOnMasterUnit (): Boolean {
        val data = byteArrayOf(0x01.toByte())
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDeleteAllKeysOnMasterUnit, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun deleteKeyOnLocker (slaveMacAddress: String): Boolean {

        val data: ByteArray
        when {
            // this is for mpl with new lockers with p16
            slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                val first6ByteMacAddress = slaveMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH)
                val lastByteMacAddress = slaveMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH)
                data = first6ByteMacAddress.macRealToBytes().reversedArray() + lastByteMacAddress.hexToByteArray()
            }
            // this is for mpl with old lockers
            else -> data = slaveMacAddress.macRealToBytes().reversedArray()
        }
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDeleteKeyOnLocker, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

}
