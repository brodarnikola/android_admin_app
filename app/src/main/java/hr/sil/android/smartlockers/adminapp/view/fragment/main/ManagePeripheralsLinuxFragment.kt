package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.LockerP16Status
import hr.sil.android.smartlockers.adminapp.data.RLockerLinuxDataUiModel
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentManagePeripheralsLinuxBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.PeripheralAdapterLinux
import hr.sil.android.smartlockers.adminapp.view.adapter.SearchOptionsLinuxAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.DisableUserActionsDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import hr.sil.android.util.general.extensions.hexToByteArray
import hr.sil.android.view_util.extensions.hideKeyboard
import kotlinx.coroutines.* 
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class ManagePeripheralsLinuxFragment : BaseFragment() {

    val log = logger()
    lateinit var macAddress: String

    private val MAC_ADDRESS_7_BYTES_P16 = 7
    private val MAC_ADDRESS_6_BYTES_NORMAL_SLAVE = 12
    private lateinit var peripheralAdapter: PeripheralAdapterLinux
    private lateinit var searchOptionsAdapter: SearchOptionsLinuxAdapter
    private var device: MPLDevice? = null

    private var updatingMaster: AtomicBoolean = AtomicBoolean(false)
    private var devices: MutableList<RLockerLinuxDataUiModel> = mutableListOf()

    private var filterSearchImage: Drawable? = null
    private var filterDeleteTextImage: Drawable? = null

    private var filterText: String = ""

    private lateinit var binding: FragmentManagePeripheralsLinuxBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        initializeToolbarUIMainActivity(
            true,
            getString(R.string.main_locker_manage_peripherals),
            false,
            false,
            requireContext()
        )
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        macAddress = arguments?.getString("masterMac", "") ?: ""
        log.info("Received mac address is: " + macAddress)

        device = MPLDeviceStore.devices[macAddress]

        binding = FragmentManagePeripheralsLinuxBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdaptersForLockerDataUi()
        setupAdaptersForSearchOptionUi()

        getLockerData()
    }

    private fun setupAdaptersForLockerDataUi() {
        binding.peripheralsRecyclerView.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )

        peripheralAdapter = PeripheralAdapterLinux(
            this@ManagePeripheralsLinuxFragment,
            mutableListOf(),
            macAddress,
            requireContext(),
            activity as MainActivity,
            device?.latitude,
            device?.longitude
        )
        binding.peripheralsRecyclerView.adapter = peripheralAdapter
    }

    private fun setupAdaptersForSearchOptionUi() {
        binding.rvSearchOptions.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        searchOptionsAdapter = SearchOptionsLinuxAdapter(
            mutableListOf()
        )
        binding.rvSearchOptions.adapter = searchOptionsAdapter
    }

    private fun getLockerData() {
        val disableUserActionDialog = DisableUserActionsDialog()

        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, ""
        )
        try {

            lifecycleScope.launch {

                withTimeout(60000) {

                    updatingMaster.set(true)

                    delay(500)

                    device = MPLDeviceStore.refreshMasterUnit(macAddress)

                    withContext(Dispatchers.Main) {

                        setupFilterText()
                        setupSearchOptions()

                        disableUserActionDialog.dismiss()

                        if( isAtLeastOneSearchParameterEnabled() ) {
                            binding.clSearchOptions.visibility = View.VISIBLE
                            updatedSearchOptionsAdapter()
                        }

                        updatingMaster.set(false)
                        displayPeripHeralsOnScreen()
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.app_generic_error))
        }
    }

    private fun setupSearchOptions() {
        binding.ivSearchOptions.setOnClickListener {
            // THAT WE WILL USE LATTER
            if (binding.llSearchOptions.isVisible) {
                binding.peripheralsRecyclerView.visibility = View.VISIBLE
                binding.clSearchOptions.visibility = if( isAtLeastOneSearchParameterEnabled() ) View.VISIBLE else View.GONE
                if( isAtLeastOneSearchParameterEnabled() ) updatedSearchOptionsAdapter()
                binding.llSearchOptions.visibility = View.GONE
            } else {
                binding.llSearchOptions.visibility = View.VISIBLE
                binding.clSearchOptions.visibility = View.GONE
                binding.peripheralsRecyclerView.visibility = View.GONE
            }
        }

        binding.ivClearSearch.setOnClickListener {
            if (binding.xsCheckBox.isChecked) binding.xsCheckBox.isChecked = false
            if (binding.sCheckBox.isChecked) binding.sCheckBox.isChecked = false
            if (binding.mCheckBox.isChecked) binding.mCheckBox.isChecked = false
            if (binding.lCheckBox.isChecked) binding.lCheckBox.isChecked = false
            if (binding.xlCheckBox.isChecked) binding.xlCheckBox.isChecked = false
            if (binding.emptyCheckBox.isChecked) binding.emptyCheckBox.isChecked = false
            if (binding.occupiedCheckBox.isChecked) binding.occupiedCheckBox.isChecked = false
            if (binding.needsCleaningCheckBox.isChecked) binding.needsCleaningCheckBox.isChecked = false
            if (binding.reducedMobilityCheckBox.isChecked) binding.reducedMobilityCheckBox.isChecked = false
            binding.peripheralsRecyclerView.visibility = View.VISIBLE
            binding.clSearchOptions.visibility = View.GONE
            binding.llSearchOptions.visibility = View.GONE
            searchOptionsAdapter.updateDevices(mutableListOf())
            displayPeripHeralsOnScreen()
        }

        binding.btnSaveChanges.setOnClickListener {

            // delete all parameters from adapter for search.. hide all items
            if (!isAtLeastOneSearchParameterEnabled()) {
                binding.llSearchOptions.visibility = View.GONE
                binding.clSearchOptions.visibility = View.GONE
                searchOptionsAdapter.updateDevices(mutableListOf())
                binding.peripheralsRecyclerView.visibility = View.VISIBLE
                displayPeripHeralsOnScreen()
            } else {
                binding.llSearchOptions.visibility = View.GONE
                binding.clSearchOptions.visibility = View.VISIBLE
                binding.peripheralsRecyclerView.visibility = View.VISIBLE
                updatedSearchOptionsAdapter()
                displayPeripHeralsOnScreen()
            }
        }
    }

    private fun updatedSearchOptionsAdapter() {
        val searchOptionsList = mutableListOf<String>()
        if (binding.xsCheckBox.isChecked) searchOptionsList.add("XS")
        if (binding.sCheckBox.isChecked) searchOptionsList.add("S")
        if (binding.mCheckBox.isChecked) searchOptionsList.add("M")
        if (binding.lCheckBox.isChecked) searchOptionsList.add("L")
        if (binding.xlCheckBox.isChecked) searchOptionsList.add("XL")
        if (binding.emptyCheckBox.isChecked) searchOptionsList.add(getString(R.string.locker_empty))
        if (binding.occupiedCheckBox.isChecked) searchOptionsList.add(getString(R.string.locker_occupied))
        if (binding.needsCleaningCheckBox.isChecked) searchOptionsList.add(getString(R.string.lockers_needs_cleaning))
        if (binding.reducedMobilityCheckBox.isChecked) searchOptionsList.add(getString(R.string.reduced_mobility_friendly))
        searchOptionsAdapter.updateDevices(searchOptionsList)
    }

    private fun isAtLeastOneSearchParameterEnabled() : Boolean {
        return binding.xsCheckBox.isChecked || binding.sCheckBox.isChecked || binding.mCheckBox.isChecked || binding.lCheckBox.isChecked || binding.xlCheckBox.isChecked
                || binding.emptyCheckBox.isChecked || binding.occupiedCheckBox.isChecked || binding.needsCleaningCheckBox.isChecked || binding.reducedMobilityCheckBox.isChecked
    }

    private fun setupFilterText() {
        filterSearchImage = getDrawableAttrValue(R.attr.thmHomeScreenEdittextSearchImage)
        filterDeleteTextImage = getDrawableAttrValue(R.attr.thmHomeScreenEdittextDeleteTextImage)
        binding.filterEdittext.afterTextChangeDelay(250) { text ->
            filterText = text.trim().toUpperCase(Locale.getDefault())
            displayPeripHeralsOnScreen()
        }
        binding.filterEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE)
        binding.ivRemoveFilterText.setOnClickListener {
            clearFilter()
            requireActivity().hideKeyboard()
        }
    }

    private fun clearFilter() {
        binding.filterEdittext.setText("")
    }

    //?attr/
    private fun getDrawableAttrValue(attr: Int): Drawable? {
        val attrArray = intArrayOf(attr)
        val typedArray = requireContext().obtainStyledAttributes(attrArray)
        val result = try {
            typedArray.getDrawable(0)
        } catch (exc: Exception) {
            null
        }
        typedArray.recycle()
        return result
    }

    private fun displayPeripHeralsOnScreen() {

        devices = combineSlaves().sortedBy { it.isLockerInProximity }.sortedBy { it.deviceType }
            .sortedBy { it.slaveIndex }
            .toMutableList()

        devices = applyFilter(devices)
        lifecycleScope.launch(Dispatchers.Main) {
            peripheralAdapter.updateDevices(devices)
        }
    }

    private fun applyFilter(devices: MutableList<RLockerLinuxDataUiModel>): MutableList<RLockerLinuxDataUiModel> {
        if (filterText.isEmpty())
            binding.ivRemoveFilterText.setImageDrawable(filterSearchImage)
        else
            binding.ivRemoveFilterText.setImageDrawable(filterDeleteTextImage)

        if (filterText.isEmpty() && !binding.xsCheckBox.isChecked && !binding.sCheckBox.isChecked && !binding.mCheckBox.isChecked
            && !binding.lCheckBox.isChecked && !binding.xlCheckBox.isChecked
            && !binding.emptyCheckBox.isChecked && !binding.occupiedCheckBox.isChecked
            && !binding.needsCleaningCheckBox.isChecked && !binding.reducedMobilityCheckBox.isChecked
        ) {
            return devices
        } else {

            val deviceList = mutableListOf<RLockerLinuxDataUiModel>()
            for( device in devices ) {
                var leastOneParameter = false
                if (filterText.isNotEmpty()) {
                    leastOneParameter = device.slaveMac.toLowerCase().contains(filterText.toLowerCase()) ||
                            device.slaveIndex.toLowerCase().contains(filterText.toLowerCase())
                    if( !leastOneParameter )
                        continue
                }

                if (binding.xsCheckBox.isChecked && device.size == RLockerSize.XS) {
                    leastOneParameter = true
                }
                else if( binding.xsCheckBox.isChecked && device.size != RLockerSize.XS ) {
                    continue
                }

                if (binding.sCheckBox.isChecked && device.size == RLockerSize.S) {
                    leastOneParameter = true
                }
                else if( binding.sCheckBox.isChecked && device.size != RLockerSize.S ) {
                    continue
                }

                if (binding.mCheckBox.isChecked && device.size == RLockerSize.M) {
                    leastOneParameter = true
                }
                else if( binding.mCheckBox.isChecked && device.size != RLockerSize.M ) {
                    continue
                }

                if (binding.lCheckBox.isChecked && device.size == RLockerSize.L) {
                    leastOneParameter = true
                }
                else if( binding.lCheckBox.isChecked && device.size != RLockerSize.L ) {
                    continue
                }

                if (binding.xlCheckBox.isChecked && device.size == RLockerSize.XL) {
                    leastOneParameter = true
                }
                else if( binding.xlCheckBox.isChecked && device.size != RLockerSize.XL ) {
                    continue
                }

                if (binding.emptyCheckBox.isChecked && device.lockerStatus == LockerP16Status.EMPTY) {
                    leastOneParameter = true
                }
                else if( binding.emptyCheckBox.isChecked && device.lockerStatus != LockerP16Status.EMPTY ) {
                    continue
                }

                if (binding.occupiedCheckBox.isChecked && device.lockerStatus == LockerP16Status.OCCUPIED) {
                    leastOneParameter = true
                }
                else if( binding.occupiedCheckBox.isChecked && device.lockerStatus != LockerP16Status.OCCUPIED ) {
                    continue
                }

                if (binding.needsCleaningCheckBox.isChecked && device.cleaningNeeded == RActionRequired.CLEANING) {
                    leastOneParameter = true
                }
                else if( binding.needsCleaningCheckBox.isChecked && device.cleaningNeeded != RActionRequired.CLEANING ) {
                    continue
                }

                if (binding.reducedMobilityCheckBox.isChecked && device.isReducedMobility) {
                    leastOneParameter = true
                }
                else if( binding.reducedMobilityCheckBox.isChecked && !device.isReducedMobility) {
                    continue
                }

                if( leastOneParameter )
                    deviceList.add(device)
            }

            return  deviceList

//            return devices.filter { device ->
//                var leastOneParameter = false
//                if (filterText.isNotEmpty()) {
//                    leastOneParameter = device.slaveMac.toLowerCase().contains(filterText.toLowerCase()) ||
//                            device.slaveIndex.toLowerCase().contains(filterText.toLowerCase())
//                }
//                if (xsCheckBox.isChecked && device.size == RLockerSize.XS) {
//                    leastOneParameter = true
//                }
//                if (sCheckBox.isChecked && device.size == RLockerSize.S) {
//                    leastOneParameter = true
//                }
//                if (mCheckBox.isChecked && device.size == RLockerSize.M) {
//                    leastOneParameter = true
//                }
//                if (lCheckBox.isChecked && device.size == RLockerSize.L) {
//                    leastOneParameter = true
//                }
//                if (xlCheckBox.isChecked && device.size == RLockerSize.XL) {
//                    leastOneParameter = true
//                }
//                if (emptyCheckBox.isChecked && device.lockerStatus == LockerP16Status.EMPTY) {
//                    leastOneParameter = true
//                }
//                if (occupiedCheckBox.isChecked && device.lockerStatus == LockerP16Status.OCCUPIED) {
//                    leastOneParameter = true
//                }
//                if (needsCleaningCheckBox.isChecked && device.cleaningNeeded == RActionRequired.CLEANING) {
//                    leastOneParameter = true
//                }
//                if (reducedMobilityCheckBox.isChecked && device.isReducedMobility) {
//                    leastOneParameter = true
//                }
//                leastOneParameter
//
//            }.toMutableList()
        }
    }

    private fun combineSlaves(): MutableList<RLockerLinuxDataUiModel> {

        log.info("Executing to get all registered slaves")
        val registeredSlaveUnitsList = getAllRegisteredSlavesFromBackend()
        log.info("Slave units from backend ${registeredSlaveUnitsList.size}, Stored actions keys : ${registeredSlaveUnitsList.joinToString { it.slaveMac + it.slaveIndex }}")

        return registeredSlaveUnitsList.toMutableList()
    }

    private fun getAllRegisteredSlavesFromBackend(): MutableList<RLockerLinuxDataUiModel> {

        log.info("Size of registered slave units from local variable are: ${device?.slaveUnits?.size}, Size of registered slave units from global variable are: ${MPLDeviceStore.devices[macAddress]?.slaveUnits?.size}")

        return MPLDeviceStore.devices[macAddress]?.slaveUnits?.filter {
            it.isDeleted == false &&
                    (it.lockerType == RLockerType.NORMAL || it.lockerType == RLockerType.P16 || it.lockerType == RLockerType.CLOUD)
        }
            ?.map {

                val correctMacAddress = when {
                    it.mac.hexToByteArray().size == MAC_ADDRESS_7_BYTES_P16 -> it.mac.take(
                        MAC_ADDRESS_6_BYTES_NORMAL_SLAVE
                    )
                    else -> it.mac
                }

                val inProximity =
                    MPLDeviceStore.devices[correctMacAddress.macCleanToReal()]?.isInProximity
                        ?: false
                log.info("Slave deregistered mac address: ${it.mac.macCleanToReal()}")
                val rLockerDataUiModel = setupModelForRecyclerviewRegisteredLocker(it, inProximity)
                log.debug("Registered " + it.mac.macCleanToReal())

                rLockerDataUiModel
            }?.toMutableList() ?: mutableListOf()
    }

    private fun setupModelForRecyclerviewRegisteredLocker(
        rLockerUnit: RLockerUnit,
        inProximity: Boolean
    ): RLockerLinuxDataUiModel {
        val rLockerDataUiModel = RLockerLinuxDataUiModel(
            0,
            "",
            "",
            "",
            RLockerSize.UNKNOWN,
            false,
            RLockerType.CLOUD,
            RLockerKeyPurpose.UNKNOWN,
            "",
            "",
            "",
            false,
            false,
            0,
            false,
            false,
            "",
            RActionRequired.NULL,
            mutableListOf(),
            LockerP16Status.UNREGISTERED,
            null,
            null
        )

        rLockerDataUiModel.id = rLockerUnit.id

        if (rLockerUnit.keys != null && rLockerUnit.keys.isNotEmpty()) {
            rLockerDataUiModel.lockerKeyId = rLockerUnit.keys[0].id ?: 0
            rLockerDataUiModel.keyPurpose = rLockerUnit.keys[0].purpose ?: RLockerKeyPurpose.DELIVERY
            rLockerDataUiModel.createdFor = rLockerUnit.keys[0].createdForGroupName ?: "-"
            rLockerDataUiModel.createdByName =
                if (rLockerUnit.keys[0].createdByName != null) rLockerUnit.keys[0].createdByName
                    ?: "" else "-"
            if (rLockerUnit.keys[0].timeCreated != null)
                rLockerDataUiModel.createdOnDate = rLockerUnit.keys[0].timeCreated
            //log.info("Created for name inside fragment: ${rLockerDataUiModel.createdByName}, createdForGroup___name ${rLockerUnit.keys[0].createdForGroupName},, " +
            //        "locker id is: ${rLockerDataUiModel.id}, locker mac is ${rLockerUnit.mac}, masterMac mac is ${rLockerUnit.masterMac}")
        } else {
            rLockerDataUiModel.keyPurpose = RLockerKeyPurpose.UNKNOWN
            rLockerDataUiModel.createdByName = ""
            rLockerDataUiModel.createdFor = ""
            rLockerDataUiModel.createdOnDate = "" //Date()
        }

        rLockerDataUiModel.deviceType = RLockerType.CLOUD
        rLockerDataUiModel.slaveIndex =
            rLockerUnit.mac.macRealToClean().hexToByteArray().last().toInt().toString()
        rLockerDataUiModel.slaveMac = rLockerUnit.mac.macCleanToReal()
        rLockerDataUiModel.masterMac = rLockerUnit.masterMac
        rLockerDataUiModel.size = rLockerUnit.size
        rLockerDataUiModel.isLockerInProximity = inProximity

        val keyStatusDB =
            DataCache.getKeyStatus().firstOrNull {
                log.info("Slave mac address in mobile phone databasse: ${it.slaveMacAddress}, slave mac from ble: ${rLockerUnit.mac}")
                it.slaveMacAddress.macRealToClean() == rLockerUnit.mac
            }
//            DataCache.getKeyStatus().firstOrNull {
//                log.info("Slave mac address in mobile phone databasse: ${it.slaveMacAddress}, slave mac from ble: ${slaveMacAddres}")
//                it.slaveMacAddress.macRealToClean() == slaveMacAddres
//            }
        if (keyStatusDB != null && keyStatusDB.slaveMacAddress != "")
            rLockerDataUiModel.deletingKeyInProgress = keyStatusDB.isKeyInStatusDeleting
        else
            rLockerDataUiModel.deletingKeyInProgress = false
        rLockerDataUiModel.requestToDeletingKeyOnBackend = rLockerUnit.invalidateKeys
        rLockerDataUiModel.isMasterUnitInProximity = device?.isInProximity ?: false
        rLockerDataUiModel.isReducedMobility = rLockerUnit.reducedMobility
        rLockerDataUiModel.cleaningNeeded =
            if (rLockerUnit.actionRequired != null) rLockerUnit.actionRequired else RActionRequired.NULL

        rLockerDataUiModel.lockerStatus =
            getLockerStatus(rLockerUnit.isAvailable, rLockerUnit.isDeleted)

        rLockerDataUiModel.timeDoorOpen = rLockerUnit.timeDoorOpen
        rLockerDataUiModel.timeDoorClosed = rLockerUnit.timeDoorClose

        log.info("time door open: ${rLockerDataUiModel.timeDoorOpen}, time door close: ${rLockerDataUiModel.timeDoorClosed}, locker mac: ${rLockerDataUiModel.slaveMac}, locker id: ${rLockerDataUiModel.id}")

        return rLockerDataUiModel
    }

    // locker status can only be empty or occupied
    private fun getLockerStatus(available: Boolean, deleted: Boolean): LockerP16Status {
        return when {
            available && !deleted -> LockerP16Status.EMPTY
            else -> LockerP16Status.OCCUPIED
        }
    }


}
