package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerUnit
import hr.sil.android.smartlockers.adminapp.core.remote.model.RRoleEnum
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.LockerP16Status
import hr.sil.android.smartlockers.adminapp.data.SplPlusPeripheralsModel
import hr.sil.android.smartlockers.adminapp.data.SplPlusDisableButtonInterface
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentSplPlusBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.SplPlusAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.DisableUserActionsDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import hr.sil.android.util.general.extensions.hexToByteArray
import kotlinx.coroutines.* 

class SplPlusFragment : BaseFragment(), SplPlusDisableButtonInterface {

    val log = logger()
    private var slaveMac: String = ""
    private var masterMac: String = ""
    var splPLusList: MutableList<SplPlusPeripheralsModel> = mutableListOf()
    private val FIRST_12_CHARACTERS_FROM_SLAVE_MAC_ADDRESS = 12
    private val SPL_PLUS_LIST_SIZE = 3
    private val SPL_PLUS_FIRST_INDEX_IS_ONE = 1
    private val LAST_REGISTERED_INDEX_PLUS_ONE = 1
    var device: MPLDevice? = null

    private lateinit var binding: FragmentSplPlusBinding
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

        masterMac = arguments?.getString("masterMac", "") ?: ""
        slaveMac = arguments?.getString("slaveMac", "") ?: ""
        log.info("Received master mac address is: " + masterMac + " received slave mac is: " + slaveMac)
        device = MPLDeviceStore.devices[masterMac]

        binding = FragmentSplPlusBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.rvSplPLusPeripherals.visibility = View.INVISIBLE

        val disableUserActionDialog = DisableUserActionsDialog()
        disableUserActionDialog.isCancelable = false
        disableUserActionDialog.show(
            (requireContext() as MainActivity).supportFragmentManager, "")

