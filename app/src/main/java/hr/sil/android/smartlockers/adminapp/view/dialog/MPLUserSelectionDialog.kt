package hr.sil.android.smartlockers.adminapp.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.MPLAppDeviceStatus
import hr.sil.android.smartlockers.adminapp.data.RMplSectionUserAccess
import hr.sil.android.smartlockers.adminapp.data.RMplUserAccess
import hr.sil.android.smartlockers.adminapp.databinding.DialogMplUserSelectionBinding
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.ManageMasterAccessAdapter
import hr.sil.android.smartlockers.adminapp.view.adapter.MplUserSelectionAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.jetbrains.anko.toast


@SuppressLint("ValidFragment")
class MPLUserSelectionDialog constructor(val manageMasterAccessAdapter: ManageMasterAccessAdapter.UserViewHolder,
                                         val userAccess: RMplUserAccess, val grantUserAccess: MutableList<RMplUserAccess>,
                                         val masterMac: String)  : DialogFragment() {

    val log = logger()
    var maxRowInEPaperMplDevice: Int = 0
    val correctList: MutableList<RMplSectionUserAccess> = mutableListOf()

    private lateinit var binding: DialogMplUserSelectionBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogMplUserSelectionBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            if( (activity as MainActivity).resources != null ) {
                maxRowInEPaperMplDevice = (activity as MainActivity).resources.getInteger(R.integer.number_of_rows_in_epaper)
                log.info("maxRowInEpaper device is: " + maxRowInEPaperMplDevice)
            }

            insertDataIntoList()
            initializeAdapterForRecyclerView()

            binding.btnConfirm.setOnClickListener {
                GlobalScope.launch {
                    try {
                        log.info("Selected user index is: " + MplUserSelectionAdapter.getLastIndexChanged())

                        if (userAccess.status == MPLAppDeviceStatus.NEW) {
                            val success = WSAdmin.grantAccessToMaster(userAccess.accessId, MplUserSelectionAdapter.getLastIndexChanged())
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    manageMasterAccessAdapter.allowUserAccessToMPL(userAccess.accessId, MPLAppDeviceStatus.REGISTERED, MplUserSelectionAdapter.getLastIndexChanged())
                                    MplUserSelectionAdapter.setLastIndexChanged(0)
                                    dismiss()
                                } else {
                                    //App.ref.toast(R.string.app_generic_server_error)
                                }

                            }
                        }

                    } catch (e: Error) {
                        Log.d("MplUserSelectionAdapt: " , e.toString())
                    }
                }
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

    private fun initializeAdapterForRecyclerView() {

        val masterAccessAdapter = MplUserSelectionAdapter(correctList, userAccess, requireContext() )

        binding.userSelectionRecyclerView.layoutManager = LinearLayoutManager(manageMasterAccessAdapter.itemView.context, LinearLayoutManager.VERTICAL, false)
        binding.userSelectionRecyclerView.adapter = masterAccessAdapter
    }

    private fun insertDataIntoList() {
        val userAccessList: List<RMplUserAccess> = grantUserAccess
        var leftCurrentMplSection = RMplUserAccess()
        var rightCurrentMplSection = RMplUserAccess()
        var userInsertedIndex = 0
        var increaseByEightUserIndex = 8

        for (i in 0 until maxRowInEPaperMplDevice) {
            for (userAccessItem in userAccessList) {
                if (i == userAccessItem.index && userAccessItem.status == MPLAppDeviceStatus.REGISTERED) {
                    leftCurrentMplSection = userAccessItem
                }
                if ((i + increaseByEightUserIndex) == userAccessItem.index && userAccessItem.status == MPLAppDeviceStatus.REGISTERED) {
                    rightCurrentMplSection = userAccessItem
                }
            }

            val completedRMplUserAccess = RMplSectionUserAccess()
            leftCurrentMplSection.index = userInsertedIndex
            increaseByEightUserIndex += userInsertedIndex
            rightCurrentMplSection.index = increaseByEightUserIndex
            userInsertedIndex++
            increaseByEightUserIndex = 8
            completedRMplUserAccess.leftMPLSection = leftCurrentMplSection
            completedRMplUserAccess.rightMPLSection = rightCurrentMplSection

            correctList.add(completedRMplUserAccess)
            leftCurrentMplSection = RMplUserAccess()
            rightCurrentMplSection = RMplUserAccess()
        }
    }

}
