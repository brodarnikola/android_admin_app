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
import hr.sil.android.blecommunicator.impl.characteristics.streaming.BLEStreamingCharHolder
import hr.sil.android.blecommunicator.impl.characteristics.streaming.StreamingCommand
import hr.sil.android.smartlockers.adminapp.core.ble.comm.model.MPLGenericCommand
import hr.sil.android.smartlockers.adminapp.core.remote.base.WSBase
import hr.sil.android.smartlockers.adminapp.core.util.BLEScannerStateHolder
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToBytes
import kotlinx.coroutines.Dispatchers

/**
 * @author mfatiga
 */
abstract class BaseMPLCommunicator(
    ctx: Context,
    deviceAddress: String,
    bleScannerStateHolder: BLEScannerStateHolder,
    private val ws: WSBase
) : BaseBLECommunicator(
        ctx,
        deviceAddress,
        bleScannerStateHolder
) {
    companion object {
        private const val ENCRYPTION_ENABLED = true

        private const val CHALLENGE_SIZE = 4
        private val cmdReadChallenge = StreamingCommand(0xDE, 0xDA)

        private val cmdDiscoverSlave = StreamingCommand(0x04, 0x06)
        private val cmdGeneric = StreamingCommand(0x04, 0xAA)
    }

    // logger
    protected val log = logger()

    // characteristics holders
    protected val streaming = BLEStreamingCharHolder(handle)

    protected suspend fun readChallenge(): ByteArray? {
        val readResult = streaming.readArray(cmdReadChallenge, CHALLENGE_SIZE)
        return if (readResult.status && readResult.data.size == CHALLENGE_SIZE) {
            readResult.data
        } else null
    }

    protected suspend fun wrapEncryptData(data: ByteArray): ByteArray? {
        return if (ENCRYPTION_ENABLED) {
            val challenge = readChallenge()
            if (challenge != null) ws.encrypt(deviceAddress, challenge, data) else null
        } else data
    }

    suspend fun discoverSlave(slaveMacAddress: String): Boolean {
        val data = slaveMacAddress.macRealToBytes().reversedArray()
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDiscoverSlave, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    protected suspend fun sendGenericCommand(command: MPLGenericCommand, params: ByteArray = byteArrayOf()): Boolean {
        val data = byteArrayOf(command.code) + params
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdGeneric, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }
}