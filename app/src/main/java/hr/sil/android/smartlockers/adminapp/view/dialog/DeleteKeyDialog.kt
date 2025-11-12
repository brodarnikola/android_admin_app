package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeleteKeyBinding
import hr.sil.android.smartlockers.adminapp.view.adapter.LockerOptionsAdapter


@SuppressLint("ValidFragment")
class DeleteKeyDialog constructor(val lockerOptionsHolder: LockerOptionsAdapter.LockerKeyOptionsHolder,
                                  val isInProximity: Boolean, val isInMasterUnitProximity: Boolean)  : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogDeleteKeyBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteKeyBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            if( isInProximity && isInMasterUnitProximity ) {
                binding.tvQuestion.text = resources.getString(R.string.delete_key_in_proximity)
            }
            else {
                binding.tvQuestion.text = resources.getString(R.string.delete_key_not_in_proximity)
            }

            binding.btnConfirm.setOnClickListener {
                dismiss()
                lockerOptionsHolder.deleteKey()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}