        try {

            GlobalScope.launch {

                withTimeout(60000) {

                    log.info("mac address not clean is: " + masterMac + " mac address clean is: " + masterMac.macRealToClean())

                    device = MPLDeviceStore.refreshMasterUnit(masterMac)
                    val lockerSplPlusData = device?.slaveUnits?.filter {
                        log.info("locker mac address is: " + it.mac)
                        it.mac.take(FIRST_12_CHARACTERS_FROM_SLAVE_MAC_ADDRESS) == slaveMac.macRealToClean()
                    }?.sortedBy { it.mac } ?: listOf()
                    //WSAdmin.getLockers(masterMac.macRealToClean())?.filter { it.masterMac == masterMac.macRealToClean() }?.sortedBy { it.mac }

                    splPLusList = mutableListOf()
                    insertDataIntoSplPLusList(lockerSplPlusData)

                    log.info("spl plus data is: ${lockerSplPlusData} + " + " spl plus size is: " + lockerSplPlusData.size)

                    withContext(Dispatchers.Main) {

                        disableUserActionDialog.dismiss()
                        initializeLockerSPLPlusAdapter()
                        setupButtonClickListener()
                    }
                }
            }
        }
        catch (e: TimeoutCancellationException) {
            log.info("TimeOutCalcelException error: ${e}")
            disableUserActionDialog.dismiss()
            //App.ref.toast(requireContext().getString(R.string.main_locker_registration_error))
        }
    }

    private fun checkUserRole(): Boolean {
        log.info("User role: ${UserUtil.user?.role?.name}")
        if (UserUtil.user?.role == RRoleEnum.SUPER_ADMIN || UserUtil.user?.role == RRoleEnum.ADMIN)
            return true
        return false
    }

    private fun setupButtonClickListener() {

        if (checkUserRole()) {
            binding.btnSaveChanges.isEnabled = true
            binding.btnSaveChanges.alpha = 1.0f
            binding.btnSaveChanges.setOnClickListener {

                val lastChangedIndex = SplPlusAdapter.getLastIndexChanged()

                var sendLockerSizeList = byteArrayOf()

                for (index in 0 until splPLusList.size) {

                    if (index == (lastChangedIndex + LAST_REGISTERED_INDEX_PLUS_ONE)) {
                        break
                    } else {
                        if (splPLusList[index].lockerStatus == LockerP16Status.UNREGISTERED
                            || splPLusList[index].lockerStatus == LockerP16Status.NEW || splPLusList[index].lockerStatus == LockerP16Status.NOTHING
                        ) {

                            // when you click on register locker,
                            // and choossed keyboard is spl single, then you don't need to set byte to NOTHING or to ?
                            // because new locker will receive L size
                            if (device?.keypadType != ParcelLockerKeyboardType.SPL) {
                                splPLusList[index].lockerSizeByte = RLockerSize.NOTHING
                            }
                            //splPLusList[index].lockerSizeByte = RLockerSize.NOTHING
                        }
                        sendLockerSizeList = sendLockerSizeList.plus(
                            splPLusList[index].lockerSizeByte.code
                                ?: 0x00
                        )
                    }
                }

                val firstByte =
                    sendLockerSizeList.filter { !it.equals(RLockerSize.NOTHING.code) }.firstOrNull()
                        ?: RLockerSize.NOTHING.code
                if (device?.keypadType == ParcelLockerKeyboardType.SPL && sendLockerSizeList.filter {
                        !it.equals(
                            RLockerSize.NOTHING.code
                        )
                    }.size > 1) {
                    //App.ref.toast(requireContext().getString(R.string.spl_plus_single_keyboard_multiple_selection))
                } else if (device?.keypadType == ParcelLockerKeyboardType.SPL && firstByte?.equals(
                        RLockerSize.S.code
                    ) == true
                ) {
                    //App.ref.toast(requireContext().getString(R.string.spl_plus_single_keyboard_wrong_selection))
                } else {

                    binding.progressBarButtonClick.visibility = View.VISIBLE
                    binding.btnSaveChanges.visibility = View.GONE

                    GlobalScope.launch {

                        val communicator =
                            MPLDeviceStore.devices[masterMac]?.createBLECommunicator(this@SplPlusFragment.requireContext())

                        if (communicator != null && communicator.connect()) {

                            log.info("Connection is done ${slaveMac}")

                            val result = communicator.registerSlaveSplPLus(
                                slaveMac.macRealToClean(),
                                sendLockerSizeList
                            )

                            withContext(Dispatchers.Main) {

                                if (result) {
                                    log.info("Data (byte array) for SPL PLUS successfully started to update")
                                    //App.ref.toast(this@SplPlusFragment.getString(R.string.update_locker_data_started))
                                    SplPlusAdapter.setLastIndexChanged(0)
                                } else {
                                    log.info("Data (byte array) for SPL PLUS did not successfully started to update")
                                    //App.ref.toast(this@SplPlusFragment.getString(R.string.app_generic_error))
                                }
                                binding.progressBarButtonClick.visibility = View.GONE
                                binding.btnSaveChanges.visibility = View.VISIBLE
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                //App.ref.toast(this@SplPlusFragment.getString(R.string.main_locker_ble_connection_error))
                                log.error("Error while connecting the peripheral ${slaveMac}")
                                binding.progressBarButtonClick.visibility = View.GONE
                                binding.btnSaveChanges.visibility = View.VISIBLE
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

    private fun insertDataIntoSplPLusList(lockerSplPlusData: List<RLockerUnit>) {

        if (lockerSplPlusData.isNotEmpty()) {

            val lastRegisteredIndex =
                lockerSplPlusData.last().mac.macRealToClean().hexToByteArray().last().toInt()
            log.info("last registered index is: " + lastRegisteredIndex)

            for (index in SPL_PLUS_FIRST_INDEX_IS_ONE..SPL_PLUS_LIST_SIZE) {

                var isLockerRegistered = false
                if (index < (lastRegisteredIndex + LAST_REGISTERED_INDEX_PLUS_ONE)) {
                    for (indexRegistered in 0 until lockerSplPlusData.size) {

                        val rLockerUnit = lockerSplPlusData[indexRegistered]
                        if (index == rLockerUnit.mac.macRealToClean().hexToByteArray().last().toInt()) {
                            val p16Model = SplPlusPeripheralsModel(
                                0,
                                0,
                                RLockerSize.NOTHING,
                                LockerP16Status.NOTHING,
                                false,
                                "",
                                false,
                                false
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
                            p16Model.lockerMac = rLockerUnit.mac
                            p16Model.isAvailable = rLockerUnit.isAvailable
                            p16Model.isDeleted = rLockerUnit.isDeleted
                            splPLusList.add(p16Model)
                            isLockerRegistered = true
                            break
                        }
                    }
                    if (!isLockerRegistered) {
                        val p16Model = SplPlusPeripheralsModel(
                            index,
                            0,
                            RLockerSize.NOTHING,
                            LockerP16Status.UNREGISTERED,
                            false,
                            "",
                            false,
                            true
                        )
                        splPLusList.add(p16Model)
                    }
                } else {
                    val p16Model = SplPlusPeripheralsModel(
                        index,
                        0,
                        RLockerSize.NOTHING,
                        LockerP16Status.UNREGISTERED,
                        false,
                        "",
                        false,
                        true
                    )
                    splPLusList.add(p16Model)
                }
            }
        } else {
            for (index in SPL_PLUS_FIRST_INDEX_IS_ONE..SPL_PLUS_LIST_SIZE) {
                val p16Model = SplPlusPeripheralsModel(
                    index,
                    0,
                    RLockerSize.NOTHING,
                    LockerP16Status.UNREGISTERED,
                    false,
                    "",
                    false,
                    true
                )
                splPLusList.add(p16Model)
            }
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
        else if (lockerSize == RLockerSize.S)
            return RLockerSize.S
        else
            return RLockerSize.L
    }

    private fun getLockerSizeIndex(
        lockerSize: RLockerSize,
        available: Boolean,
        deleted: Boolean
    ): Int {

        return when {
            !available && deleted -> 0
            lockerSize == RLockerSize.NOTHING -> 0
            lockerSize == RLockerSize.S -> 1
            else -> 2
        }
    }

    private fun initializeLockerSPLPlusAdapter() {

        binding.rvSplPLusPeripherals.visibility = View.VISIBLE

        val lockerSizeList = mutableListOf<String>()
        lockerSizeList.add("?")
        lockerSizeList.add("S")
        lockerSizeList.add("L")

        val adapter = SplPlusAdapter(
            splPLusList,
            this@SplPlusFragment,
            activity as MainActivity,
            lockerSizeList,
            masterMac,
            device?.keypadType
        )

        val manager = GridLayoutManager(requireContext(), 1, GridLayoutManager.VERTICAL, false)
        binding.rvSplPLusPeripherals.layoutManager = manager
        binding.rvSplPLusPeripherals.adapter = adapter
    }

    override fun disableItem(disableSaveButton: Boolean, lockerP16Status: LockerP16Status) {
        /*btnSaveChanges.isEnabled = lockerP16Status != LockerP16Status.NEW
        when (lockerP16Status) {
            LockerP16Status.NEW -> btnSaveChanges.alpha = 0.4f
            else -> btnSaveChanges.alpha = 1.0f
        }*/
    }

}
