package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLModemStatus
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.cache.dto.CRegistration
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusHandler
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusKey
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusType
import hr.sil.android.smartlockers.adminapp.core.ble.comm.MPLAdminBLECommunicator
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.formatFromStringToDate
import hr.sil.android.smartlockers.adminapp.core.util.formatToViewDateTimeDefaults
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.RegisterMasterUnitInterface
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentDeviceDetailsBinding
import hr.sil.android.smartlockers.adminapp.events.MPLDevicesUpdatedEvent
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.util.AppUtil
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.NetworkConfigurationAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.*
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode 
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class DeviceDetailsFragment : BaseFragment(), RegisterMasterUnitInterface {

    val log = logger()
    lateinit var macAddress: String
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var deviceName: String = ""
    var deviceAddress: String = ""

    private var connecting: AtomicBoolean = AtomicBoolean(false)
    private var deleting: AtomicBoolean = AtomicBoolean(false)
    private var registering: AtomicBoolean = AtomicBoolean(false)
    lateinit var selectedItem: RNetworkConfiguration
    private var device: MPLDevice? = null

    val PREF_NAME = "PREF_NAME"
    val PREF_ADDRESS = "PREF_ADDRESS"

    val E_PAPER_TYPE_NONE = 0 // in aggrement with backend we are sending 0

    private var splType = SplType.SPL
    private var ePaperType = EPaperType.NO_EPAPER

    private var NO_EPAPER_SELECTED_ON_REGISTRATION = 0

    private var neutralValue = 0
    private var veryGoodValue = 0
    private var goodValue = 0
    private var badValue = 0
    private var veryBadValue = 0

    private var firstBackground = 0
    private var secondBackground = 0
    private var thirdBackground = 0
    private var fourthBackground = 0
    private var fifthBackground = 0
    private var sixthBackground = 0

    private var keyExpired = 168 // 168 hours, or 7 days
    var allDeleteKeysActionSuccess = DeleteExpiredKeyStatus.ALL_KEYS_ARE_GOOD

    var listLockerUnits: List<RLockerUnit> = listOf()

    enum class DeleteExpiredKeyStatus (val keyStatus: Int) {
        ALL_KEYS_ARE_GOOD(0),
        NOT_ALL_KEYS_ARE_GOOD(1)
    }

    private lateinit var binding: FragmentDeviceDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val rootView = inflater.inflate(
//            R.layout.fragment_device_details, container,
//            false
//        )

        binding = FragmentDeviceDetailsBinding.inflate(layoutInflater)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        macAddress = arguments?.getString("masterMac", "") ?: ""
        latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
        longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0
        deviceName = arguments?.getString("deviceName", "") ?: ""
        deviceAddress = arguments?.getString("deviceAddress", "") ?: ""
        log.info("Received mac address is: " + macAddress)
        device = MPLDeviceStore.devices[macAddress]
        log.info("Customer expiration days is: " + device?.customerDeliveryExpirationDays)

        val toolbarDeviceName =
            if (device?.masterUnitName != "" && device?.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED) device?.masterUnitName else getString(
                R.string.mpl_or_spl_unregistered
            )
        initializeToolbarUIMainActivity(
            true,
            toolbarDeviceName ?: "",
            false,
            false,
            requireContext()
        )

        return binding.root // rootView
        // return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAddress.text = getString(R.string.app_generic_address) + ":"
        setupBackgroundColorForButtons()

        registerDevice()
        setApnNetworkProvider()
        forceOpenDevice()
        ePaperRefresh()
        deleteAllKeys()

        setupDeviceInputData()

        binding.clLockerSettings.setOnClickListener {
            val bundle = bundleOf("masterMac" to macAddress)
            log.info("Sended masterMacAddress to locker settings is: " + bundle + " to String: " + bundle.toString())
            findNavController().navigate(
                R.id.device_details_fragment_to_locker_settings_fragment,
                bundle
            )
        }

        binding.clManagePeripherals.setOnClickListener {

            if( device?.installationType == InstalationType.LINUX ) {
                val bundle = bundleOf("masterMac" to macAddress)
                log.info("Sended masterMacAddress to manage userAccessList is: " + bundle + " to String: " + bundle.toString())
                findNavController().navigate(
                    R.id.device_details_fragment_to_manage_peripherals_linux_fragment,
                    bundle
                )
            }
            else if ((device?.type == MPLDeviceType.MASTER || device?.type == MPLDeviceType.TABLET ||
                        device?.masterUnitType == RMasterUnitType.MPL || device?.installationType == InstalationType.TABLET)
                && device?.masterUnitType != RMasterUnitType.SPL
            ) {
                val bundle = bundleOf("masterMac" to macAddress)
                log.info("Sended masterMacAddress to manage userAccessList is: " + bundle + " to String: " + bundle.toString())
                findNavController().navigate(
                    R.id.device_details_fragment_to_manage_peripherals_fragment,
                    bundle
                )
            } else if (device?.type == MPLDeviceType.SPL_PLUS && device?.masterUnitType == RMasterUnitType.SPL) {
                val bundle = bundleOf("masterMac" to macAddress, "slaveMac" to device?.macAddress)
                log.info("Sended masterMacAddress to manage userAccessList is: " + bundle + " to String: " + bundle.toString())
                findNavController().navigate(
                    R.id.device_details_fragment_to_spl_plus_fragment,
                    bundle
                )
            }
        }
        binding.clManageUsers.setOnClickListener {
            val bundle = bundleOf("masterMac" to macAddress)
            log.info("Sended masterMacAddress to locker settings is: " + bundle + " to String: " + bundle.toString())
            findNavController().navigate(
                R.id.device_details_fragment_to_manage_users_fragment,
                bundle
            )
        }
        binding.clManageNetwork.setOnClickListener {
            val bundle = bundleOf("masterMac" to macAddress)
            log.info("Sended masterMacAddress to locker settings is: " + bundle + " to String: " + bundle.toString())
            findNavController().navigate(
                R.id.device_details_fragment_to_network_settings_fragment,
                bundle
            )
        }

        binding.clKeypadLayout.setOnClickListener {
            val keypadDialog = SplPlusKeypadDialog(device, macAddress, device?.macAddress)
            keypadDialog.show((requireContext() as MainActivity).supportFragmentManager, "")
        }

        if (device?.alertsEnabled ?: false)
            binding.switchSendingAlerts.isChecked = true
        else
            binding.switchSendingAlerts.isChecked = false
        binding.switchSendingAlerts.setOnClickListener {
            if (device?.alertsEnabled ?: false) {

                val disableSendingAlertsDialog =
                    DisableSendingAlertsDialog(binding.switchSendingAlerts, macAddress)
                disableSendingAlertsDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager, ""
                )
            } else {
                lifecycleScope.launch {

                    val disableUserActionDialog = DisableUserActionsDialog()
                    disableUserActionDialog.isCancelable = false
                    disableUserActionDialog.show(
                        (requireContext() as MainActivity).supportFragmentManager, ""
                    )
                    try {
                        withTimeout(20000) {

                            val request = RMasterUnitRequest()
                            request.name = device?.masterUnitName ?: ""
                            request.address = device?.masterUnitAddress ?: ""

                            request.latitude = device?.latitude ?: 0.0
                            request.longitude = device?.longitude ?: 0.0
                            request.epdType___id = device?.ePaperTypeId
                            request.powerType =
                                if (device?.powerSaving == true) RPowerTypeEnum.BATTERY else RPowerTypeEnum.LINE
                            request.allowPinSave = device?.allowPinSave ?: false
                            request.networkConfigurationId = device?.networkConfigurationId ?: 0
                            request.alertsEnabled = true

                            log.info("Get Name:  ${request.name}")
                            log.info("Get Address: ${request.address}")
                            log.info("Get ePapertype id: ${request.epdType___id}")
                            log.info("Get networkConfiguration id: ${request.networkConfigurationId}")

                            val resultChangeSendingAlerts = WSAdmin.modifyMasterUnit(
                                macAddress.macRealToClean(),
                                request
                            ) != null
                            if (resultChangeSendingAlerts)
                                updateLocalDataForThisMasterUnit()
                            withContext(Dispatchers.Main) {
                                if (resultChangeSendingAlerts) {
                                    //App.ref.toast(resources.getString(R.string.app_generic_success))
                                    binding.switchSendingAlerts.isChecked = true
                                } else {
                                    //App.ref.toast(resources.getString(R.string.app_generic_error))
                                    binding.switchSendingAlerts.isChecked = false
                                }
                                disableUserActionDialog.dismiss()
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        log.info("TimeOutCalcelException error: ${e}")
                        binding.btnRegister.visibility = View.VISIBLE
                        disableUserActionDialog.dismiss()
                        //App.ref.toast(requireContext().getString(R.string.app_generic_error))
                    }
                }
            }
        }

        setupNetworkConfiguration()
        if (device?.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED)
            setupButtonForDeletingKeysAndLockersDetails()

        setMplViewDetails()
    }

    private suspend fun updateLocalDataForThisMasterUnit() {
        MPLDeviceStore.forceRefreshMasterUnitInLockerSettings(macAddress)
        //force update device store
        MPLDeviceStoreRemoteUpdater.forceUpdate()
    }

    private fun setupNetworkConfiguration() {
        lifecycleScope.launch {
            val list = WSAdmin.getNetworkConfigurations() ?: listOf()
            withContext(Dispatchers.Main) {
                log.info("${list.joinToString { it.name }}")

                if (binding.spinerapnNetworkSelection != null)
                    binding.spinerapnNetworkSelection.adapter = NetworkConfigurationAdapter(list)

                if (!list.isEmpty() && binding.spinerapnNetworkSelection != null)
                    selectedItem =
                        binding.spinerapnNetworkSelection.adapter.getItem(0) as RNetworkConfiguration
                else
                    selectedItem = RNetworkConfiguration()

                if( device?.type != MPLDeviceType.TABLET && device?.installationType != InstalationType.TABLET && device?.installationType != InstalationType.LINUX ) {
                    if( binding.tvRat != null && binding.tvRatValue != null ) {
                        binding.tvRat.visibility = View.VISIBLE
                        binding.tvRatValue.visibility = View.VISIBLE
                        if (list != null && list.isNotEmpty()) {
                            val networkConfigurationId =
                                list.filter { it.id == device?.networkConfigurationId }
                                    .firstOrNull()
                            if (networkConfigurationId?.modemRadioAccess == null)
                                binding.tvRatValue.text = "-"
                            else
                                binding.tvRatValue.text = " " + networkConfigurationId?.modemRadioAccess
                        } else {
                            binding.tvRatValue.text = "-"
                        }
                    }
                }
                else {
                    if( binding.tvRat != null && binding.tvRatValue != null ) {
                        binding.tvRat.visibility = View.GONE
                        binding.tvRatValue.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun deleteAllKeys() {
        val stmVersionControl = device?.stmOrAppVersion?.compareTo("2.1.0") ?: -1
        binding.clDeleteKeys.setOnClickListener {
            if( (device?.type == MPLDeviceType.SPL_PLUS || device?.masterUnitType == RMasterUnitType.SPL || device?.masterUnitType == RMasterUnitType.SPL_PLUS) && stmVersionControl < 0 ) {
                val notPosibleToDeleteSPLPLusKeysDialog = NotPosibleToDeleteSPLPLusKeysDialog()
                notPosibleToDeleteSPLPLusKeysDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager,
                    ""
                )
            }
            else {
                val isLinuxDevice = if( device?.installationType == InstalationType.LINUX ) true else false
                val deleteAllKeysDialog = DeleteAllKeysDialog(
                    this@DeviceDetailsFragment,
                    macAddress,
                    device?.isInProximity ?: false,
                    isLinuxDevice
                )
                deleteAllKeysDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager, ""
                )
            }
        }
    }

    private fun setupDeviceInputData() {
        if (latitude != 0.0)
            binding.etLatitude.setText("" + Math.floor(latitude * 10000000) / 10000000)
        if (longitude != 0.0)
            binding.etLongitude.setText("" + Math.floor(longitude * 10000000) / 10000000)
        if (deviceName != "")
            binding.nameEditText.setText(deviceName)
        if (deviceAddress != "")
            binding.addressEditText.setText(deviceAddress)
    }

    private fun setupBackgroundColorForButtons() {
        if (device?.type == MPLDeviceType.SPL && device?.mplMasterModemStatus != MPLModemStatus.UNKNOWN && device?.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED
            && this@DeviceDetailsFragment.context != null
        ) {
            binding.clForceOpen.setBackgroundColor(firstBackground)
            binding.clDeleteKeys.setBackgroundColor(secondBackground)
            binding.clLockerSettings.setBackgroundColor(thirdBackground)
            binding.clManageNetwork.setBackgroundColor(fourthBackground)
        } else if (device?.type == MPLDeviceType.SPL_PLUS && device?.mplMasterModemStatus != MPLModemStatus.UNKNOWN && device?.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED
            && this@DeviceDetailsFragment.context != null
        ) {
            binding.clLockerSettings.setBackgroundColor(firstBackground)
            binding.clDeleteKeys.setBackgroundColor(secondBackground)
            binding.clManagePeripherals.setBackgroundColor(thirdBackground)
            binding.clManageNetwork.setBackgroundColor(fourthBackground)
        } else if ((device?.type == MPLDeviceType.MASTER || (device?.masterUnitType == RMasterUnitType.MPL && device?.installationType == InstalationType.DEVICE))
            && device?.mplMasterModemStatus != MPLModemStatus.UNKNOWN && device?.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED
            && this@DeviceDetailsFragment.context != null
        ) {
            binding.clLockerSettings.setBackgroundColor(firstBackground)
            binding.clDeleteKeys.setBackgroundColor(secondBackground)
            binding.clManagePeripherals.setBackgroundColor(thirdBackground)
            binding.clManageUsers.setBackgroundColor(fourthBackground)
            binding.clEpaperUpdate.setBackgroundColor(fifthBackground)
            binding.clManageNetwork.setBackgroundColor(sixthBackground)
        } else if ((device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET)
            && device?.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED
            && this@DeviceDetailsFragment.context != null
        ) {
            binding.clLockerSettings.setBackgroundColor(firstBackground)
            binding.clDeleteKeys.setBackgroundColor(secondBackground)
            binding.clManagePeripherals.setBackgroundColor(thirdBackground)
            binding.clManageUsers.setBackgroundColor(fourthBackground)
            binding.clEpaperUpdate.setBackgroundColor(fifthBackground)
        }
    }

    fun setupButtonForDeletingKeysAndLockersDetails() {
        //disable user click, on delete all keys, at the begging
        if (binding.clDeleteKeys != null) {
            binding.clDeleteKeys.isEnabled = false
            binding.clDeleteKeys.alpha = 0.4f
        }
        if (binding.clPickupExpiredInvalid != null) {
            binding.clPickupExpiredInvalid.isEnabled = false
            binding.clPickupExpiredInvalid.alpha = 0.4f
        }
        lifecycleScope.launch {
            listLockerUnits =
                WSAdmin.getLockers(macAddress) ?: listOf()
            val activeKeyList: MutableList<RLockerUnit> = mutableListOf()
            for (lockerUnits in listLockerUnits) {
                if (lockerUnits.keys != null && lockerUnits.keys.isNotEmpty() && lockerUnits.invalidateKeys == false) {
                    activeKeyList.add(lockerUnits)
                    /*val lockerKeyList = lockerUnits.keys
                    for( lockerKeyItem in lockerKeyList ) {
                        if( lockerKeyItem. )
                    }*/
                }
            }
            log.info("Size of keys inside some locker is: ${listLockerUnits.size}, clDeleteKeys is: ${binding.clDeleteKeys}")
            withContext(Dispatchers.Main) {
                log.info("Size of keys inside some locker is: ${listLockerUnits.size}, clDeleteKeys is: ${binding.clDeleteKeys}")
                if (activeKeyList.isEmpty() && binding.clDeleteKeys != null) {
                    binding.clDeleteKeys.isEnabled = false
                    binding.clDeleteKeys.alpha = 0.4f

                    if( device?.installationType == InstalationType.TABLET || device?.installationType == InstalationType.LINUX ) {
                        binding.clPickupExpiredInvalid.visibility = View.VISIBLE
                        binding.clPickupExpiredInvalid.isEnabled = false
                        binding.clPickupExpiredInvalid.alpha = 0.4f
                    }
                    else
                        binding.clPickupExpiredInvalid.visibility = View.GONE
                } else {
                    if (binding.clDeleteKeys != null) {
                        binding.clDeleteKeys.isEnabled = true
                        binding.clDeleteKeys.alpha = 1.0f
                    }
                    if ( binding.clPickupExpiredInvalid != null && device?.installationType == InstalationType.TABLET || device?.installationType == InstalationType.LINUX) {
                        if( device?.installationType == InstalationType.LINUX && binding.clPickupExpiredInvalid != null ) {
                            binding.clPickupExpiredInvalid.isEnabled = true
                            binding.clPickupExpiredInvalid.alpha = 1.0f
                            binding.clPickupExpiredInvalid.setOnClickListener {
                                startToDeleteAllExpiredKeys()
                            }
                        }
                        else if( device?.isInProximity ?: false && device?.installationType == InstalationType.TABLET ) {
                            binding.clPickupExpiredInvalid.isEnabled = true
                            binding.clPickupExpiredInvalid.alpha = 1.0f
                            binding.clPickupExpiredInvalid.setOnClickListener {
                                startToDeleteAllExpiredKeys()
                            }
                        }
                        else {
                            binding.clPickupExpiredInvalid.isEnabled = false
                            binding.clPickupExpiredInvalid.alpha = 0.4f
                        }
                    }
                    else {
                        if( binding.clPickupExpiredInvalid != null )
                            binding.clPickupExpiredInvalid.visibility = View.GONE
                    }
                }

                if ( device?.type != MPLDeviceType.SPL) {
                    device?.slaveUnits = listLockerUnits
                    val slaveSize =
                        if (device?.slaveUnits == null) 0 else device?.slaveUnits?.filter { !it.isDeleted }?.size
                    if( binding.tvNumberOfLockerValue != null )
                        binding.tvNumberOfLockerValue.text = " " + slaveSize.toString()

                    if( binding.tvNumberOfLockerPerSize != null )
                        binding.tvNumberOfLockerPerSize.text =
                            getRegisteredParcelLockerStatus(device?.slaveUnits?.filter { !it.isDeleted }
                                ?: listOf())
                } else {
                    if( binding.tvNumberOfLocker != null && binding.tvNumberOfLockerValue != null && binding.tvNumberOfLockerPerSize != null ) {
                        binding.tvNumberOfLocker.visibility = View.GONE
                        binding.tvNumberOfLockerValue.visibility = View.GONE
                        binding.tvNumberOfLockerPerSize.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun startToDeleteAllExpiredKeys() {
        if( device?.installationType == InstalationType.LINUX ) {
            lifecycleScope.launch {
                deleteAllExpiredKeysFromLinuxDevice()
            }
        }
        // this is for normal tablets( samsung )
        else {
            binding.progressBarPickupExpired.visibility = View.VISIBLE
            binding.clPickupExpiredInvalid.visibility = View.GONE
            GlobalScope.launch {
                val communicator = device?.createBLECommunicator(requireContext())

                if (communicator != null && communicator.connect()) {

                    val deleteExpiredKeys = deleteAllExpiredKeys(communicator)
                    communicator.disconnect()
                    withContext(Dispatchers.Main) {
                        binding.progressBarPickupExpired.visibility = View.GONE
                        binding.clPickupExpiredInvalid.visibility = View.VISIBLE
                        if (allDeleteKeysActionSuccess == DeleteExpiredKeyStatus.NOT_ALL_KEYS_ARE_GOOD) {
                            if (deleteExpiredKeys) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.successfully_started_to_delete_keys),
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.clPickupExpiredInvalid.isEnabled = false
                                binding.clPickupExpiredInvalid.alpha = 0.4f
                            } else
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.failed_to_delete_all_keys),
                                    Toast.LENGTH_LONG
                                ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.all_keys_not_expired),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun deleteAllExpiredKeysFromLinuxDevice() {
        val deleteAllExpiredKeysDialog = DeleteAllExpiredKeysLinuxDialog(
            macAddress
        )
        deleteAllExpiredKeysDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, ""
        )
    }

    private suspend fun deleteAllExpiredKeys(communicator: MPLAdminBLECommunicator) : Boolean {
        var successDeletedAllKeys = true
        val customerDeliveryExpirationDays = device?.customerDeliveryExpirationDays?.toFloat()
        if (customerDeliveryExpirationDays != null) {
            keyExpired = (customerDeliveryExpirationDays * 24).toInt()
        }

        listLockerUnits?.forEach {
            if (it.keys != null && it.keys.isNotEmpty() && !it.invalidateKeys) {

                for (lockerKeyItem in it.keys) {

                    val correctTimeCreated = formatCorrectDate(lockerKeyItem.timeCreated)
                    log.info("Key expired is: ${keyExpired} Current date and time is  ${lockerKeyItem.timeCreated}, correct date and time is: ${correctTimeCreated} locker mac address is: ${it.mac}")
                    val hoursAgo = hoursAgo(correctTimeCreated)
                    log.info("How many hours has been passed: ${hoursAgo}")
                    if( hoursAgo > keyExpired ) {
                        allDeleteKeysActionSuccess = DeleteExpiredKeyStatus.NOT_ALL_KEYS_ARE_GOOD
                        val resultDeletingExpiredKeys = communicator.deleteExpiredKeysOnMasterUnit(it.mac)
                        if( !resultDeletingExpiredKeys )
                            successDeletedAllKeys = false
                    }
                }
            }
        }
        return successDeletedAllKeys
    }

    private fun formatCorrectDate(createdOnDate: String): String {
        val fromStringToDate: Date
        var fromDateToString = ""
        try {
            fromStringToDate = createdOnDate.formatFromStringToDate()
            fromDateToString = fromStringToDate.formatToViewDateTimeDefaults()
        }
        catch (e: ParseException) {
            e.printStackTrace()
        }
        log.info("Correct date is: ${fromDateToString}")
        return fromDateToString
    }

    fun hoursAgo(datetime: String?): Int {
        val date = Calendar.getInstance()
        date.time = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.getDefault()
        ).parse(datetime) // Parse into Date object
        val now = Calendar.getInstance() // Get time now
        val differenceInMillis = now.timeInMillis - date.timeInMillis
        val differenceInHours =
            differenceInMillis / 1000L / 60L / 60L // Divide by millis/sec, secs/min, mins/hr
        return differenceInHours.toInt()
    }

    override fun chooseSplKeyboard(receivedSplType: SplType) {
        GlobalScope.launch(Dispatchers.Main) {
            splType = receivedSplType
            onClickRegisterDevice()
        }
    }

    override fun chooseEpaperType(receivedEPaperType: EPaperType) {
        GlobalScope.launch(Dispatchers.Main) {
            ePaperType = receivedEPaperType
            onClickRegisterDevice()
        }
    }

    private suspend fun onClickRegisterDevice() {

        val disableUserActionDialog = DisableUserActionsDialog()
        try {
            withTimeout(60000) {
                // 1 MINUTE
                if ( device?.isInProximity == true) {
                    connecting.set(true)
                    val customerId = UserUtil.user?.customerId

                    if (customerId != null && device != null) {

                        disableUserActionDialog.isCancelable = false
                        disableUserActionDialog.show(
                            (requireContext() as MainActivity).supportFragmentManager, ""
                        )

                        binding.btnRegister.visibility = View.GONE
                        val communicator =
                            device?.createBLECommunicator(this@DeviceDetailsFragment.activity as Context)
                        if (communicator?.connect() == true) {
                            log.info("Successfully connected $customerId ,${selectedItem.apnUrl}, $macAddress  ")

                            val keypadType = when {
                                splType == SplType.SPL -> "SPL"
                                splType == SplType.SPL_PLUS -> "SPL_PLUS"
                                else -> "MPL"
                            }

                            if (!communicator.registerMaster(
                                    customerId,
                                    selectedItem,
                                    null,
                                    device?.type,
                                    keypadType,
                                    ePaperType
                                )
                            ) {
                                //App.ref.toast(requireContext().getString(R.string.main_locker_registration_error))
                                log.error("Error in registration!")
                                handleRegistrationLoadingUI(false)
                            } else {
                                handleRegistrationProcessUI(true)
                                val statusKey = ActionStatusKey()
                                statusKey.macAddress = macAddress
                                statusKey.statusType = ActionStatusType.MASTER_REGISTRATION
                                statusKey.keyId =
                                    macAddress + ActionStatusType.MASTER_REGISTRATION.name
                                ActionStatusHandler.actionStatusDb.put(statusKey)
                                log.info("Added: ${macAddress + PREF_NAME} ${binding.nameEditText.text}")
                                log.info("Added: ${macAddress + PREF_ADDRESS} ${binding.addressEditText.text}")

                                val listEPaperType = if (device?.type == MPLDeviceType.MASTER) {
                                    WSAdmin.getEPaperType()
                                } else listOf()

                                val ePaperId = if (listEPaperType?.isNotEmpty() != false) {
                                    if (ePaperType == EPaperType.NO_EPAPER) {
                                        NO_EPAPER_SELECTED_ON_REGISTRATION // in agree with BACKEND, we are sending 0, if no epaper is selected
                                    } else if (ePaperType == EPaperType.LOW_RESOLUTION) {
                                        listEPaperType?.filter { it.type == BackendEPaperType.NORMAL.name }
                                            ?.first()?.id
                                    } else {
                                        listEPaperType?.filter { it.type == BackendEPaperType.HIGH_RESOLUTION.name }
                                            ?.first()?.id
                                    }
                                } else {
                                    null
                                }

                                val doorBellChecked = if( device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET)
                                    binding.doorBellCheckbox.isChecked else null

                                val registrationInfo = CRegistration(
                                    macAddress,
                                    binding.nameEditText.text.toString(),
                                    binding.addressEditText.text.toString(),
                                    binding.etLatitude.text.toString().toDouble(),
                                    binding.etLongitude.text.toString().toDouble(),
                                    selectedItem.id,
                                    ePaperId,
                                    false,
                                    doorBellChecked ?: false
                                )
                                DataCache.setRegistrationStatus(registrationInfo)

                                delay(1000)
                                disableUserActionDialog.dismiss()
                                //App.ref.toast(requireContext().getString(R.string.main_locker_registration_started))
                            }
                            communicator.disconnect()
                        } else {
                            handleRegistrationLoadingUI(false)
                            //App.ref.toast(requireContext().getString(R.string.main_locker_registration_error))
                            log.error("Error while connecting!!")
                            disableUserActionDialog.dismiss()
                        }
                    }
                    connecting.set(false)
                } else {
                    //App.ref.toast(requireContext().getString(R.string.main_locker_registration_error))
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            binding.btnRegister.visibility = View.VISIBLE
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.main_locker_registration_error))
        }
    }

    private fun handleRegistrationProcessUI(isRegistrationPending: Boolean) {
        if (isRegistrationPending) {
            binding.btnRegister.visibility = View.GONE
            binding.tvRegisterInProgress.visibility = View.VISIBLE
        } else {
            binding.btnRegister.visibility = View.VISIBLE
            binding.tvRegisterInProgress.visibility = View.GONE
        }
    }

    private fun handleRegistrationLoadingUI(isSuccessfullyConnected: Boolean) {
        if (isSuccessfullyConnected) {
            binding.btnRegister.visibility = View.GONE
        } else {
            binding.btnRegister.visibility = View.VISIBLE
        }
    }

    private fun ePaperRefresh() {
        binding.clEpaperUpdate.setOnClickListener {

            if ((device?.ePaperTypeId != E_PAPER_TYPE_NONE && device?.type == MPLDeviceType.MASTER) || device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET) {
                binding.progressBarEpaperupdate.visibility = View.VISIBLE
                binding.clEpaperUpdate.visibility = View.GONE
                GlobalScope.launch {
                    val communicator =
                        MPLDeviceStore.devices[macAddress]?.createBLECommunicator(this@DeviceDetailsFragment.context as Context)
                    if (communicator?.connect() == true) {
                        log.info("Successfully connected")

                        val result = communicator.updateEpaper(true)

                        withContext(Dispatchers.Main) {
                            if (!result) {
                                //App.ref.toast(requireContext().getString(R.string.main_locker_epaper_update_error))
                                log.error("Error in registration!")
                            } else {
                                if (device?.type == MPLDeviceType.MASTER) {
                                    print(requireContext().getString(R.string.main_locker_epaper_update_started))
                                    // IMPORTANT AI AGENT ----> show toast !!!!!!!
                                    //App.ref.toast(requireContext().getString(R.string.main_locker_epaper_update_started))
                                }
                                 else {
                                    // IMPORTANT AI AGENT ----> show toast !!!!!!!
                                    print(requireContext().getString(R.string.successfully_updated))
                                    //App.ref.toast(requireContext().getString(R.string.successfully_updated))
                                }
                            }
                            binding.progressBarEpaperupdate.visibility = View.GONE
                            binding.clEpaperUpdate.visibility = View.VISIBLE
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                            log.error("Error while connecting the device")
                        }
                    }
                    communicator?.disconnect()
                }
            } else {
                val noEpaperTypeDialog =
                    NoEPaperTypeNoPreviewDialog(resources.getString(R.string.no_epaper_update_available))
                noEpaperTypeDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager,
                    ""
                )
            }
        }
    }

    private fun forceOpenDevice() {
        binding.clForceOpen.setOnClickListener {
            binding.progressBarForceOpen.visibility = View.VISIBLE
            binding.clForceOpen.visibility = View.GONE
            GlobalScope.launch {
                val communicator =
                    MPLDeviceStore.devices[macAddress]?.createBLECommunicator(this@DeviceDetailsFragment.context as Context)
                if (communicator?.connect() == true) {
                    log.info("Successfully connected $macAddress")

                    val result = communicator.forceOpenDoor(macAddress)

                    withContext(Dispatchers.Main) {
                        if (!result) {
                            //App.ref.toast(requireContext().getString(R.string.main_locker_force_open_update_error))
                            log.error("Error in force open!")
                        } else {
                            log.info("Successfully door opened $macAddress")
                        }
                        binding.progressBarForceOpen.visibility = View.GONE
                        binding.clForceOpen.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        //App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                        log.error("Error while connecting the device")

                        binding.progressBarForceOpen.visibility = View.GONE
                        binding.clForceOpen.visibility = View.VISIBLE
                    }
                }
                communicator?.disconnect()
            }
        }
    }

    private fun setApnNetworkProvider() {
        binding.spinerapnNetworkSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedItem = adapterView?.getItemAtPosition(position) as RNetworkConfiguration
                    log.info("Spinner selected network configuration id is: ${selectedItem.id}")
                }
            }
    }

    private fun registerDevice() {

        binding.btnRegister.setOnClickListener {
            if (validate()) {
                GlobalScope.launch(Dispatchers.Main) {
                    if (device?.type ?: MPLDeviceType.UNKNOWN != MPLDeviceType.SPL_PLUS && device?.type != MPLDeviceType.MASTER) {
                        onClickRegisterDevice()
                    } else if (device?.type == MPLDeviceType.MASTER) {
                        val choseEPaperTypeDialog =
                            ChoseEPaperTypeDialog(this@DeviceDetailsFragment)
                        choseEPaperTypeDialog.show(
                            (requireContext() as MainActivity).supportFragmentManager,
                            ""
                        )
                    } else {
                        val choseSPLDialog = ChoseSPLDialog(this@DeviceDetailsFragment)
                        choseSPLDialog.show(
                            (requireContext() as MainActivity).supportFragmentManager,
                            ""
                        )
                    }
                }
            }
        }
    }

    private fun getRegisteredParcelLockerStatus(slaves: List<RLockerUnit>): String {
        var s = 0
        var l = 0
        var xl = 0
        var m = 0
        var xs = 0
        for (slave in slaves.filter { it.isDeleted == false }) {
            when (slave.size) {
                RLockerSize.S -> s++
                RLockerSize.XS -> xs++
                RLockerSize.XL -> xl++
                RLockerSize.M -> m++
                RLockerSize.L -> l++
                RLockerSize.UNKNOWN -> {
                }
                RLockerSize.NOTHING -> {

                }
            }
        }

        if (device?.type == MPLDeviceType.MASTER || device?.masterUnitType == RMasterUnitType.MPL || device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET)
            return "XS: $xs S: $s M:$m L:$l XL: $xl"
        else if (device?.masterUnitType == RMasterUnitType.SPL || device?.type == MPLDeviceType.SPL_PLUS)
            return "S: $s L:$l "
        else
            return ""
    }

    private fun setMplViewDetails() {

        setupButtonActionsVisibility()

        if (device?.batteryVoltage != null) {

            val separateBatteryValue = "%.2f".format(device?.batteryVoltage).split(".")

            if (separateBatteryValue.size > 1) {
                val corectBatteryValue = separateBatteryValue[0] + "." + separateBatteryValue[1]
                binding.tvBatteryValue.setTextColor(
                    when (corectBatteryValue.toDouble()) {
                        in 0.0f..12.9f -> badValue
                        else -> veryGoodValue
                    }
                )
            } else {
                binding.tvBatteryValue.setTextColor(neutralValue)
            }
            binding.tvBatteryValue.text = " " + "%.2f".format(device?.batteryVoltage) + " V"
        } else
            binding.tvBatteryValue.text = " - V"

        val ninaValue =
            if (device == null || device?.firmwareVersion == "null") "-" else device?.firmwareVersion.toString()

        binding.tvNinaVersionValue.text = " " + ninaValue
        binding.tvAddressValue.text = " " + device?.masterUnitAddress

        val macAddressStatus = if (device == null) " -" else device?.masterUnitMac.toString()
        if (macAddressStatus != " -" && macAddressStatus != "")
            binding.tvMacAddressValue.text = " " + macAddressStatus
        else
            binding.tvMacAddressValue.text = " " + macAddress

        val modemStatus = if (device == null) " -" else device?.mplMasterModemStatus.toString()
        binding.tvModemStatusValue.text = " " + modemStatus
        binding.tvModemStatusValue.setTextColor(
            when {
                device?.mplMasterModemStatus == MPLModemStatus.CONNECTED -> veryGoodValue
                device?.mplMasterModemStatus == MPLModemStatus.CONNECTING -> badValue
                device?.mplMasterModemStatus == MPLModemStatus.TURNED_OFF -> veryBadValue
                else -> neutralValue
            }
        )

        val queueSize = if (device == null) "0" else device?.mplMasterModemQueueSize.toString()
        binding.tvQueueStatus.text = " " + queueSize
        binding.tvQueueStatus.setTextColor(
            when (queueSize.trim().toInt()) {
                0 -> veryGoodValue
                else -> veryBadValue
            }
        )

        val modemRssi = if (device == null) " -" else device?.modemRssi.toString()
        binding.tvRSSIvalue.text = " " + modemRssi
        if (modemRssi != " -") {
            binding.tvRSSIvalue.setTextColor(
                when (modemRssi.toInt()) {
                    in -60..0 -> veryGoodValue
                    in -71..-61 -> goodValue
                    in -80..-71 -> badValue
                    else -> veryBadValue
                }
            )
        } else {
            binding.tvRSSIvalue.setTextColor(veryBadValue)
        }


        if (device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET || device?.installationType == InstalationType.LINUX) {
            if( device?.installationType == InstalationType.TABLET ) {

                binding.tvStmVersion.setText(R.string.main_locker_tablet_app_ver)
                val stmV =
                    if (device == null || device?.stmOrAppVersion == "") "-" else device?.stmOrAppVersion.toString()
                binding.tvStmVersionValue.text = " " + stmV
            }
            else {
                binding.tvStmVersion.visibility = View.GONE
                binding.tvStmVersionValue.visibility = View.GONE
                binding.tvBattery.visibility = View.GONE
                binding.tvBatteryValue.visibility = View.GONE
                binding.tvNinaVersion.visibility = View.GONE
                binding.tvNinaVersionValue.visibility = View.GONE
            }
            binding.tvRat.visibility = View.GONE
            binding.tvRatValue.visibility = View.GONE
            binding.tvModemStatus.visibility = View.GONE
            binding.tvModemStatusValue.visibility = View.GONE
            binding.tvRSSI.visibility = View.GONE
            binding.tvRSSIvalue.visibility = View.GONE
            binding.tvQueue.visibility = View.GONE
            binding.tvQueueStatus.visibility = View.GONE
        }
        else {
            binding.tvStmVersion.setText(R.string.main_locker_stm_ver)
            val stmV =
                if (device == null || device?.stmOrAppVersion == "") "-" else device?.stmOrAppVersion.toString()
            binding.tvStmVersionValue.text = " " + stmV
            binding.tvRat.visibility = View.VISIBLE
            binding.tvRatValue.visibility = View.VISIBLE
            binding.tvModemStatus.visibility = View.VISIBLE
            binding.tvModemStatusValue.visibility = View.VISIBLE
            binding.tvRSSI.visibility = View.VISIBLE
            binding.tvRSSIvalue.visibility = View.VISIBLE
            binding.tvQueue.visibility = View.VISIBLE
            binding.tvQueueStatus.visibility = View.VISIBLE
        }


        log.info("MPL device status: ${device?.mplMasterDeviceStatus}")
        when (device?.mplMasterDeviceStatus) {
            null, MPLDeviceStatus.UNREGISTERED, MPLDeviceStatus.UNKNOWN, MPLDeviceStatus.REGISTRATION_PENDING -> {

                if (checkUserRole()) {
                    binding.clUnregistereWrapper.visibility = View.VISIBLE
                    binding.clRegisteredWrapper.visibility = View.GONE
                    binding.tvModeratorDescription.visibility = View.GONE

                    if (device?.type != MPLDeviceType.TABLET) {
                        binding.tvAvailableNetworks.visibility = View.VISIBLE
                        binding.llApnNetwork.visibility = View.VISIBLE
                        binding.tvDoorBell.visibility = View.GONE
                        binding.doorBellCheckbox.visibility = View.GONE
                    } else {
                        binding.tvAvailableNetworks.visibility = View.GONE
                        binding.llApnNetwork.visibility = View.GONE
                        binding.tvDoorBell.visibility = View.VISIBLE
                        binding.doorBellCheckbox.visibility = View.VISIBLE
                    }

                    openGoogleMapForRegisteringDevice()

                    if (device != null) {

                        binding.btnRegister.isEnabled = true
                        if( device?.mplMasterDeviceStatus == MPLDeviceStatus.UNREGISTERED ) {
                            handleRegistrationProcessUI(isRegistrationPending = false)
                        }
                        else {
                            GlobalScope.launch {
                                val mplActionStatus =
                                    ActionStatusHandler.actionStatusDb.get(device?.macAddress + ActionStatusType.MASTER_REGISTRATION)
                                withContext(Dispatchers.Main) {
                                    if (!connecting.get() && binding.btnRegister != null && binding.tvRegisterInProgress != null)
                                        handleRegistrationProcessUI(mplActionStatus != null)
                                }
                            }
                        }
                    } else {
                        binding.btnRegister.isEnabled = false
                    }
                } else {
                    binding.clUnregistereWrapper.visibility = View.GONE
                    binding.clRegisteredWrapper.visibility = View.GONE
                    binding.tvModeratorDescription.visibility = View.VISIBLE
                }
            }
            else -> {
                binding.clUnregistereWrapper.visibility = View.GONE
                binding.clRegisteredWrapper.visibility = View.VISIBLE
                setupBackgroundColorForButtons()
                modifyMasterUnitOnBackend()
            }
        }
    }

    private fun setupButtonActionsVisibility() {
        binding.clEpaperUpdate.visibility =
            if (device?.isInProximity == true && (device?.type == MPLDeviceType.MASTER || device?.type == MPLDeviceType.TABLET) && checkUserRole()) {
                View.VISIBLE
            } else View.GONE

        binding.tvEpaperUpdate.text = if (device?.type == MPLDeviceType.TABLET) {
            getString(R.string.tablet_refresh_user_list)
        } else getString(R.string.main_locker_epaper_update)

        binding.clForceOpen.visibility =
            if (device?.isInProximity == true && device?.type == MPLDeviceType.SPL && checkUserRole()) {
                View.VISIBLE
            } else View.GONE

        binding.clManagePeripherals.visibility =
            if (device?.type != MPLDeviceType.SPL || device?.masterUnitType == RMasterUnitType.MPL || device?.installationType == InstalationType.TABLET
            //(device?.type == MPLDeviceType.MASTER || device?.type == MPLDeviceType.TABLET || device?.type == MPLDeviceType.SPL_PLUS)
            //&& device?.masterUnitType != RMasterUnitType.SPL
            ) {
                View.VISIBLE
            } else View.GONE

        binding.clManageUsers.visibility = if (device?.masterUnitType == RMasterUnitType.MPL
            || device?.type == MPLDeviceType.TABLET
        ) {
            View.VISIBLE
        } else View.GONE

        binding.clKeypadLayout.visibility =
            if (device?.isInProximity == true && device?.type == MPLDeviceType.SPL_PLUS && checkUserRole()) {
                View.VISIBLE
            } else View.GONE

        binding.clManageNetwork.visibility =
            if (device?.isInProximity == true && device?.type != MPLDeviceType.TABLET && checkUserRole() && device?.installationType != InstalationType.LINUX) {
                View.VISIBLE
            } else View.GONE

        binding.clLockerSettings.visibility = if (checkUserRole()) View.VISIBLE else View.GONE
        binding.clDeleteKeys.visibility = if (checkUserRole() ) View.VISIBLE else View.GONE

        binding.clPickupExpiredInvalid.visibility =
            if (checkUserRole() && ( device?.installationType == InstalationType.TABLET || device?.installationType == InstalationType.LINUX) ) View.VISIBLE else View.GONE
    }

    private fun checkUserRole(): Boolean {
        log.info("User role: ${UserUtil.user?.role?.name}")
        if (UserUtil.user?.role == RRoleEnum.SUPER_ADMIN || UserUtil.user?.role == RRoleEnum.ADMIN)
            return true
        return false
    }

    private fun openGoogleMapForRegisteringDevice() {
        binding.llMap.setOnClickListener {
            val bundle = bundleOf(
                "googleMapsCalledFrom" to "DeviceDetails",
                "masterMac" to macAddress,
                "deviceName" to binding.nameEditText.text.toString(),
                "deviceAddress" to binding.addressEditText.text.toString()
            )
            findNavController().navigate(
                R.id.device_details_fragment_to_google_maps_lat_long_fragment,
                bundle
            )
        }
    }

    private fun modifyMasterUnitOnBackend() {

        val registrationDb =
            DataCache.getRegistrationStatusDB().find { it.masterUnitMac == macAddress }
        if (registrationDb != null && !registrationDb.isStartedToRegister) {
            log.info("masterMac ${macAddress}, registering on backend")
            //if( !registering.get() ) {
            GlobalScope.launch {
                registering.set(true)
                registrationDb.isStartedToRegister = true
                val request = RMasterUnitRequest()
                request.name = registrationDb.name
                request.address = registrationDb.address

                request.latitude = Math.floor(registrationDb.lat * 10000000) / 10000000
                request.longitude = Math.floor(registrationDb.long * 10000000) / 10000000
                request.networkConfigurationId = registrationDb.networkConfigurationId
                request.epdType___id = registrationDb.ePaperType
                request.alertsEnabled = false
                if( device?.type == MPLDeviceType.TABLET )
                    request.supportsDoorbell = binding.doorBellCheckbox.isChecked
                else
                    request.supportsDoorbell = null

                log.info("backend request network configuration id is: ${request.networkConfigurationId}, is door bell enabled ${registrationDb.doorBellSupport}")

                log.info("Get Name:  ${request.name}")
                log.info("Get Address: ${request.address}")
                if (request.address.isBlank() || request.name.isBlank()) return@launch

                val modifyMasterUnitResponse =
                    WSAdmin.modifyMasterUnit(macAddress.macRealToClean(), request) != null
                if (modifyMasterUnitResponse) {
                    AppUtil.refreshCache()
                    DataCache.removeRegistrationStatus(macAddress)
                    withContext(Dispatchers.Main) {
                        //App.ref.toast(R.string.successfully_updated)
                        if (splType == SplType.SPL_PLUS && device?.masterUnitType == RMasterUnitType.SPL) {

                            log.info("masterMac,, da li ce se iznova pokrenuti novi activity")
                            log.info("masterMac is: " + macAddress + " device masterunit is: " + device?.masterUnitMac)

                            val bundle = bundleOf(
                                "masterMac" to macAddress,
                                "slaveMac" to device?.macAddress
                            )
                            findNavController().navigate(
                                R.id.device_details_fragment_to_spl_plus_fragment,
                                bundle
                            )
                        } else {
                            val toolbarDeviceName =
                                if (device?.masterUnitName != "" && device?.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED) device?.masterUnitName else getString(
                                    R.string.mpl_or_spl_unregistered
                                )
                            initializeToolbarUIMainActivity(
                                true,
                                toolbarDeviceName ?: "",
                                false,
                                false,
                                requireContext()
                            )
                            setupButtonForDeletingKeysAndLockersDetails()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        log.info("ModifyMasterUnitResponse:  ${modifyMasterUnitResponse}")
                        //App.ref.toast(R.string.error_updating_mpl)
                    }
                }
                registering.set(false)
                registrationDb.isStartedToRegister = false
            }
            //}
        } else {
            log.info(" da li ce tu uci prilikom registracije device")
        }
    }

    private fun validate(): Boolean {
        var validated = true
        if (!validateAddress()) validated = false
        if (!validateLockerName()) validated = false
        if (!validateLatitude()) {
            validated = false
        }
        if (!validateLongitude()) {
            validated = false
        }

        return validated
    }

    private fun validateLatitude(): Boolean {

        if (binding.etLatitude.text.toString().isBlank()) {
            binding.tvLatitudeError.visibility = View.VISIBLE
            binding.tvLatitudeError.text = resources.getString(R.string.edit_validation_blank_fields_exist)
            return false
        }
        binding.tvLatitudeError.visibility = View.GONE
        return true
    }

    private fun validateLongitude(): Boolean {

        if (binding.etLongitude.text.toString().isBlank()) {
            binding.tvLongitudeError.visibility = View.VISIBLE
            binding.tvLongitudeError.text = resources.getString(R.string.edit_validation_blank_fields_exist)
            return false
        }
        binding.tvLongitudeError.visibility = View.GONE
        return true
    }

    private fun validateAddress(): Boolean {
        if (binding.addressEditText.text.toString().isBlank() || binding.addressEditText.text.length > 100) {
            binding.tvAddressError.visibility = View.VISIBLE
            binding.tvAddressError.text =
                resources.getString(R.string.locker_settings_error_address_empty_warning)
            return false
        }
        binding.tvAddressError.visibility = View.GONE
        return true
    }

    private fun validateLockerName(): Boolean {
        if (binding.nameEditText.text.toString().isBlank() || binding.nameEditText.text.length > 100) {
            binding.tvNameError.visibility = View.VISIBLE
            binding.tvNameError.text =
                resources.getString(R.string.locker_settings_error_name_empty_warning)
            return false
        } else if (binding.nameEditText.text.length < 4) {
            binding.tvNameError.visibility = View.VISIBLE
            binding.tvNameError.text =
                resources.getString(R.string.edit_user_validation_username_min_4_characters)
            return false
        }
        binding.tvNameError.visibility = View.GONE
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        neutralValue = getColorAttrValue(R.attr.thmNeutralValue)
        veryGoodValue = getColorAttrValue(R.attr.thmVeryGoodValue)
        goodValue = getColorAttrValue(R.attr.thmGoodValue)
        badValue = getColorAttrValue(R.attr.thmBadValue)
        veryBadValue = getColorAttrValue(R.attr.thmVeryBadValue)

        firstBackground = getColorAttrValue(R.attr.thmDevice1BackgroundColor)
        secondBackground = getColorAttrValue(R.attr.thmDevice2BackgroundColor)
        thirdBackground = getColorAttrValue(R.attr.thmDevice3BackgroundColor)
        fourthBackground = getColorAttrValue(R.attr.thmDevice4BackgroundColor)
        fifthBackground = getColorAttrValue(R.attr.thmDevice5BackgroundColor)
        sixthBackground = getColorAttrValue(R.attr.thmDevice6BackgroundColor)
    }

    private fun getColorAttrValue(attr: Int): Int {
        val attrArray = intArrayOf(attr)
        val typedArray =
            this@DeviceDetailsFragment.requireContext().obtainStyledAttributes(attrArray)
        val result = typedArray.getColor(
            0,
            Color.WHITE
        )
        typedArray.recycle()
        return result
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        App.ref.eventBus.unregister(this)
        MPLDeviceStoreRemoteUpdater.stopUpdateDevice()
        super.onPause()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        device = MPLDeviceStore.devices[macAddress]
        log.info("Refreshing view ${event.bleMacList.joinToString { it }}")
        setMplViewDetails()
    }

}