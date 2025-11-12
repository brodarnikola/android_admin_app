package hr.sil.android.smartlockers.adminapp.view.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.firebase.analytics.FirebaseAnalytics
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.BuildConfig
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.ActivityMainBinding
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil.logout
import hr.sil.android.smartlockers.adminapp.view.fragment.main.GoogleMapsLatLongFragment
import hr.sil.android.view_util.extensions.hideKeyboard
import hr.sil.android.view_util.permission.DroidPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {

    private val log = logger()
    private val droidPermission by lazy { DroidPermission.init(this) }
    var navHostFragment: NavHostFragment? = null
    val ENABLE_LOCATION_IN_GOOGLE_MAPS_REQUEST_CODE = 1001

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!UserUtil.isUserLoggedIn()) {

            GlobalScope.launch(Dispatchers.Main) {
                if (UserUtil.login(SettingsHelper.usernameLogin)) {
                    continueOnCreate(savedInstanceState)
                } else {
                    logout()
                }
            }
        } else {
            continueOnCreate(savedInstanceState)
        }
    }

    private fun continueOnCreate(savedInstanceState: Bundle?) {
        viewLoaded = true
        setNotification()

        val toolbar: Toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //load initial fragment
        if (savedInstanceState == null) {
            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.navigation_host_fragment) as NavHostFragment?
            NavigationUI.setupWithNavController(
                binding.bottomMenu,
                navHostFragment!!.navController
            )

            navHostFragment!!.navController.addOnDestinationChangedListener { _, _, _ ->
                hideKeyboard()
            }

            /* val navHost = NavHostFragment.create(R.navigation.nav_graph_main)
             supportFragmentManager.beginTransaction()
                 .replace(R.id.navigation_host_fragment, navHost)
                 .setPrimaryNavigationFragment(navHost)
                 .commit()*/

            //bottomMenu.setupWithNavController(findNavController(this, R.id.main_frame))

        }


        val permissions = mutableListOf<String>().apply {
            addAll(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addAll(arrayOf(Manifest.permission.BLUETOOTH_SCAN,  Manifest.permission.BLUETOOTH_CONNECT))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (BuildConfig.DEBUG) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        droidPermission
            .request(*permissions)
            .done { _, deniedPermissions ->
                if (deniedPermissions.isNotEmpty()) {
                    log.info("Some permissions were denied!")
                    App.ref.permissionCheckDone = true
                } else {
                    log.info("Permissions accepted...")
                    App.ref.permissionCheckDone = true

//                    log.info("Enabling bluetooth...")
//                    App.ref.btMonitor.enable {
//                        log.info("Bluetooth enabled!")
//                        App.ref.permissionCheckDone = true
//                    }
                }
            }
            .execute()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val childFragments = navHostFragment?.childFragmentManager?.fragments
            childFragments?.forEach {
                if( it.targetFragment == GoogleMapsLatLongFragment().targetFragment && requestCode == ENABLE_LOCATION_IN_GOOGLE_MAPS_REQUEST_CODE ) {
                    log.info("MainActivity.. OnActivityResult called when enabling location, result OK, user allowed it")
                    it.onActivityResult(requestCode, resultCode, data)
                }
            }

        } else {
            log.info("MainActivity.. OnActivityResult called user did not allowed some action")
            navHostFragment?.navController?.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBluetoothStateUpdated(available: Boolean) {
        super.onBluetoothStateUpdated(available)
        bluetoothAvailable = available
        if( viewLoaded == true )
            updateUI()
    }

    override fun onNetworkStateUpdated(available: Boolean) {
        super.onNetworkStateUpdated(available)
        networkAvailable = available
        if( viewLoaded == true )
            updateUI()
    }

    override fun onLocationGPSStateUpdated(available: Boolean) {
        super.onLocationGPSStateUpdated(available)
        locationGPSAvalilable = available
        if( viewLoaded == true )
            updateUI()
    }

    private fun setNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, UserUtil.user?.id.toString())
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "NotificationReceived")
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification")
            mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!!.get(key)
                log.info("Key: $key Value: $value")
            }
        }
    }


}
