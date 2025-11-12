package hr.sil.android.smartlockers.adminapp.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.smartlockers.adminapp.util.AppUtil
import hr.sil.android.smartlockers.adminapp.util.NotificationHelper
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class MPLFireBaseMessagingService : FirebaseMessagingService() {
    val log = logger()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // TODO(developer): Handle FCM messages here.
        log.info("From: " + remoteMessage.from!!)
        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            log.info("Message data payload: " + remoteMessage.data)

                // Handle message within 10 seconds
            handleNow(remoteMessage.data)
        }
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            log.info("Message Notification Body: " + remoteMessage.notification!!.body!!)
        }
    }

    override fun onNewToken(token: String) {
        log.info("Refreshed token: $token")
        GlobalScope.launch(Dispatchers.Default) {
            if (!sendRegistrationToServer(token)) {
                withContext(Dispatchers.Main) {
                    log.error("Error in registration to server please check your internet connection")

                }
            }

        }
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */

    private fun handleNow(result: Map<String, String>) {
        val type = result["type"] ?: ""
        if (type == "DEFAULT") {
            NotificationHelper.createNotification(result["subject"], result["body"], MainActivity::class.java)
        }
        GlobalScope.launch {
            var masterMac = result["masterMac"]?.macCleanToReal()
            if (masterMac != null) {
                log.info("Refreshing Master mac: $masterMac ")
                //DataCache.getMasterUnit(masterMac, true)
                MPLDeviceStore.refreshMasterUnit(masterMac)
                DataCache.getRegisteredSlavesInBackend(true)
                MPLDeviceStoreRemoteUpdater.forceUpdate()
            } else {
                DataCache.getAlarmMessageLog(true)
                AppUtil.refreshCache()
            }
        }
        log.info("Short task when notification is opened is done")
    }




companion object {

    suspend fun sendRegistrationToServer(token: String): Boolean {
        return WSAdmin.registerDevice(token)
    }
}


}