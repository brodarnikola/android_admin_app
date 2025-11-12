package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnitRequest
import hr.sil.android.smartlockers.adminapp.core.remote.model.RNetworkConfiguration
import hr.sil.android.smartlockers.adminapp.core.remote.model.RPowerTypeEnum
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.databinding.FragmentNetworkSettingsBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.NetworkConfigurationAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.DisableUserActionsDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.* 

class NetworkSettingsFragment : BaseFragment() {

    val log = logger()
    lateinit var macAddress: String
    private var device: MPLDevice? = null
    lateinit var selectedItem: RNetworkConfiguration

    private lateinit var binding: FragmentNetworkSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        val rootView = inflater.inflate(
//            R.layout.fragment_network_settings, container,
//            false
//        )

        binding = FragmentNetworkSettingsBinding.inflate(layoutInflater)

        initializeToolbarUIMainActivity(true, getString(R.string.main_locker_manage_network), false, false, requireContext())

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        macAddress = arguments?.getString("masterMac", "") ?: ""
        log.info("Received mac address is: " + macAddress)
        device = MPLDeviceStore.devices[macAddress]

        log.info("Network configuration id in NetworkSettingFragment is: ${device?.networkConfigurationId}")

        return binding.root // rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                }
            }


        binding.btnSaveChanges.setOnClickListener {
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
                            log.info("Successfully connected: ${selectedItem.name} , ${selectedItem.apnPass}, ${selectedItem.apnUrl}, ${selectedItem.apnUser}, ${selectedItem.modemRadioAccess} , ${selectedItem.customerName}, ${selectedItem.id}")

                            val result =
                                communicator.writeNetworkConfiguration(
                                    selectedItem,
                                    null,
                                    null,
                                    null
                                )
                            var resultBackend = false
                            if (result) {
                                resultBackend = updateMasterUnitOnBackend()
                                updateLocalDataForThisMasterUnit()
                            }

                            withContext(Dispatchers.Main) {
                                if (!result || !resultBackend) {
                                    disableUserActionDialog.dismiss()
                                    //App.ref.toast(R.string.registration_error)
                                    log.error("Error in registration!")
                                } else {
                                    disableUserActionDialog.dismiss()
                                    //App.ref.toast(R.string.successfull_saved_network_configuration)
                                }
                            }
                        } else {

                            withContext(Dispatchers.Main) {
                                disableUserActionDialog.dismiss()
                                log.error("Error while connecting!!")
                                //App.ref.toast(R.string.main_locker_ble_connection_error)
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
    }

    private suspend fun updateLocalDataForThisMasterUnit() {
        DataCache.getMasterUnit(macAddress, true)
        //force update device store
        MPLDeviceStoreRemoteUpdater.forceUpdate()
    }

    private suspend fun updateMasterUnitOnBackend() : Boolean {
        val request = RMasterUnitRequest()
        request.name = device?.masterUnitName ?: ""
        request.address = device?.masterUnitAddress ?: ""

        request.latitude = device?.latitude ?: 0.0
        request.longitude = device?.longitude ?: 0.0
        request.epdType___id = device?.ePaperTypeId
        request.powerType = if(device?.powerSaving == true) RPowerTypeEnum.BATTERY else RPowerTypeEnum.LINE
        request.allowPinSave = device?.allowPinSave ?: false
        request.networkConfigurationId = selectedItem.id
        request.alertsEnabled = device?.alertsEnabled ?: false

        log.info("backend request network configuration id is: ${request.networkConfigurationId}")

        log.info("Get Name:  ${request.name}")
        log.info("Get Address: ${request.address}")
        log.info("Get ePapertype id: ${request.epdType___id}")
        log.info("Get networkConfiguration id: ${request.networkConfigurationId}")

        return WSAdmin.modifyMasterUnit(macAddress.macRealToClean(), request) != null
    }

    override fun onStart() {
        super.onStart()
        GlobalScope.launch {
            val list = WSAdmin.getNetworkConfigurations() ?: listOf()
            withContext(Dispatchers.Main) {
                binding.spinerapnNetworkSelection.adapter = NetworkConfigurationAdapter(list)
                if (list.size > 0) {
                    val networkConfigurationId = list.indexOfFirst { it.id == device?.networkConfigurationId }
                    log.info("c: ${networkConfigurationId}")
                    if( networkConfigurationId != -1 )
                        binding.spinerapnNetworkSelection.setSelection(networkConfigurationId)
                    else
                        binding.spinerapnNetworkSelection.setSelection(0)
                }

            }
        }
    }

}