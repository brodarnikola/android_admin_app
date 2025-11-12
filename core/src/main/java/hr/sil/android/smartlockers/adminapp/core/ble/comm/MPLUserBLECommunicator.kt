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
import hr.sil.android.smartlockers.adminapp.core.ble.comm.model.BLEDoorOpenResult
import hr.sil.android.smartlockers.adminapp.core.ble.comm.model.MPLGenericCommand
import hr.sil.android.smartlockers.adminapp.core.remote.WSUser
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize
import hr.sil.android.smartlockers.adminapp.core.util.BLEScannerStateHolder
import hr.sil.android.smartlockers.adminapp.core.util.macRealToBytes
import hr.sil.android.util.general.extensions.hexToByteArray
import hr.sil.android.util.general.extensions.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * @author mfatiga
 */
class MPLUserBLECommunicator(
        ctx: Context,
        deviceAddress: String,
        bleScannerStateHolder: BLEScannerStateHolder
) : BaseMPLCommunicator(
        ctx,
        deviceAddress,
        bleScannerStateHolder,
        WSUser
) {
    companion object {
        private const val POLL_OPEN_DOOR_STATUS_TIMEOUT = 20_000L
        private const val POLL_OPEN_DOOR_STATUS_PERIOD = 500L
        private val cmdReadOpenDoorResult = StreamingCommand(0x04, 0x00)
        private val cmdParcelPickup = StreamingCommand(0x04, 0x03)
        private val cmdParcelSendCreate = StreamingCommand(0x04, 0x04)
        private val cmdParcelSendCancel = StreamingCommand(0x04, 0x05)
        private val cmdParcelPickupP16 = StreamingCommand(0x04, 0x13)
        private val cmdParcelSendCancelP16 = StreamingCommand(0x04, 0x15)
    }

    private val MAC_ADDRESS_7_BYTE_LENGTH = 14
    private val MAC_ADDRESS_6_BYTE_LENGTH = 12
    private val MAC_ADDRESS_LAST_BYTE_LENGTH = 2

    // core access
    private suspend fun readOpenDoorStatusCode(): Byte? {
        val resultSize = 1
        val readResult = streaming.readArray(cmdReadOpenDoorResult, resultSize)
        return if (readResult.status && readResult.data.size == resultSize) {
            readResult.data.first()
        } else {
            null
        }
    }

    private suspend fun pollOpenDoorStatus(
            timeout: Long = POLL_OPEN_DOOR_STATUS_TIMEOUT,
            period: Long = POLL_OPEN_DOOR_STATUS_PERIOD
    ): BLEDoorOpenResult.BLESlaveErrorCode? {
        var result: BLEDoorOpenResult.BLESlaveErrorCode? = null
        val start = System.currentTimeMillis()
        while ((System.currentTimeMillis() - start) < timeout) {
            //read status code or break if null
            val statusCode = readOpenDoorStatusCode() ?: break

            //when status code is set (not equal to 0xFF), set and break
            if (statusCode != 0xFF.toByte()) {
                result = BLEDoorOpenResult.BLESlaveErrorCode.parse(statusCode)
                break
            }

            //wait for period between checks
            delay(period)
        }
        return result
    }

    suspend fun requestParcelPickup(lockerBLEMac: String, endUserId: Int): BLEDoorOpenResult {
        //result
        var bleDeviceErrorCode: BLEDoorOpenResult.BLEDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.OK
        var bleSlaveErrorCode: BLEDoorOpenResult.BLESlaveErrorCode = BLEDoorOpenResult.BLESlaveErrorCode.NONE

        //parse parameters
        var data: ByteArray
        when {
            // this is for mpl with new lockers with p16
            lockerBLEMac.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                val first6ByteMacAddress = lockerBLEMac.take(MAC_ADDRESS_6_BYTE_LENGTH)
                val lastByteMacAddress = lockerBLEMac.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH)
                data = first6ByteMacAddress.macRealToBytes().reversedArray() + lastByteMacAddress.hexToByteArray() + endUserId.toByteArray(4)
            }
            // this is for mpl with old lockers
            else -> data = lockerBLEMac.macRealToBytes().reversedArray() + endUserId.toByteArray(4)
        }

        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {

            val cmdPickupParcel = when (lockerBLEMac.length) {
                MAC_ADDRESS_7_BYTE_LENGTH -> cmdParcelPickupP16
                else -> cmdParcelPickup
            }

            if (streaming.writeArray(cmdPickupParcel, encrypted).status) {
                streaming.writeEmpty()
                val openDoorStatus = pollOpenDoorStatus()
                if (openDoorStatus != null) {
                    bleSlaveErrorCode = openDoorStatus
                } else {
                    bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.READ_RESULT_FAILED
                }
            } else {
                bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.COMMAND_WRITE_FAILED
            }
        } else {
            bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.ENCRYPTION_FAILED
        }

        val result = BLEDoorOpenResult.create(bleDeviceErrorCode, bleSlaveErrorCode)
        log.info("OpenDoorResult: $result")
        return result
    }

    suspend fun requestParcelSendCreate(lockerSize: RLockerSize, endUserId: Int, pin: Int): BLEDoorOpenResult {
        //result
        var bleDeviceErrorCode: BLEDoorOpenResult.BLEDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.OK
        var bleSlaveErrorCode: BLEDoorOpenResult.BLESlaveErrorCode = BLEDoorOpenResult.BLESlaveErrorCode.NONE

        //parse parameters
        val lockerSizeCode = lockerSize.code
        if (lockerSizeCode != null && pin > 0 && pin <= 9999) {
            val pinBytes = pin.toByteArray(2)
            val data = byteArrayOf(lockerSizeCode) + endUserId.toByteArray(4) + pinBytes

            val encrypted = wrapEncryptData(data)
            if (encrypted != null) {
                if (streaming.writeArray(cmdParcelSendCreate, encrypted).status) {
                    streaming.writeEmpty()
                    val openDoorStatus = pollOpenDoorStatus()
                    if (openDoorStatus != null) {
                        bleSlaveErrorCode = openDoorStatus
                    } else {
                        bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.READ_RESULT_FAILED
                    }
                } else {
                    bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.COMMAND_WRITE_FAILED
                }
            } else {
                bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.ENCRYPTION_FAILED
            }
        } else {
            bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.INVALID_PARAMETERS
        }

        val result = BLEDoorOpenResult.create(bleDeviceErrorCode, bleSlaveErrorCode)
        log.info("OpenDoorResult: $result")
        return result
    }

    suspend fun forceOpenDoor(slaveMacAddress: String): Boolean {
        val params = slaveMacAddress.macRealToBytes().reversedArray()
        return sendGenericCommand(MPLGenericCommand.FORCE_OPEN_DOOR, params)
    }

    suspend fun requestParcelSendCancel(lockerBLEMac: String, endUserId: Int): BLEDoorOpenResult {
        //result
        var bleDeviceErrorCode: BLEDoorOpenResult.BLEDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.OK
        var bleSlaveErrorCode: BLEDoorOpenResult.BLESlaveErrorCode = BLEDoorOpenResult.BLESlaveErrorCode.NONE

        //parse parameters
        var data: ByteArray
        when {
            // this is for mpl with new lockers with p16
            lockerBLEMac.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                val first6ByteMacAddress = lockerBLEMac.take(MAC_ADDRESS_6_BYTE_LENGTH)
                val lastByteMacAddress = lockerBLEMac.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH)
                data = first6ByteMacAddress.macRealToBytes().reversedArray() + lastByteMacAddress.hexToByteArray() + endUserId.toByteArray(4)
            }
            // this is for mpl with old lockers
            else -> data = lockerBLEMac.macRealToBytes().reversedArray() + endUserId.toByteArray(4)
        }

        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {

            val cmdCancel = when (lockerBLEMac.length) {
                MAC_ADDRESS_7_BYTE_LENGTH -> cmdParcelSendCancelP16
                else -> cmdParcelSendCancel
            }

            if (streaming.writeArray(cmdCancel, encrypted).status) {
                streaming.writeEmpty()
                val openDoorStatus = pollOpenDoorStatus()
                if (openDoorStatus != null) {
                    bleSlaveErrorCode = openDoorStatus
                } else {
                    bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.READ_RESULT_FAILED
                }
            } else {
                bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.COMMAND_WRITE_FAILED
            }
        } else {
            bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.ENCRYPTION_FAILED
        }

        val result = BLEDoorOpenResult.create(bleDeviceErrorCode, bleSlaveErrorCode)
        log.info("OpenDoorResult: $result")
        return result
    }
}