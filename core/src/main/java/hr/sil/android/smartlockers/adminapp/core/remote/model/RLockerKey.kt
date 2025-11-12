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
import java.util.*

/**
 * @author mfatiga
 */
class RLockerKey {
    var id: Int = 0
    var timeCreated: Date = Date()

    @SerializedName("locker___id")
    var lockerId: Int = 0

    @SerializedName("locker___mac")
    var lockerMac: String = ""

    @SerializedName("locker___master___id")
    var lockerMasterId: Int = 0

    @SerializedName("locker___master___mac")
    var lockerMasterMac: String = ""

    var purpose: RLockerKeyPurpose = RLockerKeyPurpose.UNKNOWN

    @SerializedName("createdBy___id")
    var createdById: Int? = null

    @SerializedName("createdBy___name")
    var createdByName: String? = null

    @SerializedName("locker___size")
    var lockerSize: String? = null

    @SerializedName("locker___master___name")
    var masterName: String? = null

    @SerializedName("locker___master___address")
    var masterAddress: String? = null


    @SerializedName("createdForEndUser___id")
    var createdForId: Int? = null

    @SerializedName("createdForEndUser___name")
    var createdForEndUserName: String? = null

    @SerializedName("createdForEndUser___email")
    var createdForEndUserEmail: String? = null


    @SerializedName("createdForGroup___id")
    var createdForGroup: Int? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RLockerKey

        if (id != other.id) return false
        if (timeCreated != other.timeCreated) return false
        if (lockerId != other.lockerId) return false
        if (lockerMac != other.lockerMac) return false
        if (lockerMasterId != other.lockerMasterId) return false
        if (lockerMasterMac != other.lockerMasterMac) return false
        if (purpose != other.purpose) return false
        if (createdById != other.createdById) return false
        if (createdByName != other.createdByName) return false
        if (lockerSize != other.lockerSize) return false
        if (masterName != other.masterName) return false
        if (masterAddress != other.masterAddress) return false
        if (createdForId != other.createdForId) return false
        if (createdForEndUserName != other.createdForEndUserName) return false
        if (createdForEndUserEmail != other.createdForEndUserEmail) return false
        if (createdForGroup != other.createdForGroup) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + timeCreated.hashCode()
        result = 31 * result + lockerId
        result = 31 * result + lockerMac.hashCode()
        result = 31 * result + lockerMasterId
        result = 31 * result + lockerMasterMac.hashCode()
        result = 31 * result + purpose.hashCode()
        result = 31 * result + (createdById ?: 0)
        result = 31 * result + (createdByName?.hashCode() ?: 0)
        result = 31 * result + (lockerSize?.hashCode() ?: 0)
        result = 31 * result + (masterName?.hashCode() ?: 0)
        result = 31 * result + (masterAddress?.hashCode() ?: 0)
        result = 31 * result + (createdForId ?: 0)
        result = 31 * result + (createdForEndUserName?.hashCode() ?: 0)
        result = 31 * result + (createdForEndUserEmail?.hashCode() ?: 0)
        result = 31 * result + (createdForGroup ?: 0)
        return result
    }
}
