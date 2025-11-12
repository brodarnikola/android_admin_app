package hr.sil.android.smartlockers.adminapp.view.adapter

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.InstalationType
import hr.sil.android.smartlockers.adminapp.core.remote.model.RAssignedGroup
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnitType
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.MPLAppDeviceStatus
import hr.sil.android.smartlockers.adminapp.data.MplGrantUserAccessInterface
import hr.sil.android.smartlockers.adminapp.data.RMplUserAccess
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.AlreadyGrantedAccessDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteUserFromMPLDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.MPLUserSelectionDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext 

class ManageMasterAccessAdapter(
    var userAccessList: MutableList<RMplUserAccess>,
    val masterMac: String,
    val ctx: Context,
    val assignedGroupsToEpaper: List<RAssignedGroup>,
    val masterUnitType: RMasterUnitType?,
    val installationType: InstalationType?,
    val clicklistener: (RMplUserAccess) -> Unit
) : RecyclerView.Adapter<ManageMasterAccessAdapter.UserViewHolder>() {

    val log = logger()

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bindItem(userAccessList[position])

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_manage_users, parent, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount() = userAccessList.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        MplGrantUserAccessInterface {
        val log = logger()
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val phone: TextView = itemView.findViewById(R.id.tvUserPhone)
        val userEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val groupName: TextView = itemView.findViewById(R.id.tvUserGroupName)
        val delete: ImageView = itemView.findViewById(R.id.ivUserDelete)
        val add: ImageView = itemView.findViewById(R.id.ivUserAdd)
        val statusImage: ImageView = itemView.findViewById(R.id.ivStatusImage)
        val newImage: ImageView = itemView.findViewById(R.id.ivNewImage)

        private val assignedUser =
            getDrawableAttrValue(R.attr.thmManageUserRegisteredImage)
        private val notAssignedUser =
            getDrawableAttrValue(R.attr.thmManageUserNotRegisteredImage)


        //?attr/
        private fun getDrawableAttrValue(attr: Int): Drawable? {
            val attrArray = intArrayOf(attr)
            val typedArray = ctx.obtainStyledAttributes(attrArray)
            val result = try {
                typedArray.getDrawable(0)
            } catch (exc: Exception) {
                null
            }
            typedArray.recycle()
            return result
        }

        override fun allowUserAccessToMPL(accessId: Int, status: MPLAppDeviceStatus, index: Int) {

            val item = userAccessList.find { v -> v.accessId == accessId }
            item?.status = status
            item?.index = index
            userAccessList = userAccessList.filter { it.status != MPLAppDeviceStatus.REJECTED }
                .sortedBy { it.index }.toMutableList()

            notifyDataSetChanged()
        }

        override fun removeUserRequestFromMPL(accessId: Int, status: MPLAppDeviceStatus) {
            GlobalScope.launch {

                log.info("Trying to delete ${accessId} request")
                val success = WSAdmin.rejectAccessToMaster(accessId)
                withContext(Dispatchers.Main) {
                    if (success) {
                        masterAccessManage(accessId)
                    } else {
                        //App.ref.toast(R.string.app_generic_server_error)

                    }
                }
            }
        }

        override fun removeUserAccessFromMPL(masterMac: String, index: Int) {
            GlobalScope.launch {
                val success = WSAdmin.unAssignMasterFromEpaper(masterMac, index)
                withContext(Dispatchers.Main) {
                    if (success) masterUnAssignManage(index)
                    else {
                        //App.ref.toast(R.string.app_generic_server_error)
                    }
                }
            }
        }


        fun bindItem(currentItem: RMplUserAccess) {
            itemView.setOnClickListener {
                clicklistener(currentItem)

            }
            userName.text = currentItem.name

            if (currentItem.phoneNumber != "")
                phone.text =
                    ctx.getString(R.string.app_generic_phone) + ": " + currentItem.phoneNumber
            else
                phone.text = ctx.getString(R.string.app_generic_phone) + ": -"

            userEmail.text = currentItem.email

            if (currentItem.groupName != "")
                groupName.text =
                    ctx.getString(R.string.group_manage_users) + " " + currentItem.groupName
            else
                groupName.text = ctx.getString(R.string.group_manage_users) + " " + "-"

            when (currentItem.status) {
                MPLAppDeviceStatus.REJECTED -> {
                    add.visibility = View.GONE
                    delete.visibility = View.GONE
                }

                MPLAppDeviceStatus.REGISTERED -> {
                    delete.visibility = View.VISIBLE
                    delete.setOnClickListener {
                        val deleteUserFromMPL =
                            DeleteUserFromMPLDialog(
                                ctx,
                                currentItem,
                                this@UserViewHolder,
                                masterMac
                            )
                        deleteUserFromMPL.show((ctx as MainActivity).supportFragmentManager, "")
                    }
                    add.visibility = View.GONE
                    newImage.visibility = View.INVISIBLE
                    statusImage.setImageDrawable(assignedUser)
                }
                MPLAppDeviceStatus.UNREGISTERED -> {
                    setUnregisteredUi(currentItem, itemView)

                }
                MPLAppDeviceStatus.NEW -> {
                    setUnregisteredUi(currentItem, itemView)
                    newImage.visibility = View.VISIBLE
                }
                else -> {
                    log.info("UNRESOLVED STATE!!")
                }
            }


        }

        private fun setUnregisteredUi(userAccess: RMplUserAccess, itemView: View) {
            delete.visibility = View.VISIBLE
            delete.setOnClickListener {
                val deleteLockerDialog =
                    DeleteUserFromMPLDialog(
                        ctx,
                        userAccess,
                        this@UserViewHolder,
                        masterMac
                    )
                deleteLockerDialog.show((ctx as MainActivity).supportFragmentManager, "")
                //showRejectDialog(userAccess)
            }
            add.visibility = View.VISIBLE
            //add.visibility = if (masterUnitType != RMasterUnitType.MPL) View.VISIBLE else View.GONE
            add.setOnClickListener {

                if( !userAccess.isRequestsAccessAllowed ) {
                    val alreadyGrantedAccessDialog = AlreadyGrantedAccessDialog()
                    alreadyGrantedAccessDialog.show(
                        (ctx as MainActivity).supportFragmentManager,
                        "")
                }
                else {

                    if (masterUnitType == RMasterUnitType.MPL && installationType == InstalationType.DEVICE) {
                        //grantAccessRights(device)
                        val grantUserRightsDialog =
                            MPLUserSelectionDialog(
                                this@UserViewHolder,
                                userAccess,
                                userAccessList,
                                masterMac
                            )
                        grantUserRightsDialog.show(
                            (ctx as MainActivity).supportFragmentManager,
                            ""
                        )
                    } else {
                        GlobalScope.launch {
                            val success = WSAdmin.grantAccessToMaster(userAccess.accessId, 0)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    val item =
                                        userAccessList.find { v -> v.accessId == userAccess.accessId }
                                    item?.status = MPLAppDeviceStatus.REGISTERED
                                    item?.index = 0
                                    userAccessList =
                                        userAccessList.filter { it.status != MPLAppDeviceStatus.REJECTED }
                                            .sortedBy { it.index }.toMutableList()

                                    notifyDataSetChanged()
                                    //App.ref.toast(ctx.getString(R.string.successfully_updated))
                                } else

                                    log.error("Error while adding user to the tablet device.")
                                //App.ref.toast(ctx.getString(R.string.app_generic_error))
                            }
                        }
                    }
                }
            }
            statusImage.setImageDrawable(notAssignedUser)
        }

        private fun showRejectDialog(masterAccess: RMplUserAccess) {

            // Late initialize an alert dialog object
            lateinit var dialog: AlertDialog
            val builder = AlertDialog.Builder(ctx)
            val message = if (masterAccess.status == MPLAppDeviceStatus.REGISTERED)
                ctx.getString(R.string.manage_mpl_access_remove_rights) else ctx.getString(R.string.manage_mpl_access_remove_access)
            builder.setTitle(message)
            // On click listener for dialog buttons
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        GlobalScope.launch {
                            if (masterAccess.status == MPLAppDeviceStatus.UNREGISTERED || masterAccess.status == MPLAppDeviceStatus.NEW) {
                                log.info("Trying to delete ${masterAccess.accessId} request")
                                val success = WSAdmin.rejectAccessToMaster(masterAccess.accessId)
                                withContext(Dispatchers.Main) {
                                    if (success) {
                                        masterAccessManage(
                                            masterAccess.accessId
                                        )
                                    } else {
                                        //App.ref.toast(R.string.app_generic_server_error)

                                    }
                                }
                            } else {
                                val success =
                                    WSAdmin.unAssignMasterFromEpaper(masterMac, masterAccess.index)
                                withContext(Dispatchers.Main) {
                                    if (success) masterUnAssignManage(
                                        masterAccess.index
                                    )
                                    else {
                                        //App.ref.toast(R.string.app_generic_server_error)
                                    }
                                }
                            }
                        }
                    }
                    DialogInterface.BUTTON_NEGATIVE -> dialog.dismiss()
                    DialogInterface.BUTTON_NEUTRAL -> dialog.dismiss()
                }
            }

            builder.setPositiveButton(
                ctx.getString(R.string.app_generic_confirm),
                dialogClickListener
            )
            builder.setNegativeButton(
                ctx.getString(R.string.app_generic_cancel),
                dialogClickListener
            )

            // Initialize the AlertDialog using builder object
            dialog = builder.create()

            // Finally, display the alert dialog
            dialog.show()
        }

        fun masterAccessManage(accessId: Int) {
            val userAccess = userAccessList.find { v -> v.accessId == accessId }

            userAccessList.remove(userAccess)
            notifyDataSetChanged()
        }

        fun masterUnAssignManage(indexId: Int) {
            val userAccess = userAccessList.filter { it.index == indexId }.firstOrNull()
            if( userAccess != null ) {
                userAccessList.remove(userAccess)
                notifyDataSetChanged()
            }
        }


    }
}