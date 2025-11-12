package  hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.databinding.DialogWrongLockerVersionBinding

class WrongLockerVersionDialog constructor(val currentLockerVersion: String, val currentMasterVersion: String)  : DialogFragment() {

    private lateinit var binding: DialogWrongLockerVersionBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogWrongLockerVersionBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.tvCurrentLockerVersion.setText(getString(R.string.current_locker_version, currentLockerVersion))
            binding.tvCurrentMasterVersion.setText(getString(R.string.current_master_version, currentMasterVersion))

            binding.llConfirm.setOnClickListener {
                dialog.dismiss()
            }
        }

        return dialog!!
    }

}