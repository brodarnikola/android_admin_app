package hr.sil.android.smartlockers.adminapp.util

import android.content.Context
import android.net.ConnectivityManager
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater


object AppUtil {
    val log = logger()

    suspend fun refreshCache() {
        log.info("App refresh cache")
        //DatabaseHandler.deliveryKeyDb.clear()
        DataCache.clearCaches()
        DataCache.preloadCaches()
        MPLDeviceStore.clear()
        //force update device store
        MPLDeviceStoreRemoteUpdater.forceUpdate()
    }


    fun isInternetAvailable(): Boolean {
        try {
            val cm = App.ref.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null
        } catch (e: SecurityException) {
            log.error("Please check if you grant ACCESS_NETWORK_STATE, or put insights = false in App init!")
            return false
        }
    }
}

