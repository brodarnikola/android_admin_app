package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeactivateP16Binding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.view.fragment.main.PeripheralsP16Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ValidFragment")
class DeactivateP16 constructor( val masterMac: String, val slaveMac: String, val peripheralsP16Fragment: PeripheralsP16Fragment) : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogDeactivateP16Binding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeactivateP16Binding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnSelect.setOnClickListener {

                binding.progressBar.visibility = View.VISIBLE
                binding.btnSelect.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE

                var sendLockerSizeList = byteArrayOf()

                for (index in 0 until 16) {
                    sendLockerSizeList = sendLockerSizeList.plus( 0x00)
                }

                log.info("Size of list is: ${sendLockerSizeList.size}")

                GlobalScope.launch {

                    val communicator = MPLDeviceStore.devices[masterMac]?.createBLECommunicator(peripheralsP16Fragment.requireContext())
                    if (communicator != null && communicator.connect()) {

                        log.info("Connection is done ${slaveMac}")

                        val result = communicator.registerSlaveP16Locker(slaveMac.macRealToClean(), sendLockerSizeList)

                        withContext(Dispatchers.Main) {

                            when {
                                result -> {
                                    log.info("Data(byte array) for P16 successfully started to update")
                                    //App.ref.toast(peripheralsP16Fragment.getString(R.string.update_locker_data_started))
                                    dismiss()
                                }
                                else -> {
                                    log.info("Data(byte array) for P16 did not successfully started to update")
                                    //App.ref.toast(peripheralsP16Fragment.getString(R.string.app_generic_error))
                                }
                            }
                            binding.progressBar.visibility = View.GONE
                            binding.btnSelect.visibility = View.VISIBLE
                            binding.btnCancel.visibility = View.GONE
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(R.string.main_locker_ble_connection_error)
                            log.error("Error while connecting the peripheral ${slaveMac}")

                            binding.progressBar.visibility = View.GONE
                            binding.btnSelect.visibility = View.VISIBLE
                            binding.btnCancel.visibility = View.GONE
                        }
                    }
                    communicator?.disconnect()
                }
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }


}