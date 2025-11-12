package hr.sil.android.smartlockers.adminapp.core.ble.comm.model

object LockerFlagsUtil {
    enum class Flag(val mask: Int) {
        REDUCED_MOBILITY(0b00000001),
        CLEANING_REQUIRED(0b00000010);
        fun isSet(flags: Byte) = (flags.toInt() and mask) > 0
    }
    data class LockerFlag(val flag: Flag, val value: Boolean)
    data class LockerUpdate(val mac: ByteArray, val index: ByteArray, val flags: Array<LockerFlag>)
    data class LockerInfo(var mac: ByteArray, var index: ByteArray)
    private fun foldFlagsMask(flags: Array<Byte>): Byte {
        return flags.fold(0x00) { acc, flag -> (acc or flag.toInt()) and 0xFF }.toByte()
    }
    private fun foldFlagsData(flags: Array<LockerFlag>): Byte {
        return foldFlagsMask(
            flags.map { if (it.value) it.flag.mask.toByte() else 0x00.toByte() }.toTypedArray()
        )
    }
    fun generateUpdateData(targetFlags: Array<Flag>, updateLockers: List<LockerUpdate>): ByteArray {
        val mask =
            foldFlagsMask(
                targetFlags.map { it.mask.toByte() }.toTypedArray()
            )
        val update = updateLockers
            .map { it.mac + it.index + foldFlagsData(
                it.flags
            )
            }
            .reduce { acc, data -> acc + data }
        return byteArrayOf(mask) + update
    }
    fun generateCleaningRequiredData(lockers: List<LockerInfo>, cleaningRequired: Boolean): ByteArray {
        val targetFlags = arrayOf(Flag.CLEANING_REQUIRED)
        val updateLockers = lockers.map { lockerInfo ->
            LockerUpdate(
                mac = lockerInfo.mac,
                index = lockerInfo.index,
                flags = arrayOf(
                    LockerFlag(
                        Flag.CLEANING_REQUIRED,
                        cleaningRequired
                    )
                )
            )
        }
        return generateUpdateData(
            targetFlags,
            updateLockers
        )
    }
}