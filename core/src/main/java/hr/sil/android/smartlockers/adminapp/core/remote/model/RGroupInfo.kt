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
 * @author szuzul
 */
class RGroupInfo {

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("group___id")
    var groupId: Int = 0

    @SerializedName("group___name")
    var groupName: String = ""

    @SerializedName("group___owner___id")
    var groupOwnerId: Long = 0

    @SerializedName("group___owner___name")
    var groupOwnerName: String = ""

    @SerializedName("group___owner___email")
    var groupOwnerEmail: String = ""

    var role: String = ""

    @SerializedName("endUser___id")
    var endUserId: Int = 0

    @SerializedName("endUser___name")
    var endUserName: String = ""


    @SerializedName("endUser___email")
    var endUserEmail: String = ""


    @SerializedName("master___name")
    var master_name: String = ""

    @SerializedName("master___id")
    var master_id: Int = 0

    @SerializedName("master___mac")
    var master_mac: String = ""


}