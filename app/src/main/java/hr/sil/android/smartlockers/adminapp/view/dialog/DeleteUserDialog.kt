package hr.sil.android.smartlockers.adminapp.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RDeleteUser
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.DialogDeleteUserBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import org.jetbrains.anko.toast

class DeleteUserDialog constructor(val endUserId: Int, val masterMacAddress: String)  : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogDeleteUserBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteUserBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            log.info("EndUser id is: ${endUserId}, inside delete user dialog, master mac is: ${masterMacAddress}")
            binding.btnConfirm.setOnClickListener {

                GlobalScope.launch {
                    val userData = RDeleteUser()
                    userData.id = endUserId

                    val backendResult = WSAdmin.deleteUserFromSystem(userData = userData)
                    withContext(Dispatchers.Main) {

                        if( backendResult ) {

                            dismiss()
                            val bundle = bundleOf("masterMac" to masterMacAddress)
                            log.info("User id: " + bundle + " to String: " + bundle.toString())
                            findNavController().navigate(
                                R.id.action_manageUserDetails_to_manage_users,
                                bundle
                            )
                            //App.ref.toast(resources.getString(R.string.app_generic_success))
                        }
                        else {
                            log.error("Error while deleting user")
                            //App.ref.toast(resources.getString(R.string.app_generic_error))
                        }
                    }
                }
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}