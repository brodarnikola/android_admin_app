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
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogAlreadyGrantedAccessBinding
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeleteLockerSlaveBinding
import hr.sil.android.smartlockers.adminapp.view.adapter.LockerOptionsP16Adapter

class DeleteLockerSlaveP16Dialog constructor(val lockerOptionsHolder: LockerOptionsP16Adapter.LockerKeyOptionsHolder) : DialogFragment() {

    private val log = logger()

    private lateinit var binding: DialogDeleteLockerSlaveBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteLockerSlaveBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnConfirm.setOnClickListener {
                dismiss()
                lockerOptionsHolder.deleteLocker()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}