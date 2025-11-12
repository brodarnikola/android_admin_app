package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.InstalationType
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogSlaveLockerSizeBinding
import hr.sil.android.smartlockers.adminapp.view.adapter.PeripheralAdapter


@SuppressLint("ValidFragment")
class LockerSlaveSizeDialog(
    val installationType: InstalationType,
    val itemView: View,
    val peripheralItemViewHolder: PeripheralAdapter,
    val slaveMacAddress: String
) : DialogFragment() {

    private val log = logger()
    var selectedSize: RLockerSize = RLockerSize.UNKNOWN
    var currentSelected: ImageView? = null
    var currentUnselectedDrawable: Drawable? = null
    var isSelectedLockerSizeFirstTime: Boolean = true

    private var xsSelected : Drawable? = null
    private var sSelected : Drawable? = null
    private var mSelected : Drawable? = null
    private var lSelected : Drawable? = null
    private var xlSelected : Drawable? = null

    private var xsUnselected : Drawable? = null
    private var sUnselected : Drawable? = null
    private var mUnselected : Drawable? = null
    private var lUnselected : Drawable? = null
    private var xlUnselected : Drawable? = null

    private lateinit var binding: DialogSlaveLockerSizeBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSlaveLockerSizeBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            if( installationType == InstalationType.TABLET )
                binding.llReducedMobility.visibility = View.VISIBLE
            else
                binding.llReducedMobility.visibility = View.GONE

            binding.sizeXs.setOnClickListener {
                handleSizeSelectionButton(it as ImageView, xsSelected, xsUnselected, RLockerSize.XS)
            }

            binding.sizeS.setOnClickListener {
                handleSizeSelectionButton(it as ImageView, sSelected, sUnselected, RLockerSize.S)
            }

            binding.sizeM.setOnClickListener {
                handleSizeSelectionButton(it as ImageView, mSelected, mUnselected, RLockerSize.M)
            }

            binding.sizeL.setOnClickListener {
                handleSizeSelectionButton(it as ImageView, lSelected, lUnselected, RLockerSize.L)
            }

            binding.sizeXL.setOnClickListener {
                handleSizeSelectionButton(it as ImageView, xlSelected, xlUnselected, RLockerSize.XL)
            }

            binding.btnConfirm.setOnClickListener {
                dismiss()
                peripheralItemViewHolder.addSlaveSizeDialog(itemView, slaveMacAddress, selectedSize, binding.reducedMobilityCheckBox.isChecked)
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }

        }

        return dialog!!
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        xsSelected = getDrawableAttrValue(R.attr.thmSlaveXSSelected)
        sSelected = getDrawableAttrValue(R.attr.thmSlaveSSelected)
        mSelected = getDrawableAttrValue(R.attr.thmSlaveMSelected)
        lSelected = getDrawableAttrValue(R.attr.thmSlaveLSelected)
        xlSelected = getDrawableAttrValue(R.attr.thmSlaveXLSelected)

        xsUnselected = getDrawableAttrValue(R.attr.thmSlaveXSUnselected)
        sUnselected = getDrawableAttrValue(R.attr.thmSlaveSUnselected)
        mUnselected = getDrawableAttrValue(R.attr.thmSlaveMUnselected)
        lUnselected = getDrawableAttrValue(R.attr.thmSlaveLUnselected)
        xlUnselected = getDrawableAttrValue(R.attr.thmSlaveXLUnselected)
    }

    //?attr/
    private fun getDrawableAttrValue(attr: Int): Drawable? {
        val attrArray = intArrayOf(attr)
        val typedArray = requireContext().obtainStyledAttributes(attrArray)
        val result = try {
            typedArray.getDrawable(0)
        } catch (exc: Exception) {
            null
        }
        typedArray.recycle()
        return result
    }

    private fun handleSizeSelectionButton(pickedButton: ImageView, selectedDrawable: Drawable?, unselectedDrawable: Drawable?, size: RLockerSize) {
        if (currentSelected != pickedButton) {
            pickedButton.background = selectedDrawable
            val drawable = if (isSelectedLockerSizeFirstTime) unselectedDrawable else currentUnselectedDrawable
            currentSelected?.background = drawable
            currentSelected = pickedButton
            currentUnselectedDrawable = unselectedDrawable
            isSelectedLockerSizeFirstTime = false
        }
        log.info("Picking size : ${size.name}")
        selectedSize = size
    }
}