package hr.sil.android.smartlockers.adminapp.core.remote.model

enum class KeypadEnum(val code: Byte) {
    SPL_PLUS_KEYPAD(0x01),
    SPL_KEYPAD(0x02),
    MPL_KEYPAD(0x03)
}