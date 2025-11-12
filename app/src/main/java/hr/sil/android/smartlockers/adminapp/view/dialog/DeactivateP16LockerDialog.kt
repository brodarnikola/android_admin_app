package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.LockerP16Status
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeactivateLockerBinding
import hr.sil.android.smartlockers.adminapp.view.adapter.PeripheralAdapterP16

@SuppressLint("ValidFragment")
class DeactivateP16LockerDialog constructor(val peripheralP16Adapter: PeripheralAdapterP16, val lockerIndex: Int, val newLockerP16Status: LockerP16Status, val adapterPosition: Int ) : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogDeactivateLockerBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeactivateLockerBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnSelect.setOnClickListener {

                peripheralP16Adapter.onItemSelected(lockerIndex, newLockerP16Status, adapterPosition)
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }


}