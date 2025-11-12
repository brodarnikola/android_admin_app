package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerKeyPurpose
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize


data class P16PeripheralsModel constructor(var lockerIndex: Int, var lockerSizeIndex: Int, var lockerSizeByte: RLockerSize, var lockerStatus: LockerP16Status, var isSelected: Boolean,
                                           var slaveIndexP16Mac: String, var isAvailable: Boolean, var isDeleted: Boolean,
                                           var keyPurpose: RLockerKeyPurpose = RLockerKeyPurpose.UNKNOWN, var createdByName: String, var createdOnDate: String = "",
                                           var reducedMobility: Boolean = false, var cleaningNeeded: RActionRequired = RActionRequired.NULL, var deletingKeyInProgress: Boolean = false, var requestToDeletingKeyOnBackend: Boolean = false,
                                           var lockerKeyId: Int = 0, var isP16DeviceInProximity: Boolean, var isMasterDeviceInProximity: Boolean = false,
                                           var doorStatus: Int = 0 )  // master device is MPL, TABLET and so ON