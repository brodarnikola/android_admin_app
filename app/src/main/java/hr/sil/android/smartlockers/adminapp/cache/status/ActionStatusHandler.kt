package hr.sil.android.smartlockers.adminapp.cache.status

import android.content.Context
import hr.sil.android.datacache.AutoCache
import hr.sil.android.datacache.TwoLevelCache
import hr.sil.android.datacache.updatable.CacheSource
import hr.sil.android.datacache.util.PersistenceClassTracker
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.core.util.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Stef on 28.11.2018.
 */
object ActionStatusHandler {

    val log = logger()

    private const val UPDATE_PERIOD = 5000L

    private val running = AtomicBoolean(false)

    fun checkClasses(context: Context) {
        PersistenceClassTracker.checkClass(context, ActionStatusKey::class)
    }

    fun run() {
        if (running.compareAndSet(false, true)) {
            GlobalScope.launch(Dispatchers.Default) {
                while (true) {
                    try {
                        checkExpired()
                    } catch (ex: Exception) {
                        log.error("Periodic remote-update failed...", ex)
                    }

                    delay(UPDATE_PERIOD)
                }
            }
        }
    }

    suspend fun checkExpired() {
        val actions = actionStatusDb.getAll()
        val currentTime = Date().time
        val listForDelete = mutableListOf<String>()

        actions.forEach {
            val timeDifference: Long = currentTime - it.timeOfInstance.time
            if ((timeDifference) > it.SCHEDULE_PERIOD) {
                listForDelete.add(it.keyId)
            } else {
                it.scheduleDelete(it.keyId, timeDifference)
            }
        }
        for (key in listForDelete) {
            actionStatusDb.del(key)
        }

    }

    val actionStatusDb by lazy {
        AutoCache.Builder(TwoLevelCache
                .Builder(ActionStatusKey::class, ActionStatusKey::keyId)
                .memoryLruMaxSize(20)
                .build(App.ref)).setSingleElementSource(CacheSource.ForKey.Suspendable(5, TimeUnit.MINUTES) { mac, it ->
            null
        }).build()
    }


}

