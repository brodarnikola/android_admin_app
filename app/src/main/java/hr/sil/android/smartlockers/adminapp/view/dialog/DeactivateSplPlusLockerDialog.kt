package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.LockerP16Status
import hr.sil.android.smartlockers.adminapp.databinding.DialogAlreadyGrantedAccessBinding
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeactivateLockerBinding
import hr.sil.android.smartlockers.adminapp.view.adapter.SplPlusAdapter

@SuppressLint("ValidFragment")
class DeactivateSplPlusLockerDialog constructor(val splPlusAdapter: SplPlusAdapter, val lockerIndex: Int, val newLockerP16Status: LockerP16Status, val adapterPosition: Int ) : DialogFragment() {

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

            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
//            val btnDialogYes = view.findViewById<Button>(R.id.btnSelect)
//            btnDialogYes.setOnClickListener {
//
//                splPlusAdapter.onItemSelected(lockerIndex, newLockerP16Status, adapterPosition)
//                dismiss()
//            }
//
//            val btnDialogNo = view.findViewById<Button>(R.id.btnCancel)
//            btnDialogNo.setOnClickListener {
//                dismiss()
//            }
        }

        return dialog!!
    }


}