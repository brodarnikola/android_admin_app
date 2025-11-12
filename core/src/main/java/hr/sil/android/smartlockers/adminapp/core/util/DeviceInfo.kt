package hr.sil.android.smartlockers.adminapp.core.util

import com.google.gson.GsonBuilder


data class DeviceInfo(val sdk_int: Int = android.os.Build.VERSION.SDK_INT,
                      val os_release: String = android.os.Build.VERSION.RELEASE,
                      val device: String = android.os.Build.DEVICE,
                      val deviceModel: String = android.os.Build.MODEL
) {

    companion object {
        val gson = GsonBuilder().create()
        fun getJsonInstance(): String {
            return gson.toJson(DeviceInfo())
        }
    }

}