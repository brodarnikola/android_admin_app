package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentGoogleMapsBinding
import hr.sil.android.smartlockers.adminapp.util.AppUtil
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import hr.sil.android.view_util.permission.DroidPermission
import hr.sil.android.zwicktablet.gps.GpsUtils
import kotlinx.coroutines.*
import java.util.*

class GoogleMapsLatLongFragment : BaseFragment()
//    , OnMapReadyCallback,
//    GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener
{

//    val log = logger()
//    private lateinit var mMap: GoogleMap
//    private var fusedLocationClient: FusedLocationProviderClient? = null
//
//    private var marker: Marker? = null
//    private var lastLatitude = 0.0
//    private var lastLongitude = 0.0
//
//    var googleMapsCalledFrom: String = ""
//    var macAddress: String = ""
//    var deviceName: String = ""
//    var deviceAddress: String = ""
//    var registeredDeviceLatitude = 0.0
//    var registeredDeviceLongitude = 0.0
//
//    val GOOGLE_MAPS_CALLED_FROM_LOCKER_SETTINGS = "LockerSettings"

    private lateinit var binding: FragmentGoogleMapsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        val rootView = inflater.inflate(
//            R.layout.fragment_google_maps, container,
//            false
//        )

//        binding = FragmentGoogleMapsBinding.inflate(layoutInflater)
//
//
//        initializeToolbarUIMainActivity(
//            true,
//            getString(R.string.select_locker_location_title),
//            false,
//            false,
//            requireContext()
//        )
//        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
//
//        googleMapsCalledFrom = arguments?.getString("googleMapsCalledFrom", "") ?: ""
//        macAddress = arguments?.getString("masterMac", "") ?: ""
//        deviceName = arguments?.getString("deviceName", "") ?: ""
//        deviceAddress = arguments?.getString("deviceAddress", "") ?: ""
//        registeredDeviceLatitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
//        registeredDeviceLongitude = arguments?.getDouble("longitude", 0.0) ?: 0.0

        return binding.root // rootView
        // return rootView
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        //parentJob = Job()
//
//        binding.btnConfirmLatLong.setOnClickListener {
//            if (lastLatitude != 0.0 && lastLongitude != 0.0) {
//
//                if (googleMapsCalledFrom == GOOGLE_MAPS_CALLED_FROM_LOCKER_SETTINGS) {
//                    val bundle = bundleOf(
//                        "latitude" to lastLatitude,
//                        "longitude" to lastLongitude,
//                        "masterMac" to macAddress,
//                        "deviceAddress" to deviceAddress
//                    )
//
//                    findNavController().navigate(
//                        R.id.google_map_lat_long_fragment_to_locker_settings_fragment,
//                        bundle
//                    )
//                } else {
//                    val bundle = bundleOf(
//                        "latitude" to lastLatitude,
//                        "longitude" to lastLongitude,
//                        "masterMac" to macAddress,
//                        "deviceName" to deviceName,
//                        "deviceAddress" to deviceAddress
//                    )
//
//                    findNavController().navigate(
//                        R.id.google_map_lat_long_fragment_to_device_details_fragment,
//                        bundle
//                    )
//                }
//            }
//        }
//
//        GpsUtils(requireContext()).turnGPSOn()
//
//        val mapFragment = childFragmentManager.findFragmentById(R.id.g_map) as SupportMapFragment?
//        if (mapFragment != null)
//            mapFragment.getMapAsync(this)
//
//        if (requireContext() != null)
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//
//        mMap = googleMap
//        mMap.uiSettings.isMapToolbarEnabled = false
//        mMap.uiSettings.isZoomControlsEnabled = true
//        mMap.setOnMarkerClickListener(this)
//        mMap.setOnMapClickListener(this)
//        log.info("da li ce tu uci: OOOO")
//
//        if (ContextCompat.checkSelfPermission(
//                activity as MainActivity,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            droidPermission
//                .request(Manifest.permission.ACCESS_FINE_LOCATION)
//                .done { _, deniedPermissions ->
//                    if (deniedPermissions.isNotEmpty()) {
//                        log.info("Permissions were denied!")
//                        Toast.makeText(
//                            requireContext(),
//                            "Permission are denied",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } else {
//                    }
//                }
//                .execute()// 1
//        } else {
//
//            mMap.isMyLocationEnabled = true
//            //scroolView.smoothScrollTo(0, scroolView.getBottom())
//            if (googleMapsCalledFrom == GOOGLE_MAPS_CALLED_FROM_LOCKER_SETTINGS) {
//                val currentLatLng = LatLng(registeredDeviceLatitude, registeredDeviceLongitude)
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16.0f))
//                val cameraMovePosition = LatLng(registeredDeviceLatitude, registeredDeviceLongitude)
//                if (marker != null)
//                    marker!!.remove()
//                marker = mMap.addMarker(MarkerOptions().position(cameraMovePosition))
//                lastLatitude = registeredDeviceLatitude
//                lastLongitude = registeredDeviceLongitude
//                addAddressValueToTextView(currentLatLng)
//            } else {
//                fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
//                    if (location != null) {
//                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16.0f))
//                        val cameraMovePosition = LatLng(location.latitude, location.longitude)
//                        if (marker != null)
//                            marker!!.remove()
//                        marker = mMap.addMarker(MarkerOptions().position(cameraMovePosition))
//                        lastLatitude = location.latitude
//                        lastLongitude = location.longitude
//                        addAddressValueToTextView(currentLatLng)
//                    }
//                }
//            }
//
//            /* pictureOverGoogleMap.setOnTouchListener { v, event ->
//
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        scroolView.requestDisallowInterceptTouchEvent(true)
//                        // Disable touch on transparent view
//                        //false
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        // Allow ScrollView to intercept touch events.
//                        scroolView.requestDisallowInterceptTouchEvent(false)
//                        //true
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        scroolView.requestDisallowInterceptTouchEvent(true)
//                        //false;
//                    }
//                    MotionEvent.ACTION_CANCEL -> {
//
//                        scroolView.requestDisallowInterceptTouchEvent(false)
//                        //false;
//                    }
//                    else -> true
//                }
//                pictureOverGoogleMap.onTouchEvent(event)
//            }*/
//        }
//    }
//
//    override fun onMarkerClick(mMarker: Marker): Boolean {
//        return true
//    }
//
//    override fun onMapClick(latLng: LatLng) {
//        val markerOptions = MarkerOptions()
//        markerOptions.position(latLng).title("" + latLng.latitude + "," + latLng.longitude)
//        if (marker != null) {
//            marker!!.remove()
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
//            marker = mMap.addMarker(markerOptions)
//            lastLatitude = latLng.latitude
//            lastLongitude = latLng.longitude
//            addAddressValueToTextView(latLng)
//        }
//        else {
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
//            marker = mMap.addMarker(markerOptions)
//            lastLatitude = latLng.latitude
//            lastLongitude = latLng.longitude
//            addAddressValueToTextView(latLng)
//        }
//    }
//
//    private val droidPermission by lazy { DroidPermission.init(activity as MainActivity) }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) = droidPermission.link(requestCode, permissions, grantResults)
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        log.info("OnActivityResult called when enabling location")
//        if (resultCode == RESULT_OK) {
//
//            log.info("OnActivityResult called when enabling location, result OK, user allowed it")
//            if (ContextCompat.checkSelfPermission(
//                    activity as AppCompatActivity,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//
//                log.info("Google maps fragment: entering 11111")
//                //ivLoading.visibility = View.VISIBLE
//                binding.btnConfirmLatLong.isEnabled = false
//                binding.btnConfirmLatLong.alpha = 0.4f
//                mMap.uiSettings.isScrollGesturesEnabled = false
//                binding.tvAddressAndLocation.text = "Please wait, GPS is trying to find your location. If it does not succeed, then please try by your self."
//                lifecycleScope.launch() {
//                    delay(10000)
//                    withContext(Dispatchers.Main) {
//                        // Maybe I will need this, because if GPS location is not turned on, then it display dialog for access location services
//                        // and then this service maybe block "mapFragment!!.getMapAsync(this@MPLItemDetailsFragment)"
//                        // .. I will see how it will display, act, without this lines of code
//                        //val mapFragment = this@MPLItemDetailsFragment.fragmentManager?.findFragmentById(R.id.g_map) as SupportMapFragment?
//                        //mapFragment!!.getMapAsync(this@MPLItemDetailsFragment)
//
//                        log.info("Google maps fragment: entering 2222")
//                        //ivLoading.visibility = View.GONE
//                        binding.btnConfirmLatLong.isEnabled = true
//                        binding.btnConfirmLatLong.alpha = 1.0f
//                        mMap.uiSettings.isScrollGesturesEnabled = true
//                        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
//                            if (location != null) {
//
//                                log.info("Google maps fragment: entering 33333")
//                                val currentLatLng =
//                                    LatLng(location.latitude, location.longitude)
//                                val cameraMovePosition =
//                                    LatLng(location.latitude, location.longitude)
//                                if (marker != null)
//                                    marker!!.remove()
//                                marker =
//                                    mMap.addMarker(MarkerOptions().position(cameraMovePosition))
//                                mMap.animateCamera(
//                                    CameraUpdateFactory.newLatLngZoom(
//                                        currentLatLng,
//                                        16.0f
//                                    )
//                                )
//                                lastLatitude = location.latitude
//                                lastLongitude = location.longitude
//                                addAddressValueToTextView(currentLatLng)
//                            }
//                        }
//
//                    }
//                }
//            }
//        }
//    }
//
//
//    private fun addAddressValueToTextView(currentLatLng: LatLng) {
//        if (AppUtil.isInternetAvailable()) {
//            lifecycleScope.launch() {
//
//                val locale = Locale(SettingsHelper.languageName)
//                val gcd = Geocoder(requireContext(), locale)
//
//                var addresses: MutableList<Address> = mutableListOf()
//                try {
//                    addresses =
//                        gcd.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1)?.toMutableList() ?: mutableListOf()
//                } catch (e: Exception) {
//                    log.info("Exception is: ${e}")
//                }
//                withContext(Dispatchers.Main) {
//                    if (addresses != null && binding.tvAddressAndLocation != null && addresses.size > 0 && addresses[0] != null
//                        && addresses[0].getAddressLine(0) != null) {
//                        binding.tvAddressAndLocation.text = addresses[0].getAddressLine(0)
//                        deviceAddress = addresses[0].getAddressLine(0)
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    override fun onStop() {
//        val mapFragment =
//            (requireContext() as MainActivity).supportFragmentManager.findFragmentById(R.id.g_map) as SupportMapFragment?
//        if (mapFragment != null) {
//            (requireContext() as MainActivity).supportFragmentManager.beginTransaction().remove(mapFragment)
//                .commit()
//        }
//        super.onStop()
//    }


}