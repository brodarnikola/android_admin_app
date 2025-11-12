package hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.MPLAppDeviceStatus
import hr.sil.android.smartlockers.adminapp.data.RMplUserAccess
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeleteMplUserAccessBinding
import hr.sil.android.smartlockers.adminapp.view.adapter.ManageMasterAccessAdapter

class DeleteUserFromMPLDialog(
    val fragmentContext: Context,
    val userAccess: RMplUserAccess,
    val masterAccessAdapter: ManageMasterAccessAdapter.UserViewHolder,
    val masterMac: String
) : DialogFragment() {


    private val log = logger()

    private lateinit var binding: DialogDeleteMplUserAccessBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteMplUserAccessBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            when {
                userAccess.status == MPLAppDeviceStatus.REGISTERED -> binding.tvTitle.text = fragmentContext.getString(R.string.manage_mpl_access_remove_rights)
                else -> binding.tvTitle.text = fragmentContext.getString(R.string.manage_mpl_access_remove_access)
            }

            binding.btnConfirm.setOnClickListener {

                if( userAccess.status == MPLAppDeviceStatus.REGISTERED )
                    masterAccessAdapter.removeUserAccessFromMPL( masterMac, userAccess.index )
                else {
                    masterAccessAdapter.removeUserRequestFromMPL( userAccess.accessId, MPLAppDeviceStatus.REJECTED )
                }
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }


}