package  hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.LockerSettingsInterface
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentLockerSettingsBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.util.AppUtil
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.*
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.* 

class LockerSettingsFragment : BaseFragment(), LockerSettingsInterface {

    val log = logger()
    lateinit var macAddress: String
    private var device: MPLDevice? = null

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var deviceAddress: String = ""
    private var NO_EPAPER_SELECTED_ON_REGISTRATION = 0

    private lateinit var binding: FragmentLockerSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLockerSettingsBinding.inflate(layoutInflater)

        initializeToolbarUIMainActivity(
            true,
            getString(R.string.main_locker_settings),
            false,
            false,
            requireContext()
        )

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        macAddress = arguments?.getString("masterMac", "") ?: ""
        latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
        longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0
        deviceAddress = arguments?.getString("deviceAddress", "") ?: ""
        log.info("Received masterMac address is: " + macAddress)
        device = MPLDeviceStore.devices[macAddress]

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeValuesToElements()

        if (device?.installationType == InstalationType.LINUX)
            initializeUiForLinuxDevices()
        else if (device?.isInProximity == true)
            initializeUiDeviceIsInProximity()
        else
            initializeUiDeviceIsNotInProximity()

        log.info("EPaper type is: ${device?.ePaperTypeId}")
        if (device?.type == MPLDeviceType.MASTER) {
            setCorrectRadioButtonForEPaper()
        }

        setupLatitudeAndLongitude()
        openGoogleMapForRegisteringDevice()

