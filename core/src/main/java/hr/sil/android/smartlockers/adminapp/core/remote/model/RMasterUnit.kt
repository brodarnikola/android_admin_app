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

package hr.sil.android.smartlockers.adminapp.core.remote.model

import com.google.gson.annotations.SerializedName

/**
 * @author mfatiga
 */
class RMasterUnit {
    var id: Int = 0
    var mac: String = ""
    var type: RMasterUnitType = RMasterUnitType.UNKNOWN
    var installationType: InstalationType = InstalationType.DEVICE
    //var installationType: String = ""
    var name: String = ""
    var address: String = ""

    @SerializedName("address___street")
    var addressStreet: String = ""

    @SerializedName("address___houseNumber")
    var addressHouseNumber: String = ""

    @SerializedName("address___postcode")
    var addressPostcode: String = ""

    @SerializedName("address___town")
    var addressTown: String = ""

    @SerializedName("address___country")
    var addressCountry: String = ""
    //var accessType: RMasterUnitAccessType? = null
    var accessTypes: List<RMasterUnitAccessType> = listOf()
    var allowPinSave: Boolean = false
    var powerType: RPowerTypeEnum = RPowerTypeEnum.BATTERY
    var modemSleepTime: Long = 60

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    @SerializedName("stmFirmware___version")
    var stmFirmwareVersion: String = ""

    @SerializedName("networkConfiguration___id")
    var networkConfigurationId: Int = 0

    @SerializedName("epdType___id")
    var ePaperTypeId: Int = 0

    var alertsEnabled: Boolean = false
    var barcodeScannerEnabled: Boolean = false
    var useExternalBarcodeScanner: Boolean = false

    var supportsDoorbell: Boolean = false

    @SerializedName("customer___deliveryExpirationDays")
    var customerDeliveryExpirationDays: Int = 7

    var pahEnabled: Boolean = true
    var endUserIdenfiticationEnabled: Boolean = true

    var publicDevice: Boolean = true
}