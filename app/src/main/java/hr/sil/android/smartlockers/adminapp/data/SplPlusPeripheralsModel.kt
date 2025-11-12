package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize


class SplPlusPeripheralsModel constructor(var lockerIndex: Int, var lockerSizeIndex: Int, var lockerSizeByte: RLockerSize, var lockerStatus: LockerP16Status, var isSelected: Boolean,
                                          var lockerMac: String, var isAvailable: Boolean, var isDeleted: Boolean)