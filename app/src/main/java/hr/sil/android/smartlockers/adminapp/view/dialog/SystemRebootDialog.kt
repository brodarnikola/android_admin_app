package  hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.databinding.DialogSystemRebootBinding
import hr.sil.android.smartlockers.adminapp.view.fragment.main.LockerSettingsFragment

class SystemRebootDialog constructor(val lockerSettingsFragment: LockerSettingsFragment)  : DialogFragment() {

    private lateinit var binding: DialogSystemRebootBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSystemRebootBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnConfirm.setOnClickListener {

                lockerSettingsFragment.systemRebootDevice()
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}