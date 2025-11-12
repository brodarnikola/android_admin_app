package hr.sil.android.smartlockers.adminapp.core.remote.model

enum class EpaperTypeByteCode(val code: Byte) {
    NO_EPAPER(0x00),
    LOW_RESOLUTION(0x01),
    HIGH_RESOLUTION(0x02)
}