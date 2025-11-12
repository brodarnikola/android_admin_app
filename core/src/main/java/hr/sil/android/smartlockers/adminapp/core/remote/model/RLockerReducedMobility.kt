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

/**
 * @author mfatiga
 */
enum class RLockerReducedMobility(val code: Byte?) {
    WITHOUT_MOBILITY_AND_WITHOUT_CLEANING_NEEDED(0x00.toByte()),
    ONLY_WITH_MOBILITY(0x01.toByte()),
    ONLY_WITH_CLEANING_NEEDED(0x02.toByte()),
    WITH_MOBILITY_AND_WITH_CLEANING_NEEDED(0x03.toByte());

    companion object {
        fun parse(code: Byte?) = values().firstOrNull { it.code == code } ?: WITHOUT_MOBILITY_AND_WITHOUT_CLEANING_NEEDED
    }
}