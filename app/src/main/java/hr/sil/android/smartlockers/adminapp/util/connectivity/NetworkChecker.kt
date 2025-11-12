/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.smartlockers.adminapp.util.connectivity

import com.esotericsoftware.minlog.Log
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.util.network.NetworkConnectivity
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
object NetworkChecker {

    private const val checkPeriod: Long = 60_000L
    val log = logger()
    private val listeners = ConcurrentHashMap<String, (Boolean) -> Unit>()

    fun addListener(listener: (Boolean) -> Unit): String {
        val key = UUID.randomUUID().toString()
        listeners[key] = listener

        //immediately call new listener with last check result
        lastResult?.let { listener.invoke(it) }

        if (listeners.size == 1) {
            Log.info("Checking if the network is available")
            runChecker()
        }
        return key
    }

    fun removeListener(key: String) {
        listeners.remove(key)
        if (listeners.size == 0) {
            checkerJob?.cancel()
        }
    }

    private fun notifyListeners(state: Boolean) {
        listeners.forEach { it.value.invoke(state) }
    }

    private var lastResult: Boolean? = null
    private var lastCheck: Long = 0L
    private var checkerJob: Job? = null

    private val fullCheckInProgress = AtomicBoolean(false)

    private  fun doCheck(notify: Boolean) {
        val isNetworkAvailable = NetworkConnectivity.isNetworkAvailable(App.ref)
        if (isNetworkAvailable) {
            val now = System.currentTimeMillis()
            if ((lastResult == null) || (now - lastCheck > checkPeriod)) {
                lastCheck = now

                if (fullCheckInProgress.compareAndSet(false, true)) {
                    val result = try {
                        isConnectionToGoogleAvailable()
                    } catch (exc: Exception) {
                        false
                    }
                    lastResult = result
                    if (notify) {
                        notifyListeners(result)
                    }
                    fullCheckInProgress.set(false)
                }
            }
        } else {
            lastCheck = 0L

            val result = false
            lastResult = result
            if (notify) {
                notifyListeners(result)
            }
        }
    }

    private fun runChecker() {
        checkerJob = GlobalScope.launch {
            while (isActive) {
                doCheck(notify = true)
                delay(5000L)
            }
        }
    }

    fun notifyInternetConnection(available: Boolean) {
        if (fullCheckInProgress.compareAndSet(false, true)) {
            lastCheck = System.currentTimeMillis()
            lastResult = available
            notifyListeners(available)
        }
    }

    suspend fun isInternetConnectionAvailable(): Boolean {
        //force update lastResult if it is null, otherwise use previous value
        if (lastResult == null) doCheck(notify = false)
        return lastResult!!
    }
    fun isConnectionToGoogleAvailable(): Boolean {
        return try {
            val urlConnection = URL("https://clients3.google.com/generate_204").openConnection() as HttpURLConnection
            urlConnection.responseCode == 204 && urlConnection.contentLength == 0
        } catch (exc: Exception) {
            log.error(exc.message, exc)
            false
        }
    }

}