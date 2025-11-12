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

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.widget.Toast
import com.esotericsoftware.minlog.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author mfatiga
 */
class LocationGPSChecker (context: Context)  {
    private var lastResult: Boolean? = null
    private var lastCheck: Long = 0L
    private var checkerJob: Job? = null
    private val checkPeriod: Long = 5_000L

    private val listeners = ConcurrentHashMap<String, (Boolean) -> Unit>()

    fun addListener(listener: (Boolean) -> Unit): String {
        val key = UUID.randomUUID().toString()
        listeners[key] = listener

        //immediately call new listener with last check result
        lastResult?.let { listener.invoke(it) }
        //lastResult?.let { listener.invoke(it) }

        if (listeners.size == 1) {
            Log.info("Checking if location, gps service is available")
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

    private var context: Context
    private var mSettingsClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest

    init {
        this.context = context
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mSettingsClient = LocationServices.getSettingsClient(context)

        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10 * 1000
        this.locationRequest.fastestInterval = 5 * 1000

        /* Show default dialog, that LOCATION OR GPS is TURNED OFF,
        //val builder = LocationSettingsRequest.Builder()
        //    .addLocationRequest(this.locationRequest)
        //mLocationSettingsRequest = builder.build()
        // --------------------------
        //builder.setAlwaysShow(true) //this is the key ingredient
        // --------------------------  */
    }

    // method for turn on GPS
    fun turnGPSOn(): Boolean {

        if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)!!) {
            return true
        } else {
            if( mLocationSettingsRequest != null )
                mSettingsClient
                    ?.checkLocationSettings(mLocationSettingsRequest!!)
                    ?.addOnSuccessListener(context as Activity) {
                        //  GPS is already enable, callback GPS status through listener
                        true
                    }
                    ?.addOnFailureListener(context as Activity) { e ->
                        val statusCode = (e as ApiException).statusCode
                        when (statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->

                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    val rae = e as ResolvableApiException
                                    rae.startResolutionForResult(context as Activity, 1001)
                                } catch (sie: IntentSender.SendIntentException) {
                                    android.util.Log.i(ContentValues.TAG, "PendingIntent unable to execute request.")
                                }

                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                val errorMessage =
                                    "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                                android.util.Log.e(ContentValues.TAG, errorMessage)

                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
        }
        return false
    }

    private fun doCheck() {
        val now = System.currentTimeMillis()
        Log.info("Time passed in seconds: ${now - lastCheck}")
        if( (now - lastCheck > checkPeriod) ) {
            if (turnGPSOn()) {
                notifyListeners(true)
            } else {
                notifyListeners(false)
            }
        }
    }

    private fun runChecker() {
        checkerJob = GlobalScope.launch {
            while (isActive) {
                lastCheck = System.currentTimeMillis()
                delay(5500L)
                doCheck()
            }
        }
    }

}