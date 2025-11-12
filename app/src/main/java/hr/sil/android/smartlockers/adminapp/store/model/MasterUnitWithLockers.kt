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

import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerUnit
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnit
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMessageDataLog
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMessageLog

/**
 * @author mfatiga
 */
data class MasterUnitWithLockers(
    val masterUnit: RMasterUnit,
    val alarmsMessageLog: List<RMessageDataLog> = listOf(),
    var lockerUnits: List<RLockerUnit>
) {
    constructor(masterUnit: RMasterUnit, lockerUnits: List<RLockerUnit>) : this(masterUnit = masterUnit, lockerUnits = lockerUnits, alarmsMessageLog = listOf<RMessageDataLog>())
}