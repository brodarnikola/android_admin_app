package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerKeyPurpose
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerType
import java.util.*

class RLockerDataUiModel(var id: Int = 0, var mac: String, var masterMac: String = "", var status: MPLAppDeviceStatus,
                         var size: RLockerSize = RLockerSize.UNKNOWN, var isLockerInProximity: Boolean, var deviceType: RLockerType = RLockerType.NORMAL,
                         var keyPurpose: RLockerKeyPurpose = RLockerKeyPurpose.UNKNOWN, var createdByName: String, var createdOnDate: String = "",
                         var deletingKeyInProgress: Boolean = false, var requestToDeletingKeyOnBackend: Boolean = false, var lockerKeyId: Int = 0,
                         var isMasterUnitInProximity: Boolean = false, var isReducedMobility: Boolean = false, var lockerSingleOrP16Version: String = "",
                         var cleaningNeeded: RActionRequired = RActionRequired.NULL, //: Boolean = false
                         var doorStatus: MutableList<Int> = mutableListOf()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RLockerDataUiModel

        if (id != other.id) return false
        if (mac != other.mac) return false
        if (masterMac != other.masterMac) return false
        if (status != other.status) return false
        if (size != other.size) return false
        if (isLockerInProximity != other.isLockerInProximity) return false
        if (deviceType != other.deviceType) return false
        if( keyPurpose != other.keyPurpose ) return false
        if( deletingKeyInProgress != other.deletingKeyInProgress ) return false
        if( requestToDeletingKeyOnBackend != other.requestToDeletingKeyOnBackend ) return false
        if( lockerKeyId != other.lockerKeyId ) return false
        if( isMasterUnitInProximity != other.isMasterUnitInProximity ) return false
        if( isReducedMobility != other.isReducedMobility ) return false
        if( lockerSingleOrP16Version != other.lockerSingleOrP16Version ) return false
        if( cleaningNeeded != other.cleaningNeeded ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + mac.hashCode()
        result = 31 * result + masterMac.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + isLockerInProximity.hashCode()
        result = 31 * result + deviceType.hashCode()
        result = 31 * result + keyPurpose.hashCode()
        result = 31 * result + deletingKeyInProgress.hashCode()
        result = 31 * result + requestToDeletingKeyOnBackend.hashCode()
        result = 31 * result + lockerKeyId.hashCode()
        result = 31 * result + isMasterUnitInProximity.hashCode()
        result = 31 * result + isReducedMobility.hashCode()
        result = 31 * result + lockerSingleOrP16Version.hashCode()
        result = 31 * result + cleaningNeeded.hashCode()
        return result
    }
}