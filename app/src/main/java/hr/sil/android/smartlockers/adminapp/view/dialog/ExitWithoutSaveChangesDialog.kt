package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogExitWithoutSaveChangesBinding

@SuppressLint("ValidFragment")
class ExitWithoutSaveChangesDialog constructor(val masterMac: String) : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogExitWithoutSaveChangesBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogExitWithoutSaveChangesBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnSelect.setOnClickListener {
                dismiss()
                val bundle = bundleOf("masterMac" to masterMac)
                log.info("Sended masterMacAddress to manage userAccessList is: " + bundle + " to String: " + bundle.toString())
                findNavController().navigate(
                    R.id.p16_lockers_fragment_to_manage_peripherals_fragment,
                    bundle
                )
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}