package hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.InstalationType
import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerKeyPurpose
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.LockerOptionModel
import hr.sil.android.smartlockers.adminapp.databinding.DialogLockerOptionsP16Binding
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.LockerOptionsP16Adapter
import hr.sil.android.smartlockers.adminapp.view.adapter.PeripheralAdapterP16
import hr.sil.android.util.general.extensions.hexToByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockerOptionsP16Dialog(
    val slaveIndexP16Mac: String,
    val deviceP16Mac: String,
    val masterMacAddress: String,
    val isLockerInProximity: Boolean,
    val keyPurpose: RLockerKeyPurpose,
    val isAvailable: Boolean,
    val createdForName: String,
    val createdOnDate: String,
    val isMasterUnitInProximity: Boolean,
    val deletingKeyInProgressBLE: Boolean,
    val deletingKeyInProgressBACKEND: Boolean,
    val reducedMobility: Boolean,
    val cleaningNeeded: RActionRequired,
    val peripheralAdapterP16: PeripheralAdapterP16,
    val lockerIndex: Int,
    val installationType: InstalationType?,
    val adapterPosition: Int
) : DialogFragment() {

    private val log = logger()

    lateinit var lockerOptionAdapter: LockerOptionsP16Adapter

    private lateinit var binding: DialogLockerOptionsP16Binding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogLockerOptionsP16Binding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            log.info("Is in proximity: ${isLockerInProximity} in lockerOptions Dialog")

            val slaveP16Index =
                slaveIndexP16Mac.macRealToClean().hexToByteArray().last().toInt().toString()

            binding.tvTitle.text = resources.getString(R.string.manage_peripherals_p16_title, slaveP16Index, slaveIndexP16Mac.macCleanToReal())

            binding.ivArrowBack.setOnClickListener {
                dismiss()
            }

            val lockerOptions = addItemsToRecyclerView()

            lockerOptionAdapter = LockerOptionsP16Adapter(
                lockerOptions,
                slaveIndexP16Mac,
                deviceP16Mac,
                masterMacAddress,
                requireContext(),
                this@LockerOptionsP16Dialog,
                isLockerInProximity,
                keyPurpose,
                isAvailable,
                createdForName,
                createdOnDate,
                isMasterUnitInProximity,
                deletingKeyInProgressBLE,
                deletingKeyInProgressBACKEND,
                reducedMobility,
                cleaningNeeded,
                activity as MainActivity,
                peripheralAdapterP16,
                lockerIndex,
                installationType,
                adapterPosition
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

            log.info("Master mac address is: ${slaveIndexP16Mac}")
            val result = WSAdmin.getLockerDetails(slaveIndexP16Mac)
            withContext(Dispatchers.Main) {

                log.info("Invalidate keys in locker/{mac}: ${result?.invalidateKeys}")
                log.info("Locker mac is available: ${result?.isAvailable}")
                lockerOptionAdapter.updateDevice(result?.isAvailable ?: false)
            }
        }
    }

}