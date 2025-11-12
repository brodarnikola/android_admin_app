package hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.InstalationType
import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerKeyPurpose
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.LockerOptionModel
import hr.sil.android.smartlockers.adminapp.databinding.DialogLockerOptionsBinding
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.LockerOptionsAdapter
import hr.sil.android.smartlockers.adminapp.view.adapter.PeripheralAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockerOptionsDialog(
    val itemView: View,
    val peripheralItemViewHolder: PeripheralAdapter,
    val installationType: InstalationType,
    val slaveMacAddress: String,
    val masterMacAddress: String,
    val isLockerInProximity: Boolean,
    val keyPurpose: RLockerKeyPurpose,
    val createdForName: String,
    val createdOnDate: String,
    val isMasterUnitInProximity: Boolean,
    val deletingKeyInProgressBLE: Boolean,
    val deletingKeyInProgressBACKEND: Boolean,
    val cleaningNeeded: RActionRequired,
    val reducedMobility: Boolean,
    val lockerSize: RLockerSize
) : DialogFragment() {

    //val parentJob = Job()
    private val log = logger()

    lateinit var lockerOptionAdapter: LockerOptionsAdapter

    private lateinit var binding: DialogLockerOptionsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogLockerOptionsBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            log.info("Is in proximity: ${isLockerInProximity} in lockerOptions Dialog")

            binding.tvTitle.text = resources.getString(R.string.manage_peripherals_title,slaveMacAddress)

            binding.ivArrowBack.setOnClickListener {
                dismiss()
            }

            val lockerOptions = addItemsToRecyclerView()

            lockerOptionAdapter = LockerOptionsAdapter(
                lockerOptions,
                masterMacAddress,
                slaveMacAddress,
                requireContext(),
                installationType,
                this@LockerOptionsDialog,
                isLockerInProximity,
                keyPurpose,
                createdForName,
                createdOnDate,
                isMasterUnitInProximity,
                deletingKeyInProgressBLE,
                deletingKeyInProgressBACKEND,
                cleaningNeeded,
                reducedMobility,
                lockerSize,
                activity as MainActivity
            )
            binding.rlLockerOptions.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rlLockerOptions.adapter = lockerOptionAdapter

            isKeyInsideLocker()
        }

        return dialog!!
    }

    private fun addItemsToRecyclerView() : List<LockerOptionModel> {

        val lockerOptionsList = mutableListOf<LockerOptionModel>( )

        val lockerDiscover = LockerOptionModel()
        lockerDiscover.description = resources.getString(R.string.locker_action_discover)
        lockerDiscover.buttonText = resources.getString(R.string.peripheral_settings_discover_peripheral)
        lockerOptionsList.add(lockerDiscover)

        val lockerForceOpen = LockerOptionModel()
        lockerForceOpen.description = resources.getString(R.string.locker_action_force_open)
        lockerForceOpen.buttonText = resources.getString(R.string.peripheral_settings_force_open)
        lockerOptionsList.add(lockerForceOpen)

        val lockerInvalidateKey = LockerOptionModel()
        if( isLockerInProximity && isMasterUnitInProximity )
            lockerInvalidateKey.description = resources.getString(R.string.locker_action_delete_key_in_proximity)
        else
            lockerInvalidateKey.description = resources.getString(R.string.locker_action_delete_key_not_in_proximity)
        lockerInvalidateKey.buttonText = resources.getString(R.string.invalidate_key)
        lockerDiscover.isEmptyLocker = false
        lockerOptionsList.add(lockerInvalidateKey)

        val lockerRemoveLocker = LockerOptionModel()
        lockerRemoveLocker.description = resources.getString(R.string.locker_action_delete_locker)
        lockerRemoveLocker.buttonText = resources.getString(R.string.delete_locker)
        lockerOptionsList.add(lockerRemoveLocker)

        return lockerOptionsList
    }

    private fun isKeyInsideLocker() {
        lifecycleScope.launch {

            log.info("Master mac address is: ${slaveMacAddress}")
            val result = WSAdmin.getLockerDetails(slaveMacAddress)
            withContext(Dispatchers.Main) {

                log.info("Invalidate keys in locker/{mac}: ${result?.invalidateKeys}")
                log.info("Locker mac is available: ${result?.isAvailable}")
                lockerOptionAdapter.updateDevice(result?.isAvailable ?: false)
            }
        }
    }

    /*override fun onPause() {
        super.onPause()
        if( parentJob.isActive ) parentJob.cancel()
    }*/

}