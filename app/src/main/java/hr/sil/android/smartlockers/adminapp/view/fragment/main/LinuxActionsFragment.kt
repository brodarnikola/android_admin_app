package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLinuxSizeReducedMobilityRequest
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentLinuxActionsBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.LockerSizeAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.DisableUserActionsDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.LinuxConfirmationActionsDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.* 

class LinuxActionsFragment : BaseFragment() {

    val log = logger()
    lateinit var masterMac: String
    lateinit var lockerMac: String
    lateinit var lockerMacIndex: String

    var lockerId: Int = 0
    var deviceLatitude: Double = 0.0
    var deviceLongitude: Double = 0.0
    lateinit var deviceType: String
    lateinit var keyPurpose: String
    lateinit var keyFrom: String
    lateinit var keyFor: String
    lateinit var keyDate: String
    lateinit var lockerSize: String
    lateinit var needsCleaning: String
    var reducedMobility: Boolean = false
    private var device: MPLDevice? = null

    private lateinit var binding: FragmentLinuxActionsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLinuxActionsBinding.inflate(layoutInflater)

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
        lockerMac = arguments?.getString("lockerMac", "") ?: ""
        lockerMacIndex = arguments?.getString("lockerMacIndex", "") ?: ""

        lockerId = arguments?.getInt("lockerId", 0) ?: 0
        deviceLatitude = arguments?.getDouble("deviceLatitude", 0.0) ?: 0.0
        deviceLongitude = arguments?.getDouble("deviceLongitude", 0.0) ?: 0.0
        deviceType = arguments?.getString("deviceType", "") ?: ""
        keyPurpose = arguments?.getString("keyPurpose", "") ?: ""
        keyFrom = arguments?.getString("keyFrom", "") ?: ""
        keyFor = arguments?.getString("keyFor", "") ?: ""
        keyDate = arguments?.getString("keyDate", "") ?: ""
        lockerSize = arguments?.getString("lockerSize", "") ?: ""
        needsCleaning = arguments?.getString("needsCleaning", "") ?: ""
        reducedMobility = arguments?.getBoolean("reducedMobility", false) ?: false
        log.info("Received mac address is: ${masterMac}, locker mac address is: ${lockerMac}, locker size is: ${lockerSize} ")
        device = MPLDeviceStore.devices[masterMac]