        systemRebootClickListener()
        deleteDeviceClickListener()
        saveChangesSetOnClickListener()
    }

    private fun initializeUiForLinuxDevices() {
        binding.tvPinManagment.visibility = View.GONE
        binding.pinSavingCheckBox.visibility = View.GONE
        binding.tvPowerSaving.visibility = View.GONE
        binding.powerSavingCheckBox.visibility = View.GONE
        binding.tvDoorBell.visibility = View.GONE
        binding.doorBellCheckbox.visibility = View.GONE
        binding.clEPaperType.visibility = View.GONE
        binding.clSystemReboot.visibility = View.GONE
        binding.clDeleteDevice.visibility = View.GONE
    }

    private fun initializeValuesToElements() {
        binding.nameEditText.setText(device?.masterUnitName ?: "")
        binding.addressStreetEditText.setText(device?.masterUnitStreet ?: "")
        binding.addressHouseNumberEditText.setText(device?.masterUnitHouseNumber ?: "")
        binding.addressPostcodeEditText.setText(device?.masterUnitPostcode ?: "")
        binding.addressTownEditText.setText(device?.masterUnitTown ?: "")
        binding.addressCountryEditText.setText(device?.masterUnitCountry ?: "")
        binding.etLatitude.setText("" + device?.latitude)
        binding.etLongitude.setText("" + device?.longitude)

        binding.pinSavingCheckBox.isChecked = device?.allowPinSave ?: false
        binding.powerSavingCheckBox.isChecked = device?.powerSaving ?: false

        binding.powerSavingCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                val generatedPinDialog = PowerSavingWarningDialog(this@LockerSettingsFragment)
                generatedPinDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager,
                    ""
                )
            }
        }

        if (device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET) {
            binding.doorBellCheckbox.isChecked = device?.doorBellSupport ?: false
            binding.enableCourierPinIdentification.isChecked = device?.barcodeScannerEnabled ?: false
            binding.enableBarCodeScanning.isChecked = device?.useExternalBarcodeScanner ?: false
        }

        if (device?.installationType == InstalationType.LINUX) {
            binding.enableCourierPinIdentification.isChecked = device?.barcodeScannerEnabled ?: false
            binding.enableBarCodeScanning.isChecked = device?.useExternalBarcodeScanner ?: false
            binding.userIdentification.isChecked = device?.endUserIdenfiticationEnabled ?: true
            binding.pahKeysEnabled.isChecked = device?.pahEnabled ?: true
        }
    }

    private fun setCorrectRadioButtonForEPaper() {

        lifecycleScope.launch() {

            val ePaperTypeList = DataCache.getEPaperTypes()
            val result = ePaperTypeList.filter { it.id == device?.ePaperTypeId }.firstOrNull()
            withContext(Dispatchers.Main) {
                when {
                    result == null -> binding.noEpaper.isChecked = true
                    result.type == BackendEPaperType.NORMAL.name -> binding.ePaperLowResolution.isChecked =
                        true
                    else -> binding.ePaperHighResolution.isChecked = true
                }
            }
        }
    }

    private fun saveChangesSetOnClickListener() {
        val initializedPowerSavingState = binding.powerSavingCheckBox.isChecked
        log.info("Is power saving enabled: " + initializedPowerSavingState)
        binding.btnSaveChanges.setOnClickListener {
            if (binding.nameEditText.text.isNullOrBlank()) {
                binding.tvNameError.visibility = View.VISIBLE
                return@setOnClickListener
            } else
                binding.tvNameError.visibility = View.GONE

            if ( binding.addressStreetEditText.text.isNullOrBlank()) {
                binding.tvAddressStreetError.visibility = View.VISIBLE
                return@setOnClickListener
            } else
                binding.tvAddressStreetError.visibility = View.GONE

            if ( binding.addressHouseNumberEditText.text.isNullOrBlank()) {
                binding.tvAddressHouseNumberError.visibility = View.VISIBLE
                return@setOnClickListener
            } else
                binding.tvAddressHouseNumberError.visibility = View.GONE

            if ( binding.addressPostcodeEditText.text.isNullOrBlank()) {
                binding.tvAddresspostcodeError.visibility = View.VISIBLE
                return@setOnClickListener
            } else
                binding.tvAddresspostcodeError.visibility = View.GONE

            if ( binding.addressTownEditText.text.isNullOrBlank()) {
                binding.tvAddressTownError.visibility = View.VISIBLE
                return@setOnClickListener
            } else
                binding.tvAddressTownError.visibility = View.GONE

            if ( binding.addressCountryEditText.text.isNullOrBlank()) {
                binding.tvAddressCountryError.visibility = View.VISIBLE
                return@setOnClickListener
            } else
                binding.tvAddressCountryError.visibility = View.GONE

            if (binding.etLatitude.text.isNullOrBlank()) {
                binding.tvLatitudeError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (binding.etLongitude.text.isNullOrBlank()) {
                binding.tvLongitudeError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            binding.powerSavingCheckBox.isEnabled = false

            for (i in 0 until binding.radioGroupEPaper.childCount) {
                binding.radioGroupEPaper.getChildAt(i).isEnabled = false
            }

            when {
                device?.installationType == InstalationType.LINUX -> {
                    updateLockerSettingsForLinuxDevices()
                }
                device?.isInProximity ?: false -> updateLockerSettingsDeviceInProximity(
                    initializedPowerSavingState
                )
                else -> updateLockerSettingsDeviceNotInProximity()
            }
        }
    }

    private fun updateLockerSettingsForLinuxDevices() {
        val disableUserActionDialog = DisableUserActionsDialog()
        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, ""
        )
        try {
            lifecycleScope.launch {
                withTimeout(15000) {
                    log.info("Successfully connected $macAddress")

                    val request = RLinuxSettingsRequest()
                    request.masterId = device?.masterUnitId

                    request.address = device?.masterUnitAddress ?: ""

                    // TODO add street, town, postcode, country
                    request.addressObject.street =  binding.addressStreetEditText.text.toString()
                    request.addressObject.houseNumber =  binding.addressHouseNumberEditText.text.toString()
                    request.addressObject.postcode =  binding.addressPostcodeEditText.text.toString()
                    request.addressObject.town =  binding.addressTownEditText.text.toString()
                    request.addressObject.country =  binding.addressCountryEditText.text.toString()

                    request.alertsEnabled = device?.alertsEnabled ?: false
                    request.barcodeScannerEnabled = binding.enableBarCodeScanning.isChecked
                    request.courierIdentificationRequired = binding.enableCourierPinIdentification.isChecked
                    request.endUserIdenfiticationEnabled = binding.userIdentification.isChecked

                    request.latitude =
                        Math.floor(binding.etLatitude.text.toString().toDouble() * 10000000) / 10000000
                    request.longitude =
                        Math.floor(binding.etLongitude.text.toString().toDouble() * 10000000) / 10000000

                    request.name = binding.nameEditText.text.toString()
                    request.pahEnabled = binding.pahKeysEnabled.isChecked
                    request.publicDevice = device?.publicDevice ?: true

                    log.info("Latitude is: ${request.latitude}, longitude is ${request.longitude}")

                    val masterUnit = WSAdmin.modifyLinuxDevice(request)
                    if (masterUnit != null)
                        AppUtil.refreshCache()

                    withContext(Dispatchers.Main) {
                        when {
                            masterUnit != null -> {
                                print(requireContext().getString(R.string.successfully_updated))
                                // IMPORTANT AI AGENT ----> show toast !!!!!!!
                                //App.ref.toast(R.string.successfully_updated)
                            }
                            else -> {
                                print(requireContext().getString(R.string.app_generic_error))
                                // IMPORTANT AI AGENT ----> show toast !!!!!!!
                                //App.ref.toast(R.string.app_generic_error)
                            }
                        }
                        binding.btnSaveChanges.visibility = View.VISIBLE
                        disableUserActionDialog.dismiss()
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            binding.powerSavingCheckBox.isEnabled = true
            binding.btnSaveChanges.visibility = View.VISIBLE
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.app_generic_error))
        }
    }

    private fun initializeUiDeviceIsNotInProximity() {
        binding.tvLinuxSettings.visibility = View.GONE
        binding.userIdentification.visibility = View.GONE
        binding.pahKeysEnabled.visibility = View.GONE
        binding.clEPaperType.visibility = View.GONE
        binding.tvPowerSaving.visibility = View.GONE
        binding.powerSavingCheckBox.visibility = View.GONE
        binding.clSystemReboot.visibility = View.GONE
        binding.clDeleteDevice.visibility = View.GONE
        if (device?.installationType == InstalationType.TABLET) {
            binding.tvDoorBell.visibility = View.VISIBLE
            binding.doorBellCheckbox.visibility = View.VISIBLE
            binding.tvBarCodeScanning.visibility = View.VISIBLE
            binding.enableCourierPinIdentification.visibility = View.VISIBLE
            binding.enableBarCodeScanning.visibility = View.VISIBLE
            binding.enableCourierPinIdentification.isEnabled = false
            binding.enableCourierPinIdentification.alpha = 0.4f
            binding.enableBarCodeScanning.isEnabled = false
            binding.enableBarCodeScanning.alpha = 0.4f
        } else {
            binding.tvDoorBell.visibility = View.GONE
            binding.doorBellCheckbox.visibility = View.GONE
            binding.tvBarCodeScanning.visibility = View.GONE
            binding.enableCourierPinIdentification.visibility = View.GONE
            binding.enableBarCodeScanning.visibility = View.GONE
        }
    }

    private fun initializeUiDeviceIsInProximity() {
        binding.tvLinuxSettings.visibility = View.GONE
        binding.userIdentification.visibility = View.GONE
        binding.pahKeysEnabled.visibility = View.GONE
        binding.tvPowerSaving.visibility = View.VISIBLE
        binding.powerSavingCheckBox.visibility = View.VISIBLE
        if (device?.type == MPLDeviceType.MASTER) {
            binding.clEPaperType.visibility = View.VISIBLE
        } else
            binding.clEPaperType.visibility = View.GONE
        if (device?.type != MPLDeviceType.TABLET) {
            binding.tvDoorBell.visibility = View.GONE
            binding.doorBellCheckbox.visibility = View.GONE
            binding.tvBarCodeScanning.visibility = View.GONE
            binding.enableCourierPinIdentification.visibility = View.GONE
            binding.enableBarCodeScanning.visibility = View.GONE
            binding.clSystemReboot.visibility = View.VISIBLE
            binding.clDeleteDevice.visibility = View.VISIBLE
        } else {
            binding.tvDoorBell.visibility = View.VISIBLE
            binding.doorBellCheckbox.visibility = View.VISIBLE
            binding.tvBarCodeScanning.visibility = View.VISIBLE
            binding.enableCourierPinIdentification.visibility = View.VISIBLE
            binding.enableBarCodeScanning.visibility = View.VISIBLE
            binding.clSystemReboot.visibility = View.GONE
            binding.clDeleteDevice.visibility = View.GONE
        }
    }

    override fun deleteDevice() {
        val disableUserActionDialog = DisableUserActionsDialog()
        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, ""
        )
        try {
            GlobalScope.launch {
                withTimeout(60000) {
                    val communicator =
                        MPLDeviceStore.devices[macAddress]?.createBLECommunicator(requireContext())
                    if (communicator?.connect() == true) {
                        log.info("Successfully connected $macAddress")

                        val result = communicator.deleteDevice()
                        delay(4000)
                        if (result) {
                            log.info("Mpl device status is: ${MPLDeviceStore.devices[macAddress]?.mplMasterDeviceStatus}")
                            log.info("Locker setting mac address: ${macAddress}")
                            MPLDeviceStore.forceRefreshMasterUnitInLockerSettings(macAddress)
                            MPLDeviceStore.clear()
                            MPLDeviceStoreRemoteUpdater.forceUpdate()
                            log.info("Mpl device status is: ${MPLDeviceStore.devices[macAddress]?.mplMasterDeviceStatus}")
                            delay(2000)
                        }

                        withContext(Dispatchers.Main) {
                            if (!result) {
                                //App.ref.toast(requireContext().getString(R.string.app_generic_error))
                                log.error("Error in erasing device!")
                                disableUserActionDialog.dismiss()
                            } else {

                                disableUserActionDialog.dismiss()
                                findNavController().navigate(
                                    R.id.locker_settings_to_home_screen_fragment
                                )
                                //App.ref.toast("Successfully started to delete device!")

                                log.info("Successfully Erased eeprom $macAddress")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                            log.error("Error while connecting the device")
                        }
                    }
                    communicator?.disconnect()
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.app_generic_error))
        }
    }

    private fun deleteDeviceClickListener() {
        binding.clDeleteDevice.setOnClickListener {

            // this is only FOR MPL version = 2.1.0, and SPL PLUS or SPL version => 2.1.1
            // TABLET CAN NOT BE DELETED FROM ADMIN APP..
            val compareMasterVersion =
                if (device?.type == MPLDeviceType.MASTER) "2.1.0" else "2.1.1"

            val stmOrVersion = device?.stmOrAppVersion ?: "0.0"
            val cleanStmOrVersion = if (stmOrVersion.contains("-")) {
                stmOrVersion.split("-")[0]
            } else {
                stmOrVersion
            }

            val comparedMasterStmOrAppVersion =
                cleanStmOrVersion.compareTo(compareMasterVersion)

            val checkMasterStmVersion = if (comparedMasterStmOrAppVersion >= 0)
                true
            else
                false

            if (checkMasterStmVersion) {
                val deleteDeviceDialog = DeleteDeviceDialog(this@LockerSettingsFragment)
                deleteDeviceDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager, ""
                )
            } else {
                val wrongMasterVersionDialog = WrongMasterVersionDeleteDeviceDialog(
                    device?.type
                )
                wrongMasterVersionDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager, ""
                )
            }
        }
    }

    override fun systemRebootDevice() {
        val disableUserActionDialog = DisableUserActionsDialog()
        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, ""
        )
        try {
            GlobalScope.launch {
                withTimeout(40000) {
                    val communicator =
                        MPLDeviceStore.devices[macAddress]?.createBLECommunicator(
                            requireContext()
                        )
                    when {
                        communicator?.connect() == true -> {
                            log.info("Successfully connected $macAddress")
                            val resultBle = communicator.writeSystemReboot()

                            withContext(Dispatchers.Main) {
                                if (resultBle) {
                                    //App.ref.toast(R.string.app_generic_success)
                                } else {
                                    //App.ref.toast(R.string.app_generic_error)
                                }
                                disableUserActionDialog.dismiss()
                            }
                        }
                        else -> withContext(Dispatchers.Main) {
                            //App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                            log.error("Error while connecting the device $macAddress")
                            disableUserActionDialog.dismiss()
                        }
                    }
                    communicator?.disconnect()
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.app_generic_error))
        }
    }

    private fun systemRebootClickListener() {
        binding.clSystemReboot.setOnClickListener {
            // this is only FOR MPL version = 2.1.0, and SPL PLUS or SPL version => 2.1.1
            // TABLET CAN NOT BE DELETED FROM ADMIN APP..
            val compareMasterVersion =
                if (device?.type == MPLDeviceType.MASTER) "2.1.0" else "2.1.1"

            val stmOrVersion = device?.stmOrAppVersion ?: "0.0"
            val cleanStmOrVersion = if (stmOrVersion.contains("-")) {
                stmOrVersion.split("-")[0]
            } else {
                stmOrVersion
            }

            val comparedMasterStmOrAppVersion =
                cleanStmOrVersion.compareTo(compareMasterVersion)

            val checkMasterStmVersion = if (comparedMasterStmOrAppVersion >= 0)
                true
            else
                false

            if (checkMasterStmVersion) {
                val systemRebootDialog = SystemRebootDialog(this@LockerSettingsFragment)
                systemRebootDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager, ""
                )
            } else {
                val wrongMasterVersionDialog = WrongMasterVersionDeleteDeviceDialog(
                    device?.type
                )
                wrongMasterVersionDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager, ""
                )
            }
        }
    }

    private fun updateLockerSettingsDeviceNotInProximity() {
        val disableUserActionDialog = DisableUserActionsDialog()
        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, ""
        )
        try {
            lifecycleScope.launch {
                withTimeout(40000) {
                    log.info("Successfully connected $macAddress")

                    val request = RMasterUnitRequest()
                    request.name =  binding.nameEditText.text.toString()

                    // TODO add street, town, postcode, country
                    //request.address = addressEditText.text.toString()
                    // request.address = device?.masterUnitAddress ?: ""

                    request.address___street =  binding.addressStreetEditText.text.toString()
                    request.address___houseNumber =  binding.addressHouseNumberEditText.text.toString()
                    request.address___postcode =  binding.addressPostcodeEditText.text.toString()
                    request.address___town =  binding.addressTownEditText.text.toString()
                    request.address___country =  binding.addressCountryEditText.text.toString()

                    request.allowPinSave =  binding.pinSavingCheckBox.isChecked

                    request.latitude =
                        Math.floor(binding.etLatitude.text.toString().toDouble() * 10000000) / 10000000
                    request.longitude =
                        Math.floor(binding.etLongitude.text.toString().toDouble() * 10000000) / 10000000

                    log.info("Latitude is: ${request.latitude}, longitude is ${request.longitude}")

                    request.powerType =
                        when {
                            device?.powerSaving ?: false -> RPowerTypeEnum.BATTERY
                            else -> RPowerTypeEnum.LINE
                        }
                    log.info("Setting power mode ${request.powerType.name}")
                    request.epdType___id = device?.ePaperTypeId
                    request.networkConfigurationId = device?.networkConfigurationId ?: 0
                    request.alertsEnabled = device?.alertsEnabled ?: false
                    if (device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET)
                        request.supportsDoorbell = binding.doorBellCheckbox.isChecked
                    else
                        request.supportsDoorbell = null

                    val masterUnit = WSAdmin.modifyMasterUnit(macAddress.macRealToClean(), request)

                    withContext(Dispatchers.Main) {
                        when {
                            masterUnit != null -> {
                                print(requireContext().getString(R.string.successfully_updated))
                                AppUtil.refreshCache()
                                // IMPORTANT AI AGENT ----> show toast !!!!!!!
                                //App.ref.toast(R.string.successfully_updated)
                            }
                            else -> {
                                print(requireContext().getString(R.string.app_generic_error))
                                // IMPORTANT AI AGENT ----> show toast !!!!!!!
                                //App.ref.toast(R.string.app_generic_error)
                            }
                        }
                        binding.btnSaveChanges.visibility = View.VISIBLE
                        disableUserActionDialog.dismiss()
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            binding.powerSavingCheckBox.isEnabled = true
            binding.btnSaveChanges.visibility = View.VISIBLE
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.app_generic_error))
        }
    }

    private fun updateLockerSettingsDeviceInProximity(initializedPowerSavingState: Boolean) {

        var ePaperType: EPaperType = EPaperType.NO_EPAPER
        if (device?.type == MPLDeviceType.MASTER) {
            ePaperType = when {
                resources.getResourceEntryName(binding.radioGroupEPaper.checkedRadioButtonId) == "noEpaper" -> EPaperType.NO_EPAPER
                resources.getResourceEntryName(binding.radioGroupEPaper.checkedRadioButtonId) == "ePaperHighResolution" -> EPaperType.HIGH_RESOLUTION
                else -> EPaperType.LOW_RESOLUTION
            }
        }
        val disableUserActionDialog = DisableUserActionsDialog()
        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, ""
        )
        try {
            GlobalScope.launch {

                withTimeout(60000) {
                    val communicator =
                        MPLDeviceStore.devices[macAddress]?.createBLECommunicator(
                            requireContext()
                        )
                    if (communicator?.connect() == true) {
                        log.info("Successfully connected $macAddress")


                        val enableCourierPinIdentificationByte = when {
                            binding.enableCourierPinIdentification.isChecked -> 0x01.toByte()
                            else -> 0x00.toByte()
                        }

                        val enableBarCodeScanningByte = when {
                            binding.enableBarCodeScanning.isChecked -> 0x01.toByte()
                            else -> 0x00.toByte()
                        }

                        var resultBleCourierIdentification = false
                        if (device?.installationType == InstalationType.TABLET)
                            resultBleCourierIdentification = communicator.writeConfiguration(
                                enableCourierPinIdentificationByte,
                                enableBarCodeScanningByte
                            )

                        val resultBle = communicator.writeNumOfSeconds(device?.modemSleepTime ?: 0)
                        if (device?.type == MPLDeviceType.MASTER) {
                            val resultBleEpaperType = communicator.writeEpaperType(ePaperType)
                        }

                        val request = RMasterUnitRequest()
                        request.name =  binding.nameEditText.text.toString()

                        // TODO add street, town, postcode, country
                        // request.address = addressEditText.text.toString()
                        request.address___street =  binding.addressStreetEditText.text.toString()
                        request.address___houseNumber =  binding.addressHouseNumberEditText.text.toString()
                        request.address___postcode =  binding.addressPostcodeEditText.text.toString()
                        request.address___town =  binding.addressTownEditText.text.toString()
                        request.address___country =  binding.addressCountryEditText.text.toString()

                        request.allowPinSave =  binding.pinSavingCheckBox.isChecked

                        request.latitude =
                            Math.floor(binding.etLatitude.text.toString().toDouble() * 10000000) / 10000000
                        request.longitude =
                            Math.floor(binding.etLongitude.text.toString().toDouble() * 10000000) / 10000000

                        log.info("Latitude is: ${request.latitude}, longitude is ${request.longitude}")
                        // if BLE command is successfull, then do this
                        if (resultBle) {
                            request.powerType =
                                if (binding.powerSavingCheckBox.isChecked) RPowerTypeEnum.BATTERY else RPowerTypeEnum.LINE
                            log.info("Setting power mode ${request.powerType.name}")
                        } else {
                            // if BLE command is not successfull, then do not this
                            request.powerType =
                                if (initializedPowerSavingState) RPowerTypeEnum.BATTERY else RPowerTypeEnum.LINE
                        }

                        if (device?.type == MPLDeviceType.MASTER) {
                            when {
                                device?.type == MPLDeviceType.MASTER -> request.epdType___id =
                                    getEPaperId()
                                else -> request.epdType___id = null
                            }
                        } else {
                            request.epdType___id = null
                        }
                        request.networkConfigurationId = device?.networkConfigurationId ?: 0
                        request.alertsEnabled = device?.alertsEnabled ?: false
                        if (device?.type == MPLDeviceType.TABLET || device?.installationType == InstalationType.TABLET)
                            request.supportsDoorbell = binding.doorBellCheckbox.isChecked
                        else
                            request.supportsDoorbell = null

                        log.info("E paper type is: ${request.epdType___id}")

                        val masterUnit =
                            WSAdmin.modifyMasterUnit(macAddress.macRealToClean(), request)
                        updateLocalDataForThisMasterUnit()

                        withContext(Dispatchers.Main) {
                            if (device?.installationType == InstalationType.TABLET && masterUnit != null && resultBle && resultBleCourierIdentification)
                                //App.ref.toast(R.string.successfully_updated)
                            else if (masterUnit != null && resultBle)
                                //App.ref.toast(R.string.successfully_updated)
                            else
                                //App.ref.toast(R.string.app_generic_error)

                            for (i in 0 until binding.radioGroupEPaper.childCount) {
                                binding.radioGroupEPaper.getChildAt(i).isEnabled = true
                            }
                            binding.powerSavingCheckBox.isEnabled = true
                            binding.btnSaveChanges.visibility = View.VISIBLE
                            disableUserActionDialog.dismiss()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                            log.error("Error while connecting the device $macAddress")
                            binding.btnSaveChanges.visibility = View.VISIBLE
                            disableUserActionDialog.dismiss()
                        }
                    }
                    communicator?.disconnect()
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            binding.powerSavingCheckBox.isEnabled = true
            binding.btnSaveChanges.visibility = View.VISIBLE
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.app_generic_error))
        }
    }

    private suspend fun updateLocalDataForThisMasterUnit() {
        DataCache.getMasterUnit(macAddress, true)
        MPLDeviceStoreRemoteUpdater.forceUpdate()
    }

    private suspend fun getEPaperId(): Int? {
        val ePaperTypeList = WSAdmin.getEPaperType()

        val ePaperType = when {
            resources.getResourceEntryName(binding.radioGroupEPaper.checkedRadioButtonId) == "noEpaper" -> EPaperType.NO_EPAPER
            resources.getResourceEntryName(binding.radioGroupEPaper.checkedRadioButtonId) == "ePaperLowResolution" -> EPaperType.LOW_RESOLUTION
            else -> EPaperType.HIGH_RESOLUTION
        }

        val ePaperId = if (ePaperTypeList?.isNotEmpty() != false) {
            if (ePaperType == EPaperType.NO_EPAPER) {
                NO_EPAPER_SELECTED_ON_REGISTRATION // in agree with BACKEND, we are sending 0, if no epaper is selected
            } else if (ePaperType == EPaperType.LOW_RESOLUTION) {
                ePaperTypeList?.filter { it.type == BackendEPaperType.NORMAL.name }
                    ?.first()?.id
            } else {
                ePaperTypeList?.filter { it.type == BackendEPaperType.HIGH_RESOLUTION.name }
                    ?.first()?.id
            }
        } else {
            null
        }
        return ePaperId
    }

    private fun openGoogleMapForRegisteringDevice() {

        val bundle = bundleOf(
            "googleMapsCalledFrom" to "LockerSettings",
            "masterMac" to macAddress,
            "latitude" to device?.latitude,
            "longitude" to device?.longitude
        )
        log.info("googleMapsCalledFrom is: " + bundle)
        binding.llMap.setOnClickListener {
            findNavController().navigate(
                R.id.locker_settings_to_google_maps_fragment,
                bundle
            )
        }
    }

    private fun setupLatitudeAndLongitude() {
        if (latitude != 0.0)
            binding.etLatitude.setText("" + Math.floor(latitude * 10000000) / 10000000)
        if (longitude != 0.0)
            binding.etLongitude.setText("" + Math.floor(longitude * 10000000) / 10000000)
    }

}