package hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogNoEpaperTypeBinding

class NoEPaperTypeNoPreviewDialog constructor(val descriptionText: String) : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogNoEpaperTypeBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogNoEpaperTypeBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.tvDescription.text = descriptionText

            binding.llConfirm.setOnClickListener {
                dialog.dismiss()
            }
        }

        return dialog!!
    }


}