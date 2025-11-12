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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.EPaperType
import hr.sil.android.smartlockers.adminapp.databinding.DialogAlreadyGrantedAccessBinding
import hr.sil.android.smartlockers.adminapp.databinding.DialogChooseEpaperTypeBinding
import hr.sil.android.smartlockers.adminapp.view.fragment.main.DeviceDetailsFragment


@SuppressLint("ValidFragment")
class ChoseEPaperTypeDialog constructor(val mplItemDetailsFragment: DeviceDetailsFragment)  : DialogFragment() {

    private lateinit var binding: DialogChooseEpaperTypeBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChooseEpaperTypeBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.ePaperHighResolution.isChecked = true

            binding.btnConfirm.setOnClickListener {

                val id: Int = binding.radioGroupEPaper.checkedRadioButtonId
                // If any radio button checked from radio group
                if (id != -1){

                    val ePaperType = when {
                        resources.getResourceEntryName(binding.radioGroupEPaper.checkedRadioButtonId) == "noEpaper" -> EPaperType.NO_EPAPER
                        resources.getResourceEntryName(binding.radioGroupEPaper.checkedRadioButtonId) == "ePaperLowResolution" -> EPaperType.LOW_RESOLUTION
                        else -> EPaperType.HIGH_RESOLUTION
                    }
                    mplItemDetailsFragment.chooseEpaperType(ePaperType)
                    dismiss()

                }else{
                    // If no radio button checked in this radio group
                    Toast.makeText(requireContext(),R.string.nothing_selected,
                        Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}