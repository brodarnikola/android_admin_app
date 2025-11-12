package hr.sil.android.smartlockers.adminapp.util.connectivity

import android.bluetooth.BluetoothAdapter
import hr.sil.android.smartlockers.adminapp.App
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author mfatiga
 */
object BluetoothChecker {
    private val listeners = ConcurrentHashMap<String, (Boolean) -> Unit>()
    fun addListener(listener: (Boolean) -> Unit): String {
        val key = UUID.randomUUID().toString()
        listeners[key] = listener

        //immediately call new listener with last check result
        listener.invoke(getLastResult())

        if (listeners.size == 1) {
            enableInternalListener()
        }
        return key
    }

    fun removeListener(key: String) {
        listeners.remove(key)
        if (listeners.size == 0) {
            disableInternalListener()
        }
    }

    @Volatile private var lastResult: Boolean? = null
    private fun getLastResult(): Boolean {
        if (lastResult == null) {
            lastResult = App.ref.btMonitor.isEnabled
        }
        return lastResult!!
    }

    private fun notifyListeners(state: Boolean) {
        lastResult = state
        listeners.forEach { it.value.invoke(state) }
    }


    private var internalListenerKey: String? = null
    private fun enableInternalListener() {
        internalListenerKey = App.ref.btMonitor.addStateChangeListener(this::internalListener)
    }

    private fun internalListener(state: Int) {
        if (state == BluetoothAdapter.STATE_ON) {
            notifyListeners(true)
        } else if (state == BluetoothAdapter.STATE_OFF) {
            notifyListeners(false)
        }
    }

    private fun disableInternalListener() {
        internalListenerKey?.let { App.ref.btMonitor.removeStateChangeListener(it) }
    }
}