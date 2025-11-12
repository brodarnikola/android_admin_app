package hr.sil.android.smartlockers.adminapp.view.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.cache.dto.KeyStatus
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusHandler
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusKey
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusType
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.InstalationType
import hr.sil.android.smartlockers.adminapp.core.remote.model.RActionRequired
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerKeyPurpose
import hr.sil.android.smartlockers.adminapp.core.remote.model.RRoleEnum
import hr.sil.android.smartlockers.adminapp.core.util.formatFromStringToDate
import hr.sil.android.smartlockers.adminapp.core.util.formatToViewDateTimeDefaults
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.DeleteLockerKeyInterface
import hr.sil.android.smartlockers.adminapp.data.LockerOptionModel
import hr.sil.android.smartlockers.adminapp.data.LockerP16Status
import hr.sil.android.smartlockers.adminapp.store.KeyStatusStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteKeyP16Dialog
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteLockerSlaveP16Dialog
import hr.sil.android.smartlockers.adminapp.view.dialog.LockerOptionsP16Dialog
import hr.sil.android.smartlockers.adminapp.view.dialog.P16OccupiedLockersDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext 
import java.text.ParseException
import java.util.*

class LockerOptionsP16Adapter(
    var splLockers: List<LockerOptionModel>,
    val slaveIndexP16Mac: String,
    val deviceP16Mac: String,
    val masterMacAddress: String,
    val context: Context,
    val lockerOptionsDialog: LockerOptionsP16Dialog,
    val isLockerInProximity: Boolean,
    val keyPurpose: RLockerKeyPurpose,
    val isAvailableLocker: Boolean,
    val createdForName: String,
    val createdOnDate: String,
    val isMasterUnitInProximity: Boolean,
    val deletingKeyInProgressBLE: Boolean,
    val deletingKeyInProgressBACKEND: Boolean,
    var reducedMobility: Boolean,
    val cleaningNeeded: RActionRequired,
    val activity: MainActivity,
    val peripheralAdapterP16: PeripheralAdapterP16,
    val lockerIndex: Int,
    val installationType: InstalationType?,
    val beforeAdapterPosition: Int
) :
    RecyclerView.Adapter<LockerOptionsP16Adapter.LockerKeyOptionsHolder>() {


    private val log = logger()

    override fun onBindViewHolder(holder: LockerKeyOptionsHolder, position: Int) {
        holder.bindItem(splLockers[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockerKeyOptionsHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_options_p16, parent, false)

        return LockerKeyOptionsHolder(itemView)
    }

    fun updateDevice(isAvailable: Boolean) {
        splLockers[2].isEmptyLocker = isAvailable // delete key option
        notifyItemChanged(2)
    }

    override fun getItemCount() = splLockers.size

    inner class LockerKeyOptionsHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        DeleteLockerKeyInterface {

        var lockerOptionImage: ImageView = itemView.findViewById(R.id.ivLockerImage)
        val lockerOptionDescription: TextView = itemView.findViewById(R.id.tvLockerDescription)
        val btnLockerAction: Button = itemView.findViewById(R.id.btnLockerAction)
        val progressLockerAction: ProgressBar =
            itemView.findViewById(R.id.progressLockerOptionAction)
        val clKeyData: ConstraintLayout = itemView.findViewById(R.id.clKeyData)
        val tvKeyPurpose: TextView = itemView.findViewById(R.id.tvKeyPurpose)
        val tvKeyCreatedFor: TextView = itemView.findViewById(R.id.tvKeyCreatedFor)
        val reducedMobilityCheckBox: CheckBox = itemView.findViewById(R.id.reducedMobilityCheckBox)
        val llReducedMobility: LinearLayout = itemView.findViewById(R.id.llReducedMobility)

        val cleaningNeededCheckBox: CheckBox = itemView.findViewById(R.id.cleaningNeededCheckBox)
        val llCleaningNeeded: LinearLayout = itemView.findViewById(R.id.llCleaningNeeded)

        val discoverImage = getDrawableAttrValue(R.attr.thmLockerOptionDiscoverImage)
        val forceOpenImage = getDrawableAttrValue(R.attr.thmLockerOptionForceOpenImage)
        val invalidateKeyImage = getDrawableAttrValue(R.attr.thmLockerOptionDeleteKeyImage)
        val deleteLockerImage = getDrawableAttrValue(R.attr.thmLockerOptionRemoveLockerImage)

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

        fun bindItem(parcelLocker: LockerOptionModel) {

            lockerOptionDescription.text = parcelLocker.description
            btnLockerAction.text = parcelLocker.buttonText
            if (adapterPosition == 0) {

                if( installationType == InstalationType.TABLET ) {
                    llReducedMobility.visibility = View.VISIBLE
                    llCleaningNeeded.visibility = View.VISIBLE
                    if (reducedMobility)
                        reducedMobilityCheckBox.isChecked = true
                    else
                        reducedMobilityCheckBox.isChecked = false
                    reducedMobilityCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (reducedMobility) {
                            buttonView.isChecked = false
                            peripheralAdapterP16.disableEnableReducedMobility(
                                lockerIndex,
                                beforeAdapterPosition,
                                false
                            )
                        } else {
                            buttonView.isChecked = true
                            peripheralAdapterP16.disableEnableReducedMobility(
                                lockerIndex,
                                beforeAdapterPosition,
                                true
                            )
                        }
                        lockerOptionsDialog.dismiss()
                    }

                    if (cleaningNeeded == RActionRequired.CLEANING)
                        cleaningNeededCheckBox.isChecked = true
                    else
                        cleaningNeededCheckBox.isChecked = false

                    cleaningNeededCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (cleaningNeeded == RActionRequired.CLEANING) {
                            buttonView.isChecked = false
                            peripheralAdapterP16.cleanOrDirtyLocker(
                                lockerIndex,
                                beforeAdapterPosition,
                                RActionRequired.NULL
                            )
                        } else {
                            buttonView.isChecked = true
                            peripheralAdapterP16.cleanOrDirtyLocker(
                                lockerIndex,
                                beforeAdapterPosition,
                                RActionRequired.CLEANING
                            )
                        }
                        lockerOptionsDialog.dismiss()
                    }
                }
                else {
                    llReducedMobility.visibility = View.GONE
                    llCleaningNeeded.visibility = View.GONE
                }

                clKeyData.visibility = View.GONE
                lockerOptionImage.setImageDrawable(discoverImage)
                if (isLockerInProximity && isMasterUnitInProximity) {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                } else {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                }
                discoverSetOnClickListener()
            } else if (adapterPosition == 1) {
                llReducedMobility.visibility = View.GONE
                llCleaningNeeded.visibility = View.GONE
                clKeyData.visibility = View.GONE
                lockerOptionImage.setImageDrawable(forceOpenImage)
                if (isLockerInProximity && isMasterUnitInProximity) {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                } else {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                }
                forceOpenSetOnClickListener()
            }
            // ONCE WE ENALBED DELETE KEY ON P16 LOCKER, THEN WE CAN UNCOMMENT THIS CODE HERE
            else if( adapterPosition == 2 ) {
                llReducedMobility.visibility = View.GONE
                llCleaningNeeded.visibility = View.GONE
                lockerOptionImage.setImageDrawable( invalidateKeyImage )
                if( deletingKeyInProgressBLE ) {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                    clKeyData.visibility = View.VISIBLE
                    tvKeyPurpose.text = lockerOptionsDialog.resources.getString(R.string.deleting_key_in_progress)
                }
                else if( parcelLocker.isEmptyLocker || (deletingKeyInProgressBACKEND && !isLockerInProximity) || !checkUserRole() ) {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                    clKeyData.visibility = View.GONE
                }
                else {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                    clKeyData.visibility = View.VISIBLE
                    tvKeyPurpose.text = keyPurpose.name
                    val correctCreatedOnDate = formatCorrectDate(createdOnDate)
                    tvKeyCreatedFor.text = createdForName + ", " + correctCreatedOnDate
                }
                deleteKeySetOnClickListener()
            }
            else {
                llReducedMobility.visibility = View.GONE
                llCleaningNeeded.visibility = View.GONE
                lockerOptionImage.setImageDrawable(deleteLockerImage)
                clKeyData.visibility = View.GONE
                // Once we have this options, that we can not delete locker in P16, when there is key, then I need to uncommnet this
                //if( keyPurpose == RLockerKeyPurpose.UNKNOWN && isLockerInProximity && isMasterUnitInProximity && checkUserRole() ) {
                if (isLockerInProximity && isMasterUnitInProximity && checkUserRole()) {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                    removeLockerSetOnClickListener()
                } else {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                }
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

        private fun checkUserRole(): Boolean {
            log.info("User role: ${UserUtil.user?.role?.name}")
            if (UserUtil.user?.role == RRoleEnum.SUPER_ADMIN || UserUtil.user?.role == RRoleEnum.ADMIN)
                return true
            return false
        }

        private fun removeLockerSetOnClickListener() {

            btnLockerAction.setOnClickListener {
                if (isAvailableLocker) {
                    val deleteLockerSlaveP16Dialog =
                        DeleteLockerSlaveP16Dialog(this@LockerKeyOptionsHolder)
                    deleteLockerSlaveP16Dialog.show(
                        (context as MainActivity).supportFragmentManager,
                        ""
                    )
                } else {
                    val dialog = P16OccupiedLockersDialog()
                    dialog.show(
                        (activity).supportFragmentManager,
                        ""
                    )
                }
            }
        }

        override fun deleteLocker() {
            progressLockerAction.visibility = View.VISIBLE
            btnLockerAction.visibility = View.GONE
            lockerOptionsDialog.dismiss()
            peripheralAdapterP16.onItemSelected(
                lockerIndex,
                LockerP16Status.UNREGISTERED,
                beforeAdapterPosition
            )
        }

        private fun deleteKeySetOnClickListener() {
            btnLockerAction.setOnClickListener {
                val deleteKeyDialog = DeleteKeyP16Dialog(
                    this@LockerKeyOptionsHolder,
                    isLockerInProximity,
                    isMasterUnitInProximity
                )
                deleteKeyDialog.show((context as MainActivity).supportFragmentManager, "")
            }
        }

        override fun deleteKey() {
            progressLockerAction.visibility = View.VISIBLE
            btnLockerAction.visibility = View.GONE
            GlobalScope.launch {

                if (isLockerInProximity && isMasterUnitInProximity) {
                    val communicator =
                        MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)
                    if (communicator != null && communicator.connect()) {

                        val bleResultForceOpen = communicator.forceOpenDoor(slaveIndexP16Mac)
                        log.info("Is force open successfully after the BLE COMMAND FOR invalidate(deleting) key: ${bleResultForceOpen}, for slave, locker device ${slaveIndexP16Mac}")

                        val bleResult = communicator.deleteKeyOnLocker(slaveIndexP16Mac)
                        if (bleResult) {
                            log.info("Starting to deleting locker key, on slave mac address: ${slaveIndexP16Mac}")
                            val keyStatus = KeyStatus(
                                slaveIndexP16Mac,
                                true
                            )
                            DataCache.setKeyStatus(keyStatus)
                            KeyStatusStore.run(slaveIndexP16Mac, masterMacAddress, false)
                        }

                        withContext(Dispatchers.Main) {

                            if (bleResult) {
                                log.info("Successfully delete key on locker ${deviceP16Mac} ")
                                //App.ref.toast(R.string.update_locker_data_started)
                                lockerOptionsDialog.dismiss()
                            } else {
                                progressLockerAction.visibility = View.GONE
                                btnLockerAction.visibility = View.VISIBLE
                                log.info("Not successfully delete key on locker ${deviceP16Mac} ")
                                //App.ref.toast(R.string.app_generic_error)
                            }
                        }

                    } else {
                        log.error("Error while connecting the peripheral ")
                        withContext(Dispatchers.Main) {

                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            //App.ref.toast(R.string.main_locker_ble_connection_error)
                        }
                    }
                    communicator?.disconnect()
                } else {

                    val result = WSAdmin.deleteKeyOnLocker(slaveIndexP16Mac)
                    if (result == true) {
                        DataCache.getMasterUnit(masterMacAddress, true)
                    }

                    withContext(Dispatchers.Main) {
                        if (result == true) {
                            log.info("Successfully delete key on locker ${slaveIndexP16Mac} ")
                            lockerOptionsDialog.dismiss()
                        } else {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            log.info("Not successfully delete key on locker ${slaveIndexP16Mac} ")
                            //App.ref.toast(R.string.app_generic_error)
                        }
                    }
                }
            }
        }

        private suspend fun modifyStatusChange(macAddress: String, status: ActionStatusType) {
            val statusKey = ActionStatusKey()
            statusKey.macAddress = macAddress
            statusKey.statusType = status
            statusKey.keyId = macAddress + status.name
            ActionStatusHandler.actionStatusDb.put(statusKey)
        }

        private fun forceOpenSetOnClickListener() {
            btnLockerAction.setOnClickListener {

                log.info("Force open ${slaveIndexP16Mac}")
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                progressLockerAction.visibility = View.VISIBLE
                btnLockerAction.visibility = View.GONE
                GlobalScope.launch {
                    val communicator =
                        MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)

                    if (communicator != null && communicator.connect()) {

                        val bleResult = communicator.forceOpenDoor(slaveIndexP16Mac)

                        withContext(Dispatchers.Main) {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            if (bleResult)
                                log.info("Successfully send force open operation ${masterMacAddress} slave - ${slaveIndexP16Mac} ")
                            else
                                log.info("Not successfully send force open operation ${masterMacAddress} slave - ${slaveIndexP16Mac} ")

                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            //App.ref.toast(R.string.main_locker_ble_connection_error)
                            log.error("Error while connecting the peripheral ${slaveIndexP16Mac}")
                        }
                    }
                    communicator?.disconnect()
                }
            }
        }

        private fun discoverSetOnClickListener() {
            btnLockerAction.setOnClickListener {
                log.info("Discover peripheral ${deviceP16Mac}")
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                progressLockerAction.visibility = View.VISIBLE
                btnLockerAction.visibility = View.GONE
                GlobalScope.launch {
                    val communicator =
                        MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)

                    if (communicator != null && communicator.connect()) {

                        val bleResult = communicator.discoverSlave(deviceP16Mac)
                        withContext(Dispatchers.Main) {

                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            if (bleResult)
                                log.info("Successfully send discover operation ${masterMacAddress} slave - ${deviceP16Mac} ")
                            else
                                log.info("Not successfully send discover operation ${masterMacAddress} slave - ${deviceP16Mac} ")

                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            //App.ref.toast(R.string.main_locker_ble_connection_error)
                            log.error("Error while connecting the peripheral ${deviceP16Mac}")
                        }
                    }

                    communicator?.disconnect()
                }
            }
        }
    }

}