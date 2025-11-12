package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplSlaveP16
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToBytes
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.LockerP16Status
import hr.sil.android.smartlockers.adminapp.data.P16DisableSaveButtonInterface
import hr.sil.android.smartlockers.adminapp.data.P16PeripheralsModel
import hr.sil.android.smartlockers.adminapp.databinding.FragmentPeripheralsP16Binding
import hr.sil.android.smartlockers.adminapp.events.MPLDevicesUpdatedEvent
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.PeripheralAdapterP16
import hr.sil.android.smartlockers.adminapp.view.dialog.DeactivateP16
import hr.sil.android.smartlockers.adminapp.view.dialog.ExitWithoutSaveChangesDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.P16OccupiedLockersDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.WrongLockerVersionDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import hr.sil.android.util.general.extensions.hexToByteArray
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode 
import java.util.*

class PeripheralsP16Fragment : BaseFragment(), P16DisableSaveButtonInterface {

    val log = logger()
    private var slaveMac: String = ""
    private var masterMac: String = ""
    private var stmOrAppVersion: String = ""
    private var lockerVersion: String = ""
    private var isRegistered: Boolean = false

    var lockerP16List: MutableList<P16PeripheralsModel> = mutableListOf()
    var copyLockerP16List: MutableList<P16PeripheralsModel> = mutableListOf()
    private val P16_LIST_SIZE = 16
    private val P16_FIRST_INDEX_IS_ONE = 1
    private val P16_LOCKERS_MAC_ADDRESS_SIZE = 14
    private val FIRST_12_CHARACTERS_FROM_SLAVE_MAC_ADDRESS =
        12 // 12 characters or 6 bytes for mac address
    private val LAST_REGISTERED_INDEX_PLUS_ONE = 1

    private var device: MPLDevice? = null

    private var adapter = PeripheralAdapterP16(
        mutableListOf(),
        this@PeripheralsP16Fragment,
        null,
        mutableListOf(),
        masterMac,
        slaveMac,
        device?.installationType
    )

    var lockerP16Data = listOf<RLockerUnit>()

    private lateinit var binding: FragmentPeripheralsP16Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPeripheralsP16Binding.inflate(layoutInflater)

        initializeToolbarUIMainActivity(
            true,
            getString(R.string.main_locker_manage_peripherals),
            false,
            false,
            requireContext()
        )
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        masterMac = arguments?.getString("masterMac", "") ?: ""
        slaveMac = arguments?.getString("slaveMac", "") ?: ""
        stmOrAppVersion = arguments?.getString("stmOrAppVersion", "") ?: ""
        lockerVersion = arguments?.getString("lockerVersion", "") ?: ""
        isRegistered = arguments?.getBoolean("isRegistered", false) ?: false

        log.info("Received master mac address is: " + masterMac + " received slave mac is: " + slaveMac + " locker version is: " + lockerVersion)

        device = MPLDeviceStore.devices[masterMac]

