package hr.sil.android.smartlockers.adminapp.view.adapter

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.model.InstalationType
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnitType
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.ItemHomeScreen
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.util.ListDiffer
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity

class MPLSplAdapter(
    var mplLocker: MutableList<ItemHomeScreen>,
    val clickListener: (ItemHomeScreen.Child) -> Unit,
    val activity: MainActivity
) : RecyclerView.Adapter<MPLSplAdapter.ViewHolder>() {

    val log = logger()
    private val devices: MutableList<ItemHomeScreen> = mplLocker.toMutableList()

    enum class ITEM_TYPES(val typeValue: Int) {
        ITEM_HEADER_HOME_SCREEN(0),
        ITEM_CHILD_HOME_SCREEN(1);

        companion object {
            fun from(findValue: Int): ITEM_TYPES = values().first { it.typeValue == findValue }
        }
    }

    override fun getItemViewType(position: Int): Int {

        return ITEM_TYPES.from(devices.get(position).getRecyclerviewItemType()).typeValue
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = devices[position]
        when (viewType.getRecyclerviewItemType()) {
            ITEM_TYPES.ITEM_HEADER_HOME_SCREEN.typeValue -> {

                holder as HeaderHolder
                val headerItem = devices[position] as ItemHomeScreen.Header
                holder.bindItem(headerItem)
            }
            ITEM_TYPES.ITEM_CHILD_HOME_SCREEN.typeValue -> {

                holder as MplItemViewHolder
                val childItem = devices[position] as ItemHomeScreen.Child
                holder.bindItem(childItem, clickListener)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0) {
            val itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_home_screen_header, parent, false)
            return HeaderHolder(itemView)
        } else {
            val itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_home_screen_child, parent, false)
            return MplItemViewHolder(itemView)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun updateDevices(updatedDevices: MutableList<ItemHomeScreen>) {
        val listDiff = ListDiffer.getDiff(
            devices,
            updatedDevices,
            { old, new ->
                if (old is ItemHomeScreen.Child && new is ItemHomeScreen.Child) {
                    old.mplOrSplDevice?.macAddress == new.mplOrSplDevice?.macAddress &&
                            old.mplOrSplDevice?.type == new.mplOrSplDevice?.type &&
                            old.mplOrSplDevice?.masterUnitType == new.mplOrSplDevice?.masterUnitType &&
                            old.mplOrSplDevice?.slaveUnits == new.mplOrSplDevice?.slaveUnits &&
                            old.mplOrSplDevice?.masterUnitId == new.mplOrSplDevice?.masterUnitId &&
                            old.mplOrSplDevice?.masterUnitMac == new.mplOrSplDevice?.masterUnitMac &&
                            old.mplOrSplDevice?.masterUnitName == new.mplOrSplDevice?.masterUnitName &&
                            old.mplOrSplDevice?.masterUnitAddress == new.mplOrSplDevice?.masterUnitAddress &&
                            old.mplOrSplDevice?.isInProximity == new.mplOrSplDevice?.isInProximity &&
                            old.mplOrSplDevice?.modemRssi == new.mplOrSplDevice?.modemRssi &&
                            old.mplOrSplDevice?.stmOrAppVersion == new.mplOrSplDevice?.stmOrAppVersion &&
                            old.mplOrSplDevice?.accessTypes == new.mplOrSplDevice?.accessTypes
                } else if (old is ItemHomeScreen.Header && new is ItemHomeScreen.Header) {
                    old.headerTitle == new.headerTitle
                } else
                    false
            })

        for (diff in listDiff) {
            when (diff) {
                is ListDiffer.DiffInserted -> {
                    devices.addAll(diff.elements)
                    log.info("notifyItemRangeInserted")
                    notifyItemRangeInserted(diff.position, diff.elements.size)
                }
                is ListDiffer.DiffRemoved -> {
                    //remove devices
                    for (i in (devices.size - 1) downTo diff.position) {
                        devices.removeAt(i)
                    }
                    log.info("notifyItemRangeRemoved")
                    notifyItemRangeRemoved(diff.position, diff.count)
                }
                is ListDiffer.DiffChanged -> {
                    devices[diff.position] = diff.newElement
                    log.info("notifyItemChanged")
                    notifyItemChanged(diff.position)
                }
            }
        }
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    inner class HeaderHolder(itemView: View) : ViewHolder(itemView) {

        val headerTitle: TextView = itemView.findViewById(R.id.tvTitle)

        fun bindItem(keyObject: ItemHomeScreen.Header) {
            headerTitle.text = keyObject.headerTitle
        }
    }


    inner class MplItemViewHolder(itemView: View) : ViewHolder(itemView) {
        val log = logger()
        val name: TextView = itemView.findViewById(R.id.tvLockerName)
        val address: TextView = itemView.findViewById(R.id.tvLockerAddress)
        val macAddress: TextView = itemView.findViewById(R.id.tvMacAddress)
        var arrow: ImageView = itemView.findViewById(R.id.ivArrow)

        var ivDevicePicture: ImageView = itemView.findViewById(R.id.ivDevicePicture)

        val circleValue by lazy { itemView.findViewById<TextView>(R.id.device_details_circle_value) }

        fun bindItem(
            currentItem: ItemHomeScreen.Child,
            clickListener: (ItemHomeScreen.Child) -> Unit
        ) {

            val parcelLocker = currentItem.mplOrSplDevice
            if (parcelLocker != null) {

                if( parcelLocker.macAddress == "00:E0:67:27:A7:24" ) {
                    log.info("mac address is: ${parcelLocker.macAddress}, installation type: ${parcelLocker.installationType}, ble type: ${parcelLocker.type}")
                }

                val lockerKeys =
                    parcelLocker.alarmsMessageLog.filter { it -> it.id != parcelLocker.masterUnitId }

                if (lockerKeys.size > 0) {
                    circleValue.width = 1
                    circleValue.height = 15
                    circleValue?.text = lockerKeys.size.toString()
                    circleValue?.visibility = View.VISIBLE
                } else {
                    circleValue?.visibility = View.INVISIBLE
                }

                var unavailable = false

                macAddress.text = parcelLocker.macAddress

                if ( parcelLocker.isInProximity && parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED  && parcelLocker.masterUnitId != -1  ) {
                    arrow.visibility = View.VISIBLE
                    displayMPLSPLNameAndAddress(parcelLocker)
                    val drawable: Drawable?
                    when {
                        (parcelLocker.masterUnitType == RMasterUnitType.MPL && parcelLocker.installationType == InstalationType.DEVICE) || parcelLocker.type == MPLDeviceType.MASTER -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmMPLRegisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        ( (parcelLocker.masterUnitType == RMasterUnitType.SPL_PLUS || parcelLocker.masterUnitType == RMasterUnitType.SPL) && parcelLocker.installationType == InstalationType.DEVICE)
                                || parcelLocker.type == MPLDeviceType.SPL || parcelLocker.type == MPLDeviceType.SPL_PLUS -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmSplRegisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        parcelLocker.type == MPLDeviceType.TABLET || parcelLocker.installationType == InstalationType.TABLET -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmTABLETRegisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        // this are linux devices
                        else -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmLinuxRegisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                    }
                    ivDevicePicture.setImageDrawable(drawable)
                } else if ( (parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.UNREGISTERED || parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTRATION_PENDING)  && parcelLocker.isInProximity) {
                    arrow.visibility = View.VISIBLE
                    displayMPLSPLNameAndAddress(parcelLocker)
                    /*if( parcelLocker.masterUnitId == -1 )
                        unavailable = false
                    else
                        unavailable = true*/
                    val drawable: Drawable?
                    when {
                        (parcelLocker.masterUnitType == RMasterUnitType.MPL && parcelLocker.installationType == InstalationType.DEVICE) || parcelLocker.type == MPLDeviceType.MASTER -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmMPLUnregisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        ( (parcelLocker.masterUnitType == RMasterUnitType.SPL_PLUS || parcelLocker.masterUnitType == RMasterUnitType.SPL) && parcelLocker.installationType == InstalationType.DEVICE)
                                || parcelLocker.type == MPLDeviceType.SPL || parcelLocker.type == MPLDeviceType.SPL_PLUS -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmSplUnregisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        parcelLocker.type == MPLDeviceType.TABLET || parcelLocker.installationType == InstalationType.TABLET -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmTABLETUnregisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        else -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmLinuxUnregisteredImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                    }
                    ivDevicePicture.setImageDrawable(drawable)
                } else if (!parcelLocker.isInProximity && parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED ) {
                    arrow.visibility = View.VISIBLE
                    displayMPLSPLNameAndAddress(parcelLocker)
                    val drawable: Drawable?
                    when {
                        (parcelLocker.masterUnitType == RMasterUnitType.MPL && parcelLocker.installationType == InstalationType.DEVICE) || parcelLocker.type == MPLDeviceType.MASTER -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmMPLNotInProximityImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        ( (parcelLocker.masterUnitType == RMasterUnitType.SPL_PLUS || parcelLocker.masterUnitType == RMasterUnitType.SPL) && parcelLocker.installationType == InstalationType.DEVICE)
                                || parcelLocker.type == MPLDeviceType.SPL || parcelLocker.type == MPLDeviceType.SPL_PLUS -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmSplNotInProximityImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        parcelLocker.type == MPLDeviceType.TABLET || parcelLocker.installationType == InstalationType.TABLET -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmTABLETNotInProximityImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        else -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmLinuxNotInProximityImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                    }
                    ivDevicePicture.setImageDrawable(drawable)
                } else {
                    displayMPLSPLNameAndAddress(parcelLocker)
                    unavailable = true
                    arrow.visibility = View.INVISIBLE
                    val drawable: Drawable?
                    when {
                        (parcelLocker.masterUnitType == RMasterUnitType.MPL && parcelLocker.installationType == InstalationType.DEVICE) || parcelLocker.type == MPLDeviceType.MASTER -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmMPLUnavailableImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        ( (parcelLocker.masterUnitType == RMasterUnitType.SPL_PLUS || parcelLocker.masterUnitType == RMasterUnitType.SPL) && parcelLocker.installationType == InstalationType.DEVICE)
                                || parcelLocker.type == MPLDeviceType.SPL || parcelLocker.type == MPLDeviceType.SPL_PLUS -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmSplUnavailableImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        parcelLocker.type == MPLDeviceType.TABLET || parcelLocker.installationType == InstalationType.TABLET -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmTABLETUnavailableImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                        else -> {
                            drawable = with(TypedValue()) {
                                activity.theme.resolveAttribute(R.attr.thmLinuxUnavailableImage, this, true)
                                ContextCompat.getDrawable(activity, resourceId)
                            }
                        }
                    }
                    ivDevicePicture.setImageDrawable(drawable)
                }

                itemView.setOnClickListener {
                    if (!unavailable) {
                        clickListener(currentItem)
                    }
                }
            }
        }

        private fun displayMPLSPLNameAndAddress(parcelLocker: MPLDevice) {
            if (parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.UNREGISTERED && parcelLocker.isInProximity) {
                name.visibility = View.GONE
                address.visibility = View.VISIBLE
                address.text = itemView.context.getString(R.string.app_generic_not_activated)
                /*if( parcelLocker.masterUnitId == -1 )
                    address.text = itemView.context.getString(R.string.app_generic_not_activated)
                else
                    address.text = itemView.context.getString(R.string.device_registered_on_backend_deleted_eprom)*/
            } else {
                if (parcelLocker.masterUnitName?.isNotEmpty()!! && parcelLocker.masterUnitAddress.isNotEmpty()) {
                    name.visibility = View.VISIBLE
                    address.visibility = View.VISIBLE
                    name.text = parcelLocker.masterUnitName
                    address.text = parcelLocker.masterUnitAddress
                } else if (parcelLocker.masterUnitName.isEmpty() && parcelLocker.masterUnitAddress.isNotEmpty()) {
                    name.visibility = View.GONE
                    address.visibility = View.VISIBLE
                    address.text = parcelLocker.masterUnitAddress
                } else if (parcelLocker.masterUnitName.isNotEmpty() && parcelLocker.masterUnitAddress.isEmpty()) {
                    address.visibility = View.GONE
                    name.visibility = View.VISIBLE
                    name.text = parcelLocker.masterUnitName
                } else {
                    name.visibility = View.GONE
                    address.visibility = View.GONE
                }
            }

        }
    }
}