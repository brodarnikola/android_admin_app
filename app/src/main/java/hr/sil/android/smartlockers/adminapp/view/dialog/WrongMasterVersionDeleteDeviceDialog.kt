package  hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.databinding.DialogWrongMasterVersionDeleteDeviceBinding

class WrongMasterVersionDeleteDeviceDialog constructor(val deviceType: MPLDeviceType?)  : DialogFragment() {

    private lateinit var binding: DialogWrongMasterVersionDeleteDeviceBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogWrongMasterVersionDeleteDeviceBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            if( deviceType == MPLDeviceType.MASTER )
                binding.tvDescription.text = getString(R.string.wrong_master_version_delete_device_mpl)
            else
                binding.tvDescription.text = getString(R.string.wrong_master_version_delete_device_spl_plus)

            binding.llConfirm.setOnClickListener {
                dialog.dismiss()
            }
        }

        return dialog!!
    }

}