        return binding.root // rootView
    }

    private fun checkIfVersionOfLockerAndMasterAreCompatible(): Boolean {
        if (!isRegistered) {

            val compareMasterVersion =
                if (device?.masterUnitType == RMasterUnitType.MPL && device?.installationType == InstalationType.DEVICE) "3.0.0" else "3.1.0"
            val compareLockerVersion =
                if (device?.masterUnitType == RMasterUnitType.MPL && device?.installationType == InstalationType.DEVICE) "2.0.0" else "2.0.0"
            val stmVersionControl =
                stmOrAppVersion.compareTo(compareMasterVersion) ?: -1
            val cleanLockerVersion =
                if (lockerVersion.contains("-")) {
                    lockerVersion.split("-")[0]
                } else {
                    lockerVersion
                }
            val comparedLockerVersion =
                cleanLockerVersion.compareTo(compareLockerVersion)
            if (stmVersionControl >= 0 && comparedLockerVersion >= 0) {
                return true
            } else if (stmVersionControl < 0 && comparedLockerVersion < 0) {
                return true
            } else {
                val wrongLockerVersionDialog = WrongLockerVersionDialog(
                    cleanLockerVersion,
                    stmOrAppVersion
                )
                wrongLockerVersionDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager, ""
                )
                return false
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        // This callback will only be called when PeripheralsP16Fragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    if (listsEqual(lockerP16List, copyLockerP16List)) {
                        val bundle = bundleOf("masterMac" to masterMac)
                        log.info("Sended masterMacAddress to manage userAccessList is: " + bundle + " to String: " + bundle.toString())
                        findNavController().navigate(
                            R.id.p16_lockers_fragment_to_manage_peripherals_fragment,
                            bundle
                        )
                    } else {
                        val exitWithoutSaveChangesDialog =
                            ExitWithoutSaveChangesDialog(masterMac)
                        exitWithoutSaveChangesDialog.show(
                            (context as MainActivity).supportFragmentManager,
                            ""
                        )
                        //////App.ref.toast("Razlicite su liste")
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.rvP16Peripherals.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        lockerP16Data = device?.slaveUnits?.filter {
            it.mac.take(FIRST_12_CHARACTERS_FROM_SLAVE_MAC_ADDRESS) == slaveMac.macRealToClean()
                    && it.lockerType == RLockerType.P16 && !it.isDeleted
        }?.sortedBy { it.mac } ?: listOf()

        val lockerTypeList =
            lockerP16Data.filter { it.lockerType == RLockerType.P16 && it.mac.length == P16_LOCKERS_MAC_ADDRESS_SIZE && !it.isDeleted }
                ?: listOf()
        if (lockerTypeList.isEmpty())
            binding.btnDeaktivateP16.visibility = View.GONE
        else if (lockerTypeList.isNotEmpty() && checkUserRole()) {
            binding.btnDeaktivateP16.isEnabled = true
            binding.btnDeaktivateP16.alpha = 1.0f
            binding.btnDeaktivateP16.visibility = View.VISIBLE
        } else if (lockerTypeList.isNotEmpty() && !checkUserRole()) {
            binding.btnDeaktivateP16.visibility = View.VISIBLE
            binding.btnDeaktivateP16.isEnabled = false
            binding.btnDeaktivateP16.alpha = 0.4f
        }

        binding.btnDeaktivateP16.setOnClickListener {
            if (checkUserRole()) {
                if (checkIfAllLockersAreEmpty()) {
                    if (checkIfVersionOfLockerAndMasterAreCompatible()) {
                        binding.btnDeaktivateP16.isEnabled = true
                        binding.btnDeaktivateP16.alpha = 1.0f
                        val dialog = DeactivateP16(
                            masterMac,
                            slaveMac,
                            this@PeripheralsP16Fragment
                        )
                        dialog.show((requireContext() as MainActivity).supportFragmentManager, "")
                    }
                } else {
                    val dialog = P16OccupiedLockersDialog()
                    dialog.show(
                        (activity as MainActivity).supportFragmentManager,
                        ""
                    )
                }
            } else {
                binding.btnDeaktivateP16.isEnabled = false
                binding.btnDeaktivateP16.alpha = 0.4f
            }
        }

        lockerP16List = mutableListOf()

        lockerP16List = insertDataIntoLockerP16List(lockerP16Data, lockerP16List)
        initializeLockerP16Adapter()
        adapter.updateDevices(lockerP16List)

        log.info("P16 data is: ${lockerP16Data}")


        saveChangesSetOnClickListener()
    }

    private fun checkIfAllLockersAreEmpty(): Boolean {
        for (periphearlP16Model in lockerP16List) {

            if (!periphearlP16Model.isAvailable && !periphearlP16Model.isDeleted) {
                return false
            }
        }
        return true
    }

    fun listsEqual(list1: List<P16PeripheralsModel>, list2: List<P16PeripheralsModel>): Boolean {

        if (list1.size != list2.size)
            return false

        val pairList = list1.zip(list2)

        return pairList.all { (elt1, elt2) ->
            elt1.lockerSizeIndex == elt2.lockerSizeIndex &&
                    elt1.lockerSizeByte == elt2.lockerSizeByte &&
                    elt1.lockerStatus == elt2.lockerStatus &&
                    elt1.cleaningNeeded == elt2.cleaningNeeded
        }
    }

    private fun checkUserRole(): Boolean {
        log.info("User role: ${UserUtil.user?.role?.name}")
        if (UserUtil.user?.role == RRoleEnum.SUPER_ADMIN || UserUtil.user?.role == RRoleEnum.ADMIN)
            return true
        return false
    }

    private fun saveChangesSetOnClickListener() {

        if (checkUserRole()) {
            binding.btnSaveChanges.isEnabled = true
            binding.btnSaveChanges.alpha = 1.0f
            binding.btnSaveChanges.setOnClickListener {
                if (checkIfVersionOfLockerAndMasterAreCompatible()) {

                    binding.progressBarButtonClick.visibility = View.VISIBLE
                    binding.btnSaveChanges.visibility = View.GONE
                    binding.btnDeaktivateP16.visibility = View.GONE

                    var sendLockerSizeList = byteArrayOf()

                    for (index in 0 until lockerP16List.size) {

                        if (lockerP16List[index].lockerStatus == LockerP16Status.UNREGISTERED || lockerP16List[index].lockerStatus == LockerP16Status.NOTHING
                        ) {
                            lockerP16List[index].lockerSizeByte = RLockerSize.NOTHING
                        }
                        sendLockerSizeList = sendLockerSizeList.plus(
                            lockerP16List[index].lockerSizeByte.code
                                ?: 0x00
                        )
                        if( device?.installationType == InstalationType.TABLET ) {
                            if (lockerP16List[index].reducedMobility && lockerP16List[index].cleaningNeeded == RActionRequired.CLEANING ) {
                                sendLockerSizeList = sendLockerSizeList.plus(
                                    RLockerReducedMobility.WITH_MOBILITY_AND_WITH_CLEANING_NEEDED.code ?: 0x00
                                )
                            }
                            else if ( lockerP16List[index].reducedMobility && lockerP16List[index].cleaningNeeded != RActionRequired.CLEANING ) {
                                sendLockerSizeList = sendLockerSizeList.plus(
                                    RLockerReducedMobility.ONLY_WITH_MOBILITY.code ?: 0x00
                                )
                            }
                            else if ( !lockerP16List[index].reducedMobility && lockerP16List[index].cleaningNeeded == RActionRequired.CLEANING ) {
                                sendLockerSizeList = sendLockerSizeList.plus(
                                    RLockerReducedMobility.ONLY_WITH_CLEANING_NEEDED.code ?: 0x00
                                )
                            }
                            else {
                                sendLockerSizeList = sendLockerSizeList.plus(
                                    RLockerReducedMobility.WITHOUT_MOBILITY_AND_WITHOUT_CLEANING_NEEDED.code ?: 0x00
                                )
                            }
                        }

                        log.info("${index}) size: ${lockerP16List[index].lockerSizeByte.name}, is cleaning needed: ${lockerP16List[index].cleaningNeeded}, reduced mobility is: ${lockerP16List[index].reducedMobility}")
                    }

                    log.info("Byte array data: ${sendLockerSizeList.joinToString { ("\n") + it } }")

                    GlobalScope.launch {

                        val communicator =
                            MPLDeviceStore.devices[masterMac]?.createBLECommunicator(this@PeripheralsP16Fragment.requireContext())
                        if (communicator != null && communicator.connect()) {

                            log.info("Connection is done ${slaveMac}, mac real to clean: ${slaveMac.macRealToClean()}, mac real to clean to bytes: ${slaveMac.macRealToBytes()}")

                            val result = if( device?.installationType == InstalationType.TABLET ) {
                                communicator.registerSlaveP16LockerForCPL(
                                    slaveMac.macRealToClean(),
                                    sendLockerSizeList
                                )
                            } else {
                                communicator.registerSlaveP16Locker(
                                    slaveMac.macRealToClean(),
                                    sendLockerSizeList
                                )
                            }

                            withContext(Dispatchers.Main) {

                                when {
                                    result -> {
                                        log.info("Data(byte array) for P16 successfully started to update")
                                        ////App.ref.toast(this@PeripheralsP16Fragment.getString(R.string.update_locker_data_started))
                                        //PeripheralAdapterP16.setLastIndexChanged(0)
                                        copyLockerP16List.clear()
                                        copyLockerP16List.addAll(lockerP16List.map { it.copy() })
                                    }
                                    else -> {
                                        log.info("Data(byte array) for P16 did not successfully started to update")
                                        ////App.ref.toast(this@PeripheralsP16Fragment.getString(R.string.app_generic_error))
                                    }
                                }
                                binding.progressBarButtonClick.visibility = View.GONE
                                binding.btnSaveChanges.visibility = View.VISIBLE
                                binding.btnDeaktivateP16.visibility = View.VISIBLE
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                ////App.ref.toast(this@PeripheralsP16Fragment.getString(R.string.main_locker_ble_connection_error))
                                log.error("Error while connecting the peripheral ${slaveMac}")

                                binding.progressBarButtonClick.visibility = View.GONE
                                binding.btnSaveChanges.visibility = View.VISIBLE
                                binding.btnDeaktivateP16.visibility = View.VISIBLE
                            }
                        }
                        communicator?.disconnect()
                    }
                }
            }
        } else {
            binding.btnSaveChanges.isEnabled = false
            binding.btnSaveChanges.alpha = 0.5f
        }
    }

    private fun getLockerStatus(available: Boolean, deleted: Boolean): LockerP16Status {

        return when {
            available && !deleted -> LockerP16Status.EMPTY
            !available && deleted -> LockerP16Status.UNREGISTERED
            else -> LockerP16Status.OCCUPIED
        }
    }

    private fun getLockerSizeByte(
        lockerSize: RLockerSize,
        available: Boolean,
        deleted: Boolean
    ): RLockerSize {

        if (!available && deleted)
            return RLockerSize.NOTHING
        else if (lockerSize == RLockerSize.XS)
            return RLockerSize.XS
        else if (lockerSize == RLockerSize.S)
            return RLockerSize.S
        else if (lockerSize == RLockerSize.M)
            return RLockerSize.M
        else if (lockerSize == RLockerSize.L)
            return RLockerSize.L
        else
            return RLockerSize.XL
    }

    private fun getLockerSizeIndex(
        lockerSize: RLockerSize,
        available: Boolean,
        deleted: Boolean
    ): Int {

        return when {
            !available && deleted -> 0
            lockerSize == RLockerSize.NOTHING -> 0
            lockerSize == RLockerSize.XS -> 1
            lockerSize == RLockerSize.S -> 2
            lockerSize == RLockerSize.M -> 3
            lockerSize == RLockerSize.L -> 4
            else -> 5
        }
    }

    private fun initializeLockerP16Adapter() {

        binding.rvP16Peripherals.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE

        val lockerSizeList = mutableListOf<String>()
        lockerSizeList.add("?")
        lockerSizeList.add("XS")
        lockerSizeList.add("S")
        lockerSizeList.add("M")
        lockerSizeList.add("L")
        lockerSizeList.add("XL")

        val cloneLockersData = lockerP16List.map { it.copy() }
        copyLockerP16List.addAll(cloneLockersData)

         adapter = PeripheralAdapterP16(
            lockerP16List,
            this@PeripheralsP16Fragment,
            activity as MainActivity,
            lockerSizeList,
            masterMac,
            slaveMac,
            device?.installationType
        )

        val manager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        binding.rvP16Peripherals.layoutManager = manager
        binding.rvP16Peripherals.adapter = adapter
    }

    override fun disableItem(disableSaveButton: Boolean, lockerP16Status: LockerP16Status) {

        /* btnSaveChanges.isEnabled = lockerP16Status != LockerP16Status.NEW
         when (lockerP16Status) {
             LockerP16Status.NEW -> btnSaveChanges.alpha = 0.4f
             else -> btnSaveChanges.alpha = 1.0f
         }*/
    }

    var currentTime = System.currentTimeMillis()
    val minTimePassed = 3000L
    var enteredOnScreeen = true
    val doorStatusList = mutableListOf<Int>()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {

         if( System.currentTimeMillis() > currentTime + minTimePassed || enteredOnScreeen == true ) {
             doorStatusList.clear()
             val deviceBleData = MPLDeviceStore.getRegisteredP16FromBluebtooth(slaveMac)
             val bleProps = deviceBleData?.data?.properties
             when (bleProps) {
                 is BLEAdvMplSlaveP16 -> {
                     for ((index, doorStatus) in bleProps.doorStatus.value?.withIndex()
                         ?: arrayListOf()) {
                         if (doorStatus == false)
                             doorStatusList.add(0)
                         else
                             doorStatusList.add(1)
                         log.info("aaa bbb..door status ${doorStatus}, mac address is: ${deviceBleData.deviceAddress}, index inside foor loop: ${index}")
                     }
                 }
             }
             enteredOnScreeen = false
             currentTime = System.currentTimeMillis()

             var localP16List = mutableListOf<P16PeripheralsModel>()
             localP16List = insertDataIntoLockerP16List(lockerP16Data, localP16List)

             adapter.updateDevices(localP16List)
         }
    }

    private fun insertDataIntoLockerP16List(lockerP16Data: List<RLockerUnit>?, lockerP16List: MutableList<P16PeripheralsModel>) : MutableList<P16PeripheralsModel> {

        val lockerTypeList =
            lockerP16Data?.filter { it.lockerType == RLockerType.P16 && it.mac.length == P16_LOCKERS_MAC_ADDRESS_SIZE }
                ?: listOf()
        if (lockerTypeList.isNotEmpty()) {

            val lastRegisteredIndex =
                lockerTypeList.last().mac.macRealToClean().hexToByteArray().last().toInt()
            log.info("last registered index is: " + lastRegisteredIndex)

            for (index in P16_FIRST_INDEX_IS_ONE..P16_LIST_SIZE) {

                var isLockerRegistered = false
                if (index < (lastRegisteredIndex + LAST_REGISTERED_INDEX_PLUS_ONE)) {
                    for (indexRegistered in 0 until lockerTypeList.size) {

                        val rLockerUnit = lockerTypeList[indexRegistered]
                        if (index == rLockerUnit.mac.macRealToClean().hexToByteArray().last()
                                .toInt()
                        ) {
                            val p16Model = P16PeripheralsModel(
                                0,
                                0,
                                RLockerSize.NOTHING,
                                LockerP16Status.NOTHING,
                                false,
                                "",
                                true,
                                false,
                                RLockerKeyPurpose.UNKNOWN, "", "",
                                true, RActionRequired.NULL, false, false,
                                0, false, false, 0
                            )
                            p16Model.lockerIndex =
                                rLockerUnit.mac.macRealToClean().hexToByteArray().last().toInt()
                            p16Model.lockerSizeIndex = getLockerSizeIndex(
                                rLockerUnit.size,
                                rLockerUnit.isAvailable,
                                rLockerUnit.isDeleted
                            )
                            p16Model.lockerSizeByte = getLockerSizeByte(
                                rLockerUnit.size,
                                rLockerUnit.isAvailable,
                                rLockerUnit.isDeleted
                            )
                            p16Model.lockerStatus =
                                getLockerStatus(rLockerUnit.isAvailable, rLockerUnit.isDeleted)
                            p16Model.isSelected = false
                            p16Model.slaveIndexP16Mac = rLockerUnit.mac
                            p16Model.isAvailable = rLockerUnit.isAvailable
                            p16Model.isDeleted = rLockerUnit.isDeleted
                            p16Model.cleaningNeeded = if( rLockerUnit.actionRequired != null ) rLockerUnit.actionRequired else RActionRequired.NULL
                            p16Model.reducedMobility = rLockerUnit.reducedMobility

                            if (rLockerUnit.keys != null && rLockerUnit.keys.isNotEmpty()) {
                                p16Model.lockerKeyId = rLockerUnit.keys[0].id
                                p16Model.keyPurpose = rLockerUnit.keys[0].purpose
                                p16Model.createdByName = if( rLockerUnit.keys[0].createdByName != null) rLockerUnit.keys[0].createdByName ?: "" else rLockerUnit.keys[0].createdForGroupName
                                p16Model.createdOnDate = rLockerUnit.keys[0].timeCreated
                                log.info("Created for name inside fragment: ${p16Model.createdByName}, key purpose is: ${p16Model.keyPurpose},, " +
                                        "key id is: ${p16Model.lockerKeyId}, created on date: ${p16Model.createdByName}")
                            } else {
                                p16Model.keyPurpose = RLockerKeyPurpose.UNKNOWN
                                p16Model.createdByName = ""
                                p16Model.createdOnDate = ""
                            }

                            val inProximity =
                                MPLDeviceStore.devices[slaveMac]?.isInProximity
                                    ?: false
                            p16Model.isP16DeviceInProximity = inProximity
                            p16Model.isMasterDeviceInProximity =
                                MPLDeviceStore.devices[masterMac]?.isInProximity ?: false

                            p16Model.doorStatus = if( doorStatusList.size >= index ) doorStatusList[index - P16_FIRST_INDEX_IS_ONE] else 0

                            log.info("Door status is: ${p16Model.doorStatus}, locker index is: ${p16Model.lockerIndex}")

                            lockerP16List.add(p16Model)
                            isLockerRegistered = true
                            break
                        }
                    }
                    if (!isLockerRegistered) {
                        val p16Model = P16PeripheralsModel(
                            index,
                            0,
                            RLockerSize.NOTHING,
                            LockerP16Status.UNREGISTERED,
                            false,
                            "",
                            true,
                            false,
                            RLockerKeyPurpose.UNKNOWN, "", "",
                            false, RActionRequired.NULL, false, false,
                            0, false, false, 0
                        )
                        p16Model.doorStatus = if( doorStatusList.size >= index ) doorStatusList[index - P16_FIRST_INDEX_IS_ONE] else 0
                        log.info("Door status is: ${p16Model.doorStatus}, locker index is: ${p16Model.lockerIndex}")

                        lockerP16List.add(p16Model)
                    }
                } else {
                    val p16Model = P16PeripheralsModel(
                        index,
                        0,
                        RLockerSize.NOTHING,
                        LockerP16Status.UNREGISTERED,
                        false,
                        "",
                        true,
                        false,
                        RLockerKeyPurpose.UNKNOWN, "", "",
                        false, RActionRequired.NULL, false, false,
                        0, false, false, 0
                    )
                    p16Model.doorStatus = if( doorStatusList.size >= index ) doorStatusList[index - P16_FIRST_INDEX_IS_ONE] else 0
                    log.info("Door status is: ${p16Model.doorStatus}, locker index is: ${p16Model.lockerIndex}")

                    lockerP16List.add(p16Model)
                }
            }
        } else {
            for (index in P16_FIRST_INDEX_IS_ONE..P16_LIST_SIZE) {
                val p16Model = P16PeripheralsModel(
                    index,
                    0,
                    RLockerSize.NOTHING,
                    LockerP16Status.UNREGISTERED,
                    false,
                    "",
                    true,
                    false,
                    RLockerKeyPurpose.UNKNOWN, "", "",
                    false, RActionRequired.NULL, false, false,
                    0, false, false, 0
                )
                p16Model.doorStatus = if( doorStatusList.size >= index ) doorStatusList[index - P16_FIRST_INDEX_IS_ONE] else 0
                log.info("Door status is: ${p16Model.doorStatus}, locker index is: ${p16Model.lockerIndex}")

                lockerP16List.add(p16Model)
            }
        }
        return lockerP16List
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        if (App.ref.eventBus.isRegistered(this))
            App.ref.eventBus.unregister(this)
        enteredOnScreeen = true
    }

}