        log.info("Locker id is: ${lockerId}")

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        initializeUi()
        addValuesToElements()
    }

    private fun initializeUi() {

        if (keyPurpose != "UNKNOWN" && keyDate != "" && keyFrom != "")
            showKeyViewElements()
        else
            hideKeyViewElements()

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            val disableUserActionDialog = DisableUserActionsDialog()
            disableUserActionDialog.isCancelable = false
            disableUserActionDialog.show(
                (requireContext() as MainActivity).supportFragmentManager, ""
            )
            lifecycleScope.launch {
                try {
                    withTimeout(15000) {
                        val lockerData = RLinuxSizeReducedMobilityRequest()
                        lockerData.actionRequired =
                            if ( binding.needsCleaningCheckBox.isChecked) RActionRequired.CLEANING else null
                        lockerData.lockerId = lockerId
                        lockerData.lockerSize =  binding.spinnerLockerSize.selectedItem.toString()
                        lockerData.reducedMobility =  binding.reducedMobilityCheckBox.isChecked
                        val updateLinux = WSAdmin.updateCLoudLockerData(lockerData)

                        withContext(Dispatchers.Main) {
                            disableUserActionDialog.dismiss()
                            if (updateLinux != null)
                                print(getString(R.string.app_generic_success))
                                // IMPORTANT ----> show toast !!!!!!!
                                //App.ref.toast(getString(R.string.app_generic_success))
                            else
                                // IMPORTANT ----> show toast !!!!!!!
                                print(getString(R.string.app_generic_error))
                                //App.ref.toast(getString(R.string.app_generic_error))
                        }
                    }
                }
                catch (e: TimeoutCancellationException) {
                    log.info("TimeOutCalcelException error: ${e}")
                    disableUserActionDialog.dismiss()
                    //App.ref.toast(requireContext().getString(R.string.app_generic_error))
                }
            }
        }
    }

    fun hideKeyViewElements() {
        binding.tvKeyPurposeTitle.visibility = View.GONE
        binding.tvKeyPurposeValue.visibility = View.GONE
        binding.tvKeyCreatedFromTitle.visibility = View.GONE
        binding.tvKeyCreatedFromValue.visibility = View.GONE
        binding.tvKeyCreatedForTitle.visibility = View.GONE
        binding.tvKeyCreatedForValue.visibility = View.GONE
        binding.tvKeyCreatedDate.visibility = View.GONE
        binding.tvKeyCreatedDateValue.visibility = View.GONE
        binding.llImageInvalidateKey.visibility = View.GONE
        binding.tvInvalidateKeyDescription.visibility = View.GONE
        binding.fifthDivider.visibility = View.GONE
    }

    private fun showKeyViewElements() {
        binding.tvKeyPurposeTitle.visibility = View.VISIBLE
        binding.tvKeyPurposeValue.visibility = View.VISIBLE
        binding.tvKeyCreatedFromTitle.visibility = View.VISIBLE
        binding.tvKeyCreatedFromValue.visibility = View.VISIBLE
        binding.tvKeyCreatedForTitle.visibility = View.VISIBLE
        binding.tvKeyCreatedForValue.visibility = View.VISIBLE
        binding.tvKeyCreatedDate.visibility = View.VISIBLE
        binding.tvKeyCreatedDateValue.visibility = View.VISIBLE
        binding.llImageInvalidateKey.visibility = View.VISIBLE
        binding.tvInvalidateKeyDescription.visibility = View.VISIBLE
        binding.fifthDivider.visibility = View.VISIBLE
    }

    private fun addValuesToElements() {

        binding.tvTypeValue.text = deviceType
        binding.tvLockerIndexValue.text = lockerMacIndex
        binding.tvSlaveMacAddressValue.text = lockerMac

        if (keyPurpose != "UNKNOWN" && keyDate != "" && keyFrom != "") {
            binding.tvKeyPurposeValue.text = keyPurpose
            binding.tvKeyCreatedFromValue.text = keyFrom
            binding.tvKeyCreatedForValue.text = keyFor
            binding.tvKeyCreatedDateValue.text = keyDate
        }

        binding.needsCleaningCheckBox.isChecked = if (needsCleaning == "NULL") false else true
        binding.reducedMobilityCheckBox.isChecked = reducedMobility

        val lockerSizeList = mutableListOf<String>()
        lockerSizeList.add("XS")
        lockerSizeList.add("S")
        lockerSizeList.add("M")
        lockerSizeList.add("L")
        lockerSizeList.add("XL")

        binding.spinnerLockerSize.adapter = LockerSizeAdapter(lockerSizeList)
        binding.spinnerLockerSize.setSelection(lockerSizeList.indexOfFirst { it == lockerSize })

        setupClickListeners()
    }

    fun updateUiAfterDeletingKey() {
        hideKeyViewElements()
    }

    private fun setupClickListeners() {

        binding.btnForceOpen.setOnClickListener {
            val linuxDialog = LinuxConfirmationActionsDialog(lockerMac, "forceOpen", deviceLatitude, deviceLongitude)
            linuxDialog.show((requireContext() as MainActivity).supportFragmentManager, "")
        }

        binding.btnInvalidateKey.setOnClickListener {
            val linuxDialog = LinuxConfirmationActionsDialog(lockerMac, "invalidateKey", deviceLatitude, deviceLongitude)
            linuxDialog.show((requireContext() as MainActivity).supportFragmentManager, "")
        }
    }


}