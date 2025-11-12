package hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogConfirmLinuxActionsBinding
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.fragment.main.LinuxActionsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class LinuxConfirmationActionsDialog(
    val lockerMac: String,
    val lockerAction: String,
    val deviceLatitude: Double?,
    val deviceLongitude: Double?
) : DialogFragment() {

    val forceOpen = "forceOpen"
    val invalidateKey = "invalidateKey"

    private val log = logger()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private var currentTime = Date()
    private var PASSED_7_SECONDS = 7000

    private var coroutineJob = Job()
    var currentSystemTime = System.currentTimeMillis()

    private lateinit var binding: DialogConfirmLinuxActionsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogConfirmLinuxActionsBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            if( lockerAction == forceOpen )
                binding.tvQuestion.text = getString(R.string.are_you_sure_force_open)
            else
                binding.tvQuestion.text = getString(R.string.are_you_sure_delete_key)

            binding.btnConfirm.setOnClickListener {
                setLockerAction()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

    private fun setLockerAction() {
        binding.btnConfirm.visibility = View.INVISIBLE
        binding.btnCancel.visibility = View.INVISIBLE
        binding.progressBarLinuxAction.visibility = View.VISIBLE
        lifecycleScope.launch {
            val backendResponse = if (lockerAction == forceOpen)
                WSAdmin.forceOpen(lockerMac)
            else
                WSAdmin.invalidateKeyCloudLocker(lockerMac)

            log.info("Backend response is: ${backendResponse}, action is: ${lockerAction}")
            withContext(Dispatchers.Main) {

                if (lockerAction == invalidateKey) {
                    val navHostFragment =
                        (context as MainActivity).supportFragmentManager.findFragmentById(
                            R.id.navigation_host_fragment
                        )?.childFragmentManager?.fragments?.get(0)
                    if (navHostFragment != null && navHostFragment is LinuxActionsFragment) {
                        navHostFragment.updateUiAfterDeletingKey()
                    }
                }

                if (backendResponse)
                    Toast.makeText(
                        App.ref,
                        getString(R.string.app_generic_success),
                        Toast.LENGTH_SHORT
                    ).show()
                else
                    Toast.makeText(
                        App.ref,
                        getString(R.string.app_generic_error),
                        Toast.LENGTH_SHORT
                    ).show()
                dismiss()
            }
        }
    }

   /* @SuppressLint("MissingPermission")
    private fun setLockerActionWithGpsLocationProvider() {
        binding.btnConfirm.visibility = View.INVISIBLE
        binding.btnCancel.visibility = View.INVISIBLE
        binding.progressBarLinuxAction.visibility = View.VISIBLE
        lifecycleScope.launch(coroutineJob) {

            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            val locationRequest = LocationRequest.create().apply {
                interval = 3000
                fastestInterval = 3000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        // Update UI with location data
                        val timePassed = Date().time - currentTime.time

                        log.info("Time passed to get new gps coord 5555: ${System.currentTimeMillis() - currentSystemTime}")

                        val deviceLocation = Location("deviceLocation")
                        if (deviceLatitude != null && deviceLongitude != null) {
                            deviceLocation.latitude = deviceLatitude
                            deviceLocation.longitude = deviceLongitude
                        }

                        val currentLocation = Location("currentLocation")
                        if (location != null) {
                            currentLocation.latitude = location.latitude
                            currentLocation.longitude = location.longitude
                        }

                        val distance =
                            if (currentLocation != null && deviceLocation != null) deviceLocation.distanceTo(
                                currentLocation
                            ) else 100f

                        Log.i(
                            "GpsAccuracy",
                            "Gps accuracy is: ${location.accuracy}, gps distance is: ${distance}"
                        )

                        if (location != null && distance < 50f && deviceLatitude != null && deviceLongitude != null) {
                            Log.i("Tag", "New location received: ${location}")

                            lifecycleScope.launch {
                                val backendResponse = if (lockerAction == forceOpen)
                                    WSAdmin.forceOpen(lockerMac)
                                else
                                    WSAdmin.invalidateKeyCloudLocker(lockerMac)

                                log.info("Backend response is: ${backendResponse}, action is: ${lockerAction}")
                                fusedLocationClient?.removeLocationUpdates(locationCallback)
                                withContext(Dispatchers.Main) {

                                    if (lockerAction == invalidateKey) {
                                        val navHostFragment =
                                            (context as MainActivity).supportFragmentManager.findFragmentById(
                                                R.id.navigation_host_fragment
                                            )?.childFragmentManager?.fragments?.get(0)
                                        if (navHostFragment != null && navHostFragment is LinuxActionsFragment) {
                                            navHostFragment.updateUiAfterDeletingKey()
                                        }
                                    }

                                    if (backendResponse)
                                        Toast.makeText(
                                            App.ref,
                                            getString(R.string.app_generic_success),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    else
                                        Toast.makeText(
                                            App.ref,
                                            getString(R.string.app_generic_error),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    dismiss()
                                }
                            }
                        }

                        if (timePassed > PASSED_7_SECONDS) {
                            if (coroutineJob.isActive) coroutineJob.cancel()
                            fusedLocationClient?.removeLocationUpdates(locationCallback)
                            log.info("Time passed to get new gps coordinate: ${timePassed}")
                            dismiss()
                            App.ref.toast(requireContext().getString(R.string.not_in_proximity_of_locker))
                        }

                    }
                }
            }
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }*/

}