package  hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.databinding.DialogPowerSavingWarningBinding
import hr.sil.android.smartlockers.adminapp.view.fragment.main.LockerSettingsFragment

class PowerSavingWarningDialog constructor( val mplItemDetailsFragment: LockerSettingsFragment)  : DialogFragment() {

    private lateinit var binding: DialogPowerSavingWarningBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPowerSavingWarningBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.llConfirm.setOnClickListener {
                dialog.dismiss()
            }
        }

        return dialog!!
    }

}