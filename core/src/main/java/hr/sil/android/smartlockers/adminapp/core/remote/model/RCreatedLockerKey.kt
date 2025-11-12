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
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import java.util.*

/**
 * @author mfatiga
 */
class RCreatedLockerKey {
    var id: Int = 0
    var timeCreated: String = ""

    @SerializedName("locker___id")
    var lockerId: Int = 0

    @SerializedName("locker___mac")
    var lockerMac: String = ""

    var pin: String = ""

    @SerializedName("locker___master___id")
    var lockerMasterId: Int = 0

    @SerializedName("locker___master___mac")
    var lockerMasterMac: String = ""

    var purpose: RLockerKeyPurpose = RLockerKeyPurpose.UNKNOWN

    @SerializedName("createdBy___id")
    var createdById: Int? = null

    @SerializedName("createdBy___name")
    var createdByName: String? = null

    @SerializedName("createdForEndUser___id")
    var createdForId: Int? = null

    @SerializedName("createdForEndUser___name")
    var createdForEndUserName: String? = null

    @SerializedName("createdForEndUser___email")
    var createdForEndUserEmail: String? = null

    @SerializedName("basedOn___id")
    var baseId: Int? = null

    @SerializedName("basedOn___timeCreated")
    var baseTimeCreated: Date? = Date()

    @SerializedName("basedOn___createdForGroup___id")
    var baseGroupId: Int? = null

    @SerializedName("createdForGroup___name")
    var createdForGroupName: String = ""

    @SerializedName("basedOn___purpose")
    var basePurpose: String? = null

    @SerializedName("locker___size")
    var lockerSize: String? = null

    @SerializedName("locker___master___name")
    var masterName: String? = null

    @SerializedName("locker___master___address")
    var masterAddress: String? = null


    var isInBleProximity: Boolean = false

    fun getLockerBLEMacAddress(): String = lockerMac.macCleanToReal()

    fun getMasterBLEMacAddress(): String = lockerMasterMac.macCleanToReal()
}
