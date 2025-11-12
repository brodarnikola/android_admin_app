package hr.sil.android.zwicktablet.gps


import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class GpsUtils (context: Context)  {


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
        this.locationRequest.fastestInterval = 2 * 1000
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(this.locationRequest)
        mLocationSettingsRequest = builder.build()

        //**************************
        builder.setAlwaysShow(true) //this is the key ingredient
        //**************************
    }


    // method for turn on GPS
    fun turnGPSOn(): Boolean {

        if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)!!) {
            return true
        } else {
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
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage =
                                "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                            Log.e(TAG, errorMessage)

                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
        return false
    }

    interface onGpsListener {
        fun gpsStatus(isGPSEnable: Boolean)
    }

}