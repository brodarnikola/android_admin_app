package hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMessageDataLog
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogAlreadyGrantedAccessBinding
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeleteAllAlarmsMessagesBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteAllAlarmsMessagesDialog constructor(val messageList: MutableList<RMessageDataLog>, val messageRecyclerView: RecyclerView) : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogDeleteAllAlarmsMessagesBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteAllAlarmsMessagesBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            binding.btnConfirm.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnConfirm.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                GlobalScope.launch {
                    WSAdmin.deleteAll()
                    DataCache.getAlarmMessageLog(true)
                    MPLDeviceStore.clear()
                    DataCache.getMasterUnits(true)
                    MPLDeviceStoreRemoteUpdater.forceUpdate()

                    withContext(Dispatchers.Main) {
                        messageList.clear()
                        messageRecyclerView.adapter?.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                        binding.btnConfirm.visibility = View.VISIBLE
                        binding.btnCancel.visibility = View.VISIBLE
                        dismiss()
                    }
                }

            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}