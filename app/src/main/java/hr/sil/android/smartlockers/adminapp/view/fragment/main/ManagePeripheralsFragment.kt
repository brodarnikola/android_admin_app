package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplSlave
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplSlaveP16
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusHandler
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusType
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.MPLAppDeviceStatus
import hr.sil.android.smartlockers.adminapp.data.RLockerDataUiModel
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentManagePeripheralsBinding
import hr.sil.android.smartlockers.adminapp.events.MPLDevicesUpdatedEvent
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.PeripheralAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.DisableUserActionsDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import hr.sil.android.util.general.extensions.hexToByteArray
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode 
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class ManagePeripheralsFragment : BaseFragment() {

    val log = logger()
    lateinit var macAddress: String

    private val MAC_ADDRESS_7_BYTES_P16 = 7
    private val MAC_ADDRESS_6_BYTES_NORMAL_SLAVE = 12
    private lateinit var peripheralAdapter: PeripheralAdapter
    private var device: MPLDevice? = null

    private var updatingMaster: AtomicBoolean = AtomicBoolean(false)
    private var devices: MutableList<RLockerDataUiModel> = mutableListOf()

    private lateinit var binding: FragmentManagePeripheralsBinding
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

        binding = FragmentManagePeripheralsBinding.inflate(layoutInflater)

        return binding.root // rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val disableUserActionDialog = DisableUserActionsDialog()

        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, "")
        try {

            lifecycleScope.launch() {

                withTimeout(60000) {

                    updatingMaster.set(true)

                    delay(500)

                    device = MPLDeviceStore.refreshMasterUnit(macAddress)
                    withContext(Dispatchers.Main) {

                        if (binding.peripheralsRecyclerView != null) {
                            disableUserActionDialog.dismiss()
                            binding.peripheralsRecyclerView.layoutManager =
                                LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )

                            peripheralAdapter = PeripheralAdapter(
                                mutableListOf(),
                                macAddress,
                                requireContext(),
                                activity as MainActivity,
                                device?.masterUnitType ?: RMasterUnitType.UNKNOWN,
                                device?.installationType  ?: InstalationType.UNKNOWN,
                                device?.stmOrAppVersion ?: ""
                            )
                            binding.peripheralsRecyclerView.adapter = peripheralAdapter

                            updatingMaster.set(false)
                            updateDeviceList()
                        }
                    }
                }

            }
        } catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.app_generic_error))
        }
    }

    private suspend fun updateDeviceList() {

        devices = combineSlaves().sortedBy { it.isLockerInProximity }.sortedBy { it.deviceType }.sortedBy { it.status }
            .toMutableList()
        lifecycleScope.launch(Dispatchers.Main) {
            peripheralAdapter.updateDevices(devices)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        if (!updatingMaster.get()) {
            lifecycleScope.launch {
                updateDeviceList()
            }
        }
    }

    private suspend fun combineSlaves(): MutableList<RLockerDataUiModel> {
        //Non registered slave units
        val actions = ActionStatusHandler.actionStatusDb.getAll().map { it.keyId }
        val unregisteredSlaves = MPLDeviceStore.getBleDataExceptMaster(actions).toMutableList()
        log.info("Size of unregistered slaves size: ${unregisteredSlaves}")

        log.info("Executing to get all registered slaves")
        var registeredSlaveUnits: Map<String, List<RLockerDataUiModel>>? = mapOf()
        registeredSlaveUnits = getAllRegisteredSlavesFromBackend(actions)
        log.info(
            "Slave units from backend ${registeredSlaveUnits?.size}, Stored actions keys :" + actions.joinToString(
                " - "
            ) { it })

        // get all registered slaves.. get only first element in map to show in recyclerview
        val registeredSlavedList =
            getOnlyFirstElementInMapFromRegisteredSlaves(registeredSlaveUnits)
        log.info("Registered slaved size for recyclerview, for drawing elements on view: ${registeredSlavedList.size}")

        if (checkUserRole()) {
            return (unregisteredSlaves + registeredSlavedList).toMutableList()
        } else
            return registeredSlavedList.toMutableList()
    }

    private fun checkUserRole(): Boolean {
        log.info("User role: ${UserUtil.user?.role?.name}")
        if (UserUtil.user?.role == RRoleEnum.SUPER_ADMIN || UserUtil.user?.role == RRoleEnum.ADMIN)
            return true
        return false
    }

    private suspend fun getOnlyFirstElementInMapFromRegisteredSlaves(registeredSlaveUnits: Map<String, List<RLockerDataUiModel>>?): List<RLockerDataUiModel> {
        val registeredSlavedList = mutableListOf<RLockerDataUiModel>()
        registeredSlaveUnits?.forEach { keys, values ->

            if (values.isNotEmpty())
                for (items in 0 until values.size) {
                    val registeredSlave = values[items]
                    registeredSlavedList.add(registeredSlave)
                    log.debug("Registered slave mac address ${registeredSlave.masterMac}")
                    break
                }
        }
        return registeredSlavedList
    }

    private suspend fun getAllRegisteredSlavesFromBackend(actions: List<String>): Map<String, List<RLockerDataUiModel>>? {
        // group all registered slave units by slave mac
        // because backend does not group by
        // all registered slave units, needs to have 6 byte mac address
        log.info("Size of registered slave units from local variable are: ${device?.slaveUnits?.size}, Size of registered slave units from global variable are: ${MPLDeviceStore.devices[macAddress]?.slaveUnits?.size}")
        return MPLDeviceStore.devices[macAddress]?.slaveUnits?.filter {
        //App.ref.lockerDetails.filter {
        //MPLDeviceStore.devices[macAddress]?.slaveUnits?.filter {
            //device?.slaveUnits?.filter { //lockers?.filter {
            it.isDeleted == false &&
                    (it.lockerType == RLockerType.NORMAL || it.lockerType == RLockerType.P16)
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
                if (ActionStatusHandler.actionStatusDb.get(it.mac.macCleanToReal() + ActionStatusType.PERIPHERAL_DEREGISTRATION) != null) {
                    log.info("Registered - DELETE_PENDING:" + it.mac.macCleanToReal())

                    setupModelForRecyclerviewDeletePendingLocker(it, inProximity)
                } else {
                    log.debug("Registered " + it.mac.macCleanToReal())
                    setupModelForRecyclerviewRegisteredLocker(it, inProximity, actions)
                }

            }?.groupBy { rLockerDataUiModel: RLockerDataUiModel ->
                // all registered slave units, needs to have 6 byte mac address
                rLockerDataUiModel.mac
            }?.toMap()
    }

    private fun setupModelForRecyclerviewRegisteredLocker(
        rLockerUnit: RLockerUnit,
        inProximity: Boolean,
        actions: List<String>
    ): RLockerDataUiModel {
        val rLockerDataUiModel: RLockerDataUiModel = RLockerDataUiModel(
            0,
            "",
            "",
            MPLAppDeviceStatus.UNREGISTERED,
            RLockerSize.UNKNOWN,
            false,
            RLockerType.NORMAL,
            RLockerKeyPurpose.UNKNOWN,
            "",
            "",
            false,
            false,
            0,
            false,
            false,
            "",
            RActionRequired.NULL
        )

        val deviceBleData = MPLDeviceStore.getAllRegisteredSlavesFromBackend().filter {
            it.deviceAddress.macRealToClean() == rLockerUnit.mac }.firstOrNull()

        val bleProps = deviceBleData?.data?.properties
        val doorStatusList = mutableListOf<Int>()
        when (bleProps) {
            is BLEAdvMplSlave -> {
                if( bleProps.doorStatus.value == 0 )
                    doorStatusList.add( 0 )
                else
                    doorStatusList.add( 1 )
                log.info("aaa bbb..door status ${bleProps.doorStatus.value}, mac address is: ${deviceBleData.deviceAddress}")
            }
            is BLEAdvMplSlaveP16 -> {
                for( doorStatus in bleProps.doorStatus.value ?: arrayOf() ) {
                    if( doorStatus == false )
                        doorStatusList.add( 0 )
                    else
                        doorStatusList.add( 1 )
                }
                log.info("aaa bbb..door status ${bleProps.doorStatus.value}, mac address is: ${deviceBleData.deviceAddress}")
            }
        }


        val key = rLockerUnit.mac.macCleanToReal() + ActionStatusType.PERIPHERAL_REGISTRATION
        if (actions.contains(key)) {
            ActionStatusHandler.actionStatusDb.del(key)
        }
        rLockerDataUiModel.id = rLockerUnit.id
        val slaveMacAddres = if (rLockerUnit.mac.hexToByteArray().size == MAC_ADDRESS_7_BYTES_P16) {

            rLockerDataUiModel.deviceType = RLockerType.P16
            //rLockerUnit.lockerType = RLockerType.P16
            rLockerUnit.mac.take(MAC_ADDRESS_6_BYTES_NORMAL_SLAVE)
        } else {
            if (rLockerUnit.keys != null)
                log.info("Registration Keys is: ${rLockerUnit.keys.joinToString { "-" }}")
            if (rLockerUnit.keys != null && rLockerUnit.keys.isNotEmpty()) {
                rLockerDataUiModel.lockerKeyId = rLockerUnit.keys[0].id
                rLockerDataUiModel.keyPurpose = rLockerUnit.keys[0].purpose
                rLockerDataUiModel.createdByName = if( rLockerUnit.keys[0].createdByName != null) rLockerUnit.keys[0].createdByName ?: "" else rLockerUnit.keys[0].createdForGroupName
                if( rLockerUnit.keys[0].timeCreated != null )
                    rLockerDataUiModel.createdOnDate = rLockerUnit.keys[0].timeCreated
                //log.info("Created for name inside fragment: ${rLockerDataUiModel.createdByName}, createdForGroup___name ${rLockerUnit.keys[0].createdForGroupName},, " +
                //        "locker id is: ${rLockerDataUiModel.id}, locker mac is ${rLockerUnit.mac}, masterMac mac is ${rLockerUnit.masterMac}")
            } else {
                rLockerDataUiModel.keyPurpose = RLockerKeyPurpose.UNKNOWN
                rLockerDataUiModel.createdByName = ""
                rLockerDataUiModel.createdOnDate = "" //Date()
            }

            rLockerDataUiModel.deviceType = RLockerType.NORMAL //rLockerUnit.lockerType
            rLockerUnit.mac
            //rLockerUnit.lockerType = RLockerType.NORMAL
            //rLockerUnit.mac
        }
        rLockerDataUiModel.mac = slaveMacAddres.macCleanToReal()
        rLockerDataUiModel.masterMac = rLockerUnit.masterMac
        rLockerDataUiModel.status = MPLAppDeviceStatus.REGISTERED
        rLockerDataUiModel.size = rLockerUnit.size
        rLockerDataUiModel.isLockerInProximity = inProximity
        val keyStatusDB =
            DataCache.getKeyStatus().firstOrNull {
                log.info("Slave mac address in mobile phone databasse: ${it.slaveMacAddress}, slave mac from ble: ${slaveMacAddres}")
                it.slaveMacAddress.macRealToClean() == slaveMacAddres
            }
        if (keyStatusDB != null && keyStatusDB.slaveMacAddress != "")
            rLockerDataUiModel.deletingKeyInProgress = keyStatusDB.isKeyInStatusDeleting
        else
            rLockerDataUiModel.deletingKeyInProgress = false
        rLockerDataUiModel.requestToDeletingKeyOnBackend = rLockerUnit.invalidateKeys
        rLockerDataUiModel.isMasterUnitInProximity = device?.isInProximity ?: false
        rLockerDataUiModel.isReducedMobility = rLockerUnit.reducedMobility
        rLockerDataUiModel.cleaningNeeded = if( rLockerUnit.actionRequired != null ) rLockerUnit.actionRequired else RActionRequired.NULL
        rLockerDataUiModel.doorStatus = doorStatusList

        log.info("Is master unit in proximity: ${rLockerDataUiModel.isMasterUnitInProximity}")

        return rLockerDataUiModel
    }

    private fun setupModelForRecyclerviewDeletePendingLocker(
        rLockerUnit: RLockerUnit,
        inProximity: Boolean
    ): RLockerDataUiModel {

        val rLockerDataUiModel: RLockerDataUiModel = RLockerDataUiModel(
            0,
            "",
            "",
            MPLAppDeviceStatus.UNREGISTERED,
            RLockerSize.UNKNOWN,
            false,
            RLockerType.NORMAL,
            RLockerKeyPurpose.UNKNOWN,
            "",
            "",
            false,
            false,
            0,
            false,
            false,
            "",
            RActionRequired.NULL
        )

        val slaveMacAddres = if (rLockerUnit.mac.hexToByteArray().size == MAC_ADDRESS_7_BYTES_P16) {
            rLockerDataUiModel.deviceType = RLockerType.P16
            rLockerUnit.mac.take(MAC_ADDRESS_6_BYTES_NORMAL_SLAVE)
        } else {
            if (rLockerUnit.keys != null)
                log.info("Delete pending Keys is: ${rLockerUnit.keys.joinToString { "-" }}")
            if (rLockerUnit.keys != null && rLockerUnit.keys.isNotEmpty()) {
                rLockerDataUiModel.lockerKeyId = rLockerUnit.keys[0].id
                rLockerDataUiModel.keyPurpose = rLockerUnit.keys[0].purpose
                rLockerDataUiModel.createdByName = if( rLockerUnit.keys[0].createdByName != null) rLockerUnit.keys[0].createdByName ?: "" else rLockerUnit.keys[0].createdForGroupName
                if( rLockerUnit.keys[0].timeCreated != null )
                    rLockerDataUiModel.createdOnDate = rLockerUnit.keys[0].timeCreated
            } else {
                rLockerDataUiModel.keyPurpose = RLockerKeyPurpose.UNKNOWN
                rLockerDataUiModel.createdByName = ""
                rLockerDataUiModel.createdOnDate = ""
            }
            rLockerDataUiModel.deviceType = RLockerType.NORMAL
            rLockerUnit.mac
        }

        val deviceBleData = MPLDeviceStore.getAllRegisteredSlavesFromBackend().filter {
            it.deviceAddress.macRealToClean() == rLockerUnit.mac }.firstOrNull()

        val bleProps = deviceBleData?.data?.properties
        val doorStatusList = mutableListOf<Int>()
        when (bleProps) {
            is BLEAdvMplSlave -> {
                if( bleProps.doorStatus.value == 0 )
                    doorStatusList.add( 0 )
                else
                    doorStatusList.add( 1 )
                log.info("aaa bbb..door status ${bleProps.doorStatus.value}, mac address is: ${deviceBleData.deviceAddress}")
            }
            is BLEAdvMplSlaveP16 -> {
                for( doorStatus in bleProps.doorStatus.value ?: arrayOf() ) {
                    if( doorStatus == false )
                        doorStatusList.add( 0 )
                    else
                        doorStatusList.add( 1 )
                }
                log.info("aaa bbb..door status ${bleProps.doorStatus.value}, mac address is: ${deviceBleData.deviceAddress}")
            }
        }

        rLockerDataUiModel.mac = slaveMacAddres.macCleanToReal()
        rLockerDataUiModel.masterMac = rLockerUnit.masterMac
        rLockerDataUiModel.status = MPLAppDeviceStatus.DELETE_PENDING
        rLockerDataUiModel.size = rLockerUnit.size
        rLockerDataUiModel.isLockerInProximity = inProximity
        val keyStatusDB =
            DataCache.getKeyStatus().firstOrNull {
                log.info("Slave mac address in mobile phone databasse: ${it.slaveMacAddress.macRealToClean()}, slave mac from ble: ${slaveMacAddres}")
                it.slaveMacAddress.macRealToClean() == slaveMacAddres
            }
        if (keyStatusDB != null && keyStatusDB.slaveMacAddress != "")
            rLockerDataUiModel.deletingKeyInProgress = keyStatusDB.isKeyInStatusDeleting
        else
            rLockerDataUiModel.deletingKeyInProgress = false
        rLockerDataUiModel.requestToDeletingKeyOnBackend = rLockerUnit.invalidateKeys
        rLockerDataUiModel.isMasterUnitInProximity = device?.isInProximity ?: false
        rLockerDataUiModel.isReducedMobility = rLockerUnit.reducedMobility
        rLockerDataUiModel.cleaningNeeded = if( rLockerUnit.actionRequired != null ) rLockerUnit.actionRequired else RActionRequired.NULL
        rLockerDataUiModel.doorStatus = doorStatusList

        log.info("Is master unit in proximity: ${rLockerDataUiModel.isMasterUnitInProximity}")

        return rLockerDataUiModel
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        if (App.ref.eventBus.isRegistered(this))
            App.ref.eventBus.unregister(this)
        updatingMaster.set(true)
    }

}
