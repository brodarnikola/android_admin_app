package hr.sil.android.smartlockers.adminapp.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerKeyPurpose
import hr.sil.android.smartlockers.adminapp.core.util.formatFromStringToDate
import hr.sil.android.smartlockers.adminapp.core.util.formatToViewDateTimeDefaults
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.LockerP16Status
import hr.sil.android.smartlockers.adminapp.data.RLockerLinuxDataUiModel
import hr.sil.android.smartlockers.adminapp.util.ListDiffer
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.LinuxConfirmationActionsDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.main.ManagePeripheralsLinuxFragment
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PeripheralAdapterLinux(
    val managePeripheralsLinuxFragment: ManagePeripheralsLinuxFragment,
    peripherals: List<RLockerLinuxDataUiModel>, val masterMac: String,
    val ctx: Context, val activity: MainActivity,
    val deviceLatitude: Double?,
    val deviceLongitude: Double?
) : RecyclerView.Adapter<PeripheralAdapterLinux.PeripheralItemViewHolder>() {

    val log = logger()

    private val devices: MutableList<RLockerLinuxDataUiModel> = peripherals.toMutableList()

    override fun onBindViewHolder(holder: PeripheralItemViewHolder, position: Int) {
        holder.bindItem(devices[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeripheralItemViewHolder {

        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_peripherals_linux, parent, false)
        return PeripheralItemViewHolder(itemView)
    }

    override fun getItemCount() = devices.size

    fun updateDevices(updatedDevices: List<RLockerLinuxDataUiModel>) {
        val listDiff = ListDiffer.getDiff(
            devices,
            updatedDevices,
            { old, new ->
                old == new &&
                        old.doorStatus == new.doorStatus
            })

        for (diff in listDiff) {
            when (diff) {
                is ListDiffer.DiffInserted -> {
                    devices.addAll(diff.elements)
                    notifyItemRangeInserted(diff.position, diff.elements.size)
                }
                is ListDiffer.DiffRemoved -> {
                    //remove devices
                    for (i in (devices.size - 1) downTo diff.position) {
                        devices.removeAt(i)
                    }
                    notifyItemRangeRemoved(diff.position, diff.count)
                }
                is ListDiffer.DiffChanged -> {
                    devices[diff.position] = diff.newElement
                    notifyItemChanged(diff.position)
                }
            }
        }
    }

    inner class PeripheralItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val log = logger()

        val DOORS_ARE_OPENED = 1
        // start side of the screen ( of the viewholder )
        val llRegisteredButtons: LinearLayout = itemView.findViewById(R.id.llRegisteredButtons)

        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnForceOpen: Button = itemView.findViewById(R.id.btnForceOpen)

        val llLockerIndex: LinearLayout = itemView.findViewById(R.id.llLockerIndex)
        val keyPurposeLayout: LinearLayout = itemView.findViewById(R.id.llKeyPurpose)
        val keyCreatedForLayout: LinearLayout = itemView.findViewById(R.id.llKeyCreatedFor)
        val keyCreatedDate: LinearLayout = itemView.findViewById(R.id.llKeyCreatedDate)
        val keyCreatedFromLayout: LinearLayout = itemView.findViewById(R.id.llKeyCreatedFrom)

        val tvTypeValue: TextView = itemView.findViewById(R.id.tvTypeValue)
        val tvLockerIndex: TextView = itemView.findViewById(R.id.tvLockerIndex)
        val tvLockerIndexValue: TextView = itemView.findViewById(R.id.tvLockerIndexValue)
        val tvSlaveMacAddressValue: TextView = itemView.findViewById(R.id.tvSlaveMacAddressValue)
        val tvKeyPurposeValue: TextView = itemView.findViewById(R.id.tvKeyPurposeValue)
        val tvKeyCreatedFromValue: TextView = itemView.findViewById(R.id.tvKeyCreatedFromValue)
        val tvKeyCreatedForValue: TextView = itemView.findViewById(R.id.tvKeyCreatedForValue)
        val tvKeyCreatedDateValue: TextView = itemView.findViewById(R.id.tvKeyCreatedDateValue)

        // end side of the screen ( of the viewholder )
        val size: TextView = itemView.findViewById(R.id.tvLockerSize)
        val lockerSlaveImage: ImageView = itemView.findViewById(R.id.ivLockerSlaveImage)
        val tvLockerStatus: TextView = itemView.findViewById(R.id.tvLockerStatus)
        val clEnd: ConstraintLayout = itemView.findViewById(R.id.clEnd)

        val ivLockerInvalidateKey: ImageView = itemView.findViewById(R.id.ivLockerInvalidateKey)
        val tvLockerInvalidateKey: TextView = itemView.findViewById(R.id.tvLockerInvalidateKey)

        val ivReducedMobility: ImageView = itemView.findViewById(R.id.ivReducedMobility)
        val ivCleaningImages: ImageView = itemView.findViewById(R.id.ivCleaningImages)

        private val slaveLinuxEmpty = getDrawableAttrValue(R.attr.thmSlaveLinuxEmpty)
        private val slaveLinuxOccupied = getDrawableAttrValue(R.attr.thmSlaveLinuxOccupied)
        private val slaveLinuxDoorOpened = getDrawableAttrValue(R.attr.slaveLinuxDoorOpened)

        private val slaveLinuxEmptyBackgroundColor = getDrawableAttrValue(R.attr.thmSlaveLinuxEmptyBackgroundColor)
        private val slaveLinuxOccupiedBackgroundColor = getDrawableAttrValue(R.attr.thmSlaveLinuxOccupiedBackgroundColor)

        //?attr/
        private fun getDrawableAttrValue(attr: Int): Drawable? {
            val attrArray = intArrayOf(attr)
            val typedArray = activity.obtainStyledAttributes(attrArray)
            val result = try {
                typedArray.getDrawable(0)
            } catch (exc: Exception) {
                null
            }
            typedArray.recycle()
            return result
        }

        fun bindItem(device: RLockerLinuxDataUiModel) {

            tvSlaveMacAddressValue.text = device.slaveMac
            tvTypeValue.text = "" + device.deviceType
            size.text = "" + device.size

            llLockerIndex.visibility = View.VISIBLE
            tvLockerIndexValue.text = device.slaveIndex

            // doors are opened
            if( device?.timeDoorOpen != null && device?.timeDoorClosed == null ) {
                lockerSlaveImage.setImageDrawable(slaveLinuxDoorOpened)
                clEnd.background = slaveLinuxEmptyBackgroundColor
                tvLockerStatus.text = itemView.resources.getString(R.string.locker_slave_unlocked)
            }
            // doors are opened
            else if( compareTwoDates(device.timeDoorOpen, device.timeDoorClosed) == DOORS_ARE_OPENED ) {
                lockerSlaveImage.setImageDrawable(slaveLinuxDoorOpened)
                clEnd.background = slaveLinuxEmptyBackgroundColor
                tvLockerStatus.text = itemView.resources.getString(R.string.locker_slave_unlocked)
            }
            else if( device.lockerStatus == LockerP16Status.EMPTY ) {
                lockerSlaveImage.setImageDrawable(slaveLinuxEmpty)
                clEnd.background = slaveLinuxEmptyBackgroundColor
                tvLockerStatus.text = itemView.resources.getString(R.string.locker_empty)
            }
            else {
                lockerSlaveImage.setImageDrawable(slaveLinuxOccupied)
                clEnd.background = slaveLinuxOccupiedBackgroundColor
                tvLockerStatus.text = itemView.resources.getString(R.string.locker_occupied)
            }


            if( !device.isReducedMobility && device.cleaningNeeded != RActionRequired.CLEANING ) {
                ivReducedMobility.alpha = 0.4f
                ivCleaningImages.alpha = 0.4f
            }
            else {
                val reducedMobilityAlpha = if( !device.isReducedMobility ) 0.4f else 1.0f
                val cleaningNeededAlpha = if( device.cleaningNeeded != RActionRequired.CLEANING ) 0.4f else 1.0f
                ivReducedMobility.alpha = reducedMobilityAlpha
                ivCleaningImages.alpha = cleaningNeededAlpha
            }

            if( device.keyPurpose != RLockerKeyPurpose.UNKNOWN && !device.deletingKeyInProgress && !device.requestToDeletingKeyOnBackend ) {
                ivLockerInvalidateKey.visibility = View.VISIBLE
                tvLockerInvalidateKey.visibility = View.VISIBLE
                keyPurposeLayout.visibility = View.VISIBLE
                keyCreatedForLayout.visibility = View.VISIBLE
                keyCreatedFromLayout.visibility = View.VISIBLE
                keyCreatedDate.visibility = View.VISIBLE
                device.createdOnDate = formatCorrectDate(device.createdOnDate)
                tvKeyPurposeValue.text = device.keyPurpose.toString()
                tvKeyCreatedFromValue.text = device.createdByName
                tvKeyCreatedForValue.text = device.createdFor
                tvKeyCreatedDateValue.text = device.createdOnDate
            }
            else if(  device.keyPurpose != RLockerKeyPurpose.UNKNOWN && (device.deletingKeyInProgress || device.requestToDeletingKeyOnBackend) ) {
                if( device.deletingKeyInProgress ) {
                    ivLockerInvalidateKey.visibility = View.GONE
                    tvLockerInvalidateKey.visibility = View.GONE
                    keyPurposeLayout.visibility = View.GONE
                    keyCreatedDate.visibility = View.GONE
                    keyCreatedForLayout.visibility = View.GONE
                    keyCreatedFromLayout.visibility = View.GONE
                }
                else {
                    ivLockerInvalidateKey.visibility = View.VISIBLE
                    tvLockerInvalidateKey.visibility = View.VISIBLE
                    keyCreatedForLayout.visibility = View.VISIBLE
                    keyCreatedFromLayout.visibility = View.VISIBLE
                    keyPurposeLayout.visibility = View.VISIBLE
                    keyCreatedDate.visibility = View.VISIBLE
                    tvKeyPurposeValue.text = device.keyPurpose.toString()
                    keyCreatedForLayout.visibility = View.VISIBLE
                    device.createdOnDate = formatCorrectDate(device.createdOnDate)
                    tvKeyCreatedDateValue.text = device.createdOnDate
                    tvKeyCreatedFromValue.text = device.createdByName
                    tvKeyCreatedForValue.text = device.createdFor
                }
            }
            else {
                ivLockerInvalidateKey.visibility = View.GONE
                tvLockerInvalidateKey.visibility = View.GONE
                keyCreatedFromLayout.visibility = View.GONE
                keyPurposeLayout.visibility = View.GONE
                keyCreatedDate.visibility = View.GONE
                keyCreatedForLayout.visibility = View.GONE
            }

            btnForceOpen.setOnClickListener {
                val linuxDialog = LinuxConfirmationActionsDialog(device.slaveMac, "forceOpen", deviceLatitude, deviceLongitude)
                linuxDialog.show((ctx as MainActivity).supportFragmentManager, "")
            }

            btnEdit.setOnClickListener {

                val bundle = bundleOf(
                    "lockerId" to device.id,
                    "deviceLatitude" to deviceLatitude,
                    "deviceLongitude" to deviceLongitude,
                    "deviceType" to device.deviceType.name,
                    "masterMac" to device.masterMac,
                    "lockerMac" to device.slaveMac,
                    "lockerMacIndex" to device.slaveIndex,
                    "keyPurpose" to device.keyPurpose.name,
                    "keyFrom" to device.createdByName,
                    "keyFor" to device.createdFor,
                    "keyDate" to device.createdOnDate,
                    "lockerSize" to device.size.name,
                    "needsCleaning" to device.cleaningNeeded.name,
                    "reducedMobility" to device.isReducedMobility
                )
                log.info("Sended masterMacAddress to manage userAccessList is: " + bundle + " to String: " + bundle.toString())
                findNavController(itemView).navigate(
                    R.id.manage_peripherals_to_peripherals_action_fragment,
                    bundle
                )
            }
        }

        private fun compareTwoDates(timeDoorOpen: String?, timeDoorClosed: String?)  : Int {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val doorOpenDate: Date = sdf.parse(timeDoorOpen)
                val doorCloseDate: Date = sdf.parse(timeDoorClosed)
                return doorOpenDate.compareTo(doorCloseDate)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return 0
            }
        }

        private fun formatCorrectDate(createdOnDate: String): String {
            val fromStringToDate: Date
            var fromDateToString = ""
            try {
                fromStringToDate = createdOnDate.formatFromStringToDate()
                fromDateToString = fromStringToDate.formatToViewDateTimeDefaults()
            }
            catch (e: ParseException) {
                e.printStackTrace()
            }
            log.info("Correct date is: ${fromDateToString}")
            return fromDateToString
        }
    }

}
