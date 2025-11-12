package hr.sil.android.smartlockers.adminapp.cache.status

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ActionStatusKey {

    var macAddress: String = ""
    var statusType: ActionStatusType = ActionStatusType.UNKNOWN
    var keyId: String = ""
    var timeOfInstance: Date = Date()
    var isScheduleDelete: Boolean = false
    val scheduleMinutes = 1L
    val SCHEDULE_PERIOD: Long = 1000L * 60L * scheduleMinutes


    fun scheduleDelete(instanceKey: String, delayReduction: Long = 0L) {
        GlobalScope.launch(Dispatchers.Default) {
            delay(SCHEDULE_PERIOD - delayReduction)
            try {
                ActionStatusHandler.actionStatusDb.del(instanceKey)
            } catch (ex: Exception) {

            }
        }
    }

    init {
        if (isScheduleDelete) {
            scheduleDelete(keyId)
        }
    }

}