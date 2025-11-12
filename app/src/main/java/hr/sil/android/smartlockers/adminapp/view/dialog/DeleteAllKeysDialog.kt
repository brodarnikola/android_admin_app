package  hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLinuxDeleteAllKeysResponse
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeleteAllKeysBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.view.adapter.UnsuccessfullyDeleteAllKeysLinuxAdapter
import hr.sil.android.smartlockers.adminapp.view.fragment.main.DeviceDetailsFragment
import kotlinx.coroutines.* 


@SuppressLint("ValidFragment")
class DeleteAllKeysDialog constructor(val deviceDetailsFragment: DeviceDetailsFragment, val masterMacAddress: String, val isInBleProximity: Boolean, val isLinuxDevice: Boolean )  : DialogFragment() {

    val log = logger()
    val parentJob = Job()

    private lateinit var binding: DialogDeleteAllKeysBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteAllKeysBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            setCorrectTextForNumberOfKeys()

            deleteAllKeysSetOnClickListener()

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

    private fun deleteAllKeysSetOnClickListener() {
        binding.btnConfirm.setOnClickListener {

            binding.btnCancel.visibility = View.GONE
            binding.btnConfirm.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            if( isLinuxDevice ) {
                log.info("Master mac is: ${masterMacAddress}")
                lifecycleScope.launch {
                    val deletingKeys = WSAdmin.deleteAllKeysOnLinuxDevice(masterMacAddress)
                    var deleteSuccessfullyAllKeys = true
                    val unsuccessfullyDeleteKeysList = mutableListOf<RLinuxDeleteAllKeysResponse>()
                    if (deletingKeys != null) {
                        for( key in deletingKeys ) {
                            if( !key.success ) {
                                unsuccessfullyDeleteKeysList.add(key)
                                deleteSuccessfullyAllKeys = false
                            }
                        }
                    }

                    if(deleteSuccessfullyAllKeys== true) {
                        DataCache.getMasterUnit(masterMacAddress, true)
                    }

                    withContext(Dispatchers.Main) {
                        if (deleteSuccessfullyAllKeys == true) {
                            //App.ref.toast(R.string.app_generic_success)
                            log.info("Successfully deleted all keys on masterunit ${masterMacAddress} , on backend side")
                            dismiss()
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.btnCancel.visibility = View.VISIBLE
                            binding.btnConfirm.visibility = View.VISIBLE
                            binding.rlUnsuccessfullyDeletedKeys.visibility = View.VISIBLE
                            log.info("Not successfully deleted all keys on masterunit ${masterMacAddress} , on backend side")

                            val unsuccessfullyDeleteAllKeysLinuxAdapter =
                                UnsuccessfullyDeleteAllKeysLinuxAdapter(
                                    unsuccessfullyDeleteKeysList
                                )

                            if (binding.rlUnsuccessfullyDeletedKeys != null && unsuccessfullyDeleteAllKeysLinuxAdapter != null) {
                                binding.rlUnsuccessfullyDeletedKeys.layoutManager =
                                    LinearLayoutManager(
                                        requireContext(),
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )
                                binding.rlUnsuccessfullyDeletedKeys.adapter = unsuccessfullyDeleteAllKeysLinuxAdapter
                            }
                        }
                    }
                }
            }
            else if( isInBleProximity ) {
                GlobalScope.launch {

                    val communicator =
                        MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(deviceDetailsFragment.context as Context)
                    if (communicator?.connect() == true) {
                        log.info("Successfully connected $masterMacAddress, started to deleting all keys on master unit, on firmware side")

                        val result = communicator.deleteAllKeysOnMasterUnit()

                        withContext(Dispatchers.Main) {
                            if (!result) {
                                //App.ref.toast(requireContext().getString(R.string.app_generic_error))
                                log.error("Error in deleting keys!")
                                binding.progressBar.visibility = View.GONE
                                binding.btnCancel.visibility = View.VISIBLE
                                binding.btnConfirm.visibility = View.VISIBLE
                            } else {

                                dismiss()
                                //App.ref.toast(R.string.app_generic_success)
                                log.info("Successfully delete keys in master unit, mac address: $masterMacAddress , on firmware side")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                            log.error("Error while connecting the device")

                            binding.progressBar.visibility = View.GONE
                            binding.btnCancel.visibility = View.VISIBLE
                            binding.btnConfirm.visibility = View.VISIBLE
                        }
                    }
                    communicator?.disconnect()
                }
            }
            else {
                // TODO delete all keys when device is not in proximity, delete from backends
                log.info("Master mac is: ${masterMacAddress}")
                GlobalScope.launch {
                    val result = WSAdmin.deleteAllKeysOnLocker(masterMacAddress)
                    if(result == true) {
                        DataCache.getMasterUnit(masterMacAddress, true)
                    }

                    withContext(Dispatchers.Main) {
                        if (result == true) {
                            //App.ref.toast(R.string.app_generic_success)
                            log.info("Successfully deleted all keys on masterunit ${masterMacAddress} , on backend side")
                            dismiss()
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.btnCancel.visibility = View.VISIBLE
                            binding.btnConfirm.visibility = View.VISIBLE
                            log.info("Not successfully deleted all keys on masterunit ${masterMacAddress} , on backend side")
                            //App.ref.toast(R.string.app_generic_error)
                        }
                    }
                }
            }

        }
    }

    private fun setCorrectTextForNumberOfKeys() {
        GlobalScope.launch(parentJob) {
            val result = WSAdmin.getLockers(masterMacAddress)?.filter { it.keys != null && it.keys.isNotEmpty() } ?: listOf()
            withContext(Dispatchers.Main) {
                binding.progressGetAllKeys.visibility = View.GONE
                binding.btnConfirm.isEnabled = true
                binding.btnConfirm.alpha = 1.0f
                if( result.size > 1 )
                    binding.tvQuestion.text = resources.getString(R.string.delete_all_keys  )
                else
                    binding.tvQuestion.text = resources.getString(R.string.delete_key )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if( parentJob.isActive ) parentJob.cancel()
    }
}