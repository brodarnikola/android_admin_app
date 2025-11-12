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
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.communicator.BLEAsyncCommunicator
import hr.sil.android.blecommunicator.core.model.BLEConnectionParameters
import hr.sil.android.blecommunicator.core.model.BLEConnectionPriority
import hr.sil.android.smartlockers.adminapp.core.util.BLEScannerStateHolder

/**
 * @author mfatiga
 */
abstract class BaseBLECommunicator(ctx: Context, protected val deviceAddress: String, private val bleScannerStateHolder: BLEScannerStateHolder) {
    protected val handle by lazy { BLECommDeviceHandle.create(ctx, deviceAddress) }

    // debug
    fun setDebugMode(enabled: Boolean, logThreadName: Boolean = false) {
        handle.setDebugMode(enabled = enabled, logThreadName = logThreadName)
    }

    private val connectionParameters = BLEConnectionParameters(
            retryCount = 4,
            retryBackoff = 1000L,
            attemptTimeout = 10000L,
            connectionTimeout = 20000L,
            discoverCharacteristicsTimeout = 10000L
    )

    // connection
    suspend fun connect(): Boolean {
        if (bleScannerStateHolder.isScannerStarted()) {
            bleScannerStateHolder.stopScannerAsync()
        }
        val result = handle.connect(connectionParameters)
        if (!result) {
            bleScannerStateHolder.startScanner()
        }
        return result
    }

    suspend fun requestMaxMTU() =
            handle.requestMTU(BLEAsyncCommunicator.GATT_MTU_MAXIMUM)

    suspend fun requestConnectionPriority(connectionPriority: BLEConnectionPriority) =
            handle.requestConnectionPriority(connectionPriority)

    fun isConnected(): Boolean = handle.isConnected()

    suspend fun disconnect(): Boolean {
        val result = handle.disconnect()
        if (!bleScannerStateHolder.isScannerStarted()) {
            bleScannerStateHolder.startScanner()
        }
        return result
    }
}