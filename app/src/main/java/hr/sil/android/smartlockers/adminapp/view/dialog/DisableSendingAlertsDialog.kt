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
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnitRequest
import hr.sil.android.smartlockers.adminapp.core.remote.model.RPowerTypeEnum
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.databinding.DialogAlreadyGrantedAccessBinding
import hr.sil.android.smartlockers.adminapp.databinding.DialogStopSendingAlertsBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore.forceRefreshMasterUnitInLockerSettings
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.jetbrains.anko.toast

@SuppressLint("ValidFragment")
class DisableSendingAlertsDialog constructor(val switchSendingAlerts: SwitchCompat, val macAddress: String ) : DialogFragment() {

    val log = logger()

    private var device: MPLDevice? = null

    private lateinit var binding: DialogStopSendingAlertsBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogStopSendingAlertsBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            device = MPLDeviceStore.devices[macAddress]

            binding.btnSelect.setOnClickListener {

                lifecycleScope.launch {
                    val request = RMasterUnitRequest()
                    request.name = device?.masterUnitName ?: ""
                    request.address = device?.masterUnitAddress ?: ""

                    request.latitude = device?.latitude ?: 0.0
                    request.longitude =device?.longitude ?: 0.0
                    request.epdType___id = device?.ePaperTypeId
                    request.powerType =
                        if (device?.powerSaving == true) RPowerTypeEnum.BATTERY else RPowerTypeEnum.LINE
                    request.allowPinSave = device?.allowPinSave ?: false
                    request.networkConfigurationId = device?.networkConfigurationId ?: 0
                    request.alertsEnabled = false

                    log.info("Get Name:  ${request.name}")
                    log.info("Get Address: ${request.address}")
                    log.info("Get ePapertype id: ${request.epdType___id}")
                    log.info("Get networkConfiguration id: ${request.networkConfigurationId}")

                    val resultChangeSendingAlerts =
                        WSAdmin.modifyMasterUnit(macAddress.macRealToClean(), request) != null
                    if( resultChangeSendingAlerts )
                        updateLocalDataForThisMasterUnit()
                    withContext(Dispatchers.Main) {
                        if (resultChangeSendingAlerts) {
                            //App.ref.toast(resources.getString(R.string.app_generic_success))
                            switchSendingAlerts.isChecked = false
                        } else {
                            //App.ref.toast(resources.getString(R.string.app_generic_error))
                            switchSendingAlerts.isChecked = true
                        }
                        dismiss()
                    }
                }
            }

            binding.btnCancel.setOnClickListener {
                switchSendingAlerts.isChecked = true
                dismiss()
            }
        }

        return dialog!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private suspend fun updateLocalDataForThisMasterUnit() {
        forceRefreshMasterUnitInLockerSettings(macAddress)
        MPLDeviceStoreRemoteUpdater.forceUpdate()
    }

}