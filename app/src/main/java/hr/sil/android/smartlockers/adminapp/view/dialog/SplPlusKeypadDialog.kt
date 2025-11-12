package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogSplPlusKeypadBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("ValidFragment")
class SplPlusKeypadDialog constructor(val device: MPLDevice?, val masterMac: String, val slaveMac: String?  )  : DialogFragment() {

    private val log = logger()

    private lateinit var binding: DialogSplPlusKeypadBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSplPlusKeypadBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            when {
                device?.keypadType == ParcelLockerKeyboardType.SPL_PLUS -> binding.radioGroup.check(R.id.splPlus)
                else -> binding.radioGroup.check(R.id.spl)
            }

            binding.btnConfirm.setOnClickListener {

                binding.progressBar.visibility = View.VISIBLE
                binding.btnConfirm.visibility = View.INVISIBLE

                val id: Int = binding.radioGroup.checkedRadioButtonId
                // If any radio button checked from radio group
                if (id != -1){

                    GlobalScope.launch {

                        val keypadType =  when {
                            resources.getResourceEntryName(binding.radioGroup.checkedRadioButtonId) == "spl" -> "SPL"
                            else -> "SPL_PLUS"
                        }

                        var result = false
                        val communicator = device?.createBLECommunicator(this@SplPlusKeypadDialog.context as Context)
                        if (communicator?.connect() == true) {
                            result = communicator.writeKeypadLayout(keypadType)
                        }
                        communicator?.disconnect()
                        if( result ) {
                            MPLDeviceStore.refreshMasterUnit(masterMac)
                        }

                        withContext(Dispatchers.Main) {

                            if( result ) {
                                //App.ref.toast( this@SplPlusKeypadDialog.getString(R.string.spl_plus_keyboard_success))
                                if( keypadType == "SPL" ) {
                                    val bundle = bundleOf("masterMac" to masterMac, "slaveMac" to slaveMac )
                                    log.info("Sended masterMacAddress to locker settings is: " + bundle + " to String: " + bundle.toString())
                                    findNavController().navigate(
                                        R.id.keyboard_dialog_to_spl_plus_fragment,
                                        bundle
                                    )
                                }
                                dismiss()
                            }
                            else {
                                //App.ref.toast(this@SplPlusKeypadDialog.getString(R.string.main_locker_ble_connection_error))
                                log.error("Error while connecting the device")
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.btnConfirm.visibility = View.VISIBLE
                            }
                        }
                    }

                }else{
                    // If no radio button checked in this radio group
                    Toast.makeText(requireContext(),"On button click : nothing selected",
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