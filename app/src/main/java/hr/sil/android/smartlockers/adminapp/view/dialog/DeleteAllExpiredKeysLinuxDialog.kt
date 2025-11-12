package  hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
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
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLinuxDeleteAllKeysResponse
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeleteAllExpiredKeysBinding
import hr.sil.android.smartlockers.adminapp.view.adapter.UnsuccessfullyDeleteAllKeysLinuxAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("ValidFragment")
class DeleteAllExpiredKeysLinuxDialog constructor(val masterMacAddress: String) : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogDeleteAllExpiredKeysBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteAllExpiredKeysBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

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

            log.info("Master mac is: ${masterMacAddress}")
            lifecycleScope.launch {
                val deletingKeys = WSAdmin.deleteAllExpiredKeysOnLinuxDevice(masterMacAddress)
                var deleteSuccessfullyAllKeys = true
                val unsuccessfullyDeleteKeysList = mutableListOf<RLinuxDeleteAllKeysResponse>()
                if (deletingKeys != null) {
                    for (key in deletingKeys) {
                        if (!key.success) {
                            unsuccessfullyDeleteKeysList.add(key)
                            deleteSuccessfullyAllKeys = false
                        }
                    }
                }

                if (deleteSuccessfullyAllKeys == true) {
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
                            binding.rlUnsuccessfullyDeletedKeys.adapter =
                                unsuccessfullyDeleteAllKeysLinuxAdapter
                        }
                    }
                }
            }
        }
    }

}