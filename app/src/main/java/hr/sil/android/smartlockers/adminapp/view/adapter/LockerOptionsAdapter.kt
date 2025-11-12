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
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macRealToBytes
import hr.sil.android.smartlockers.adminapp.data.DeleteLockerKeyInterface
import hr.sil.android.smartlockers.adminapp.data.LockerOptionModel
import hr.sil.android.smartlockers.adminapp.store.KeyStatusStore
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteKeyDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteLockerSlaveDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.DisableUserActionsDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.LockerOptionsDialog
import hr.sil.android.util.general.extensions.hexToByteArray
import kotlinx.coroutines.* 

class LockerOptionsAdapter (var splLockers: List<LockerOptionModel>, val masterMacAddress: String,
                            val slaveMacAddress: String, val context: Context,
                            val installationType: InstalationType, val lockerOptionsDialog: LockerOptionsDialog,
                            val isLockerInProximity: Boolean, val keyPurpose: RLockerKeyPurpose,
                            val createdForName: String, val createdOnDate: String,
                            val isMasterUnitInProximity: Boolean,
                            val deletingKeyInProgressBLE: Boolean,
                            val deletingKeyInProgressBACKEND: Boolean,
                            val cleaningNeeded: RActionRequired,
                            val reducedMobility: Boolean,
                            val lockerSize: RLockerSize,
                            val activity: MainActivity
) :
    RecyclerView.Adapter<LockerOptionsAdapter.LockerKeyOptionsHolder>() {


    private val MAC_ADDRESS_7_BYTE_LENGTH = 14
    private val MAC_ADDRESS_6_BYTE_LENGTH = 12
    private val MAC_ADDRESS_LAST_BYTE_LENGTH = 2

    private val log = logger()

    override fun onBindViewHolder(holder: LockerKeyOptionsHolder, position: Int) {
        holder.bindItem(splLockers[position] )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockerKeyOptionsHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_options, parent, false)

        return LockerKeyOptionsHolder(itemView)
    }

    fun updateDevice(isAvailable: Boolean) {
        splLockers[2].isEmptyLocker = isAvailable // delete key option
        notifyItemChanged(2)
    }

    override fun getItemCount() = splLockers.size

    inner class LockerKeyOptionsHolder(itemView: View) : RecyclerView.ViewHolder(itemView), DeleteLockerKeyInterface {

        var lockerOptionImage: ImageView = itemView.findViewById(R.id.ivLockerImage)
        val lockerOptionDescription: TextView = itemView.findViewById(R.id.tvLockerDescription)
        val btnLockerAction: Button = itemView.findViewById(R.id.btnLockerAction)
        val progressLockerAction: ProgressBar = itemView.findViewById(R.id.progressLockerOptionAction)
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
            if( adapterPosition == 0 ) {

                if( installationType == InstalationType.TABLET ) {
                    llCleaningNeeded.visibility = View.VISIBLE
                    llReducedMobility.visibility = View.VISIBLE
                    cleaningNeededCheckBox.isChecked = if( cleaningNeeded == RActionRequired.CLEANING ) true else false
                    reducedMobilityCheckBox.isChecked = reducedMobility

                    when {
                        isLockerInProximity && isMasterUnitInProximity -> {
                            cleaningNeededCheckBox.isEnabled = true
                            cleaningNeededCheckBox.alpha = 1.0f
                            cleaningNeededCheckBox.setOnCheckedChangeListener { _, isChecked ->
                                isCleanOrDirtyLocker()
                            }
                        }
                        else -> {
                            cleaningNeededCheckBox.isEnabled = true
                            cleaningNeededCheckBox.alpha = 1.0f
                        }
                    }

                    when {
                        isLockerInProximity && isMasterUnitInProximity -> {
                            reducedMobilityCheckBox.isEnabled = true
                            reducedMobilityCheckBox.alpha = 1.0f
                            reducedMobilityCheckBox.setOnCheckedChangeListener { _, isChecked ->
                                enableOrDisableReducedMobility()
                            }
                        }
                        else -> {
                            reducedMobilityCheckBox.isEnabled = false
                            reducedMobilityCheckBox.alpha = 0.8f
                        }
                    }
                }
                else {
                    llReducedMobility.visibility = View.GONE
                    llCleaningNeeded.visibility = View.GONE
                }

                clKeyData.visibility = View.GONE
                lockerOptionImage.setImageDrawable(discoverImage )
                if( isLockerInProximity && isMasterUnitInProximity ) {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                }
                else {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                }
                discoverSetOnClickListener()
            }
            else if( adapterPosition == 1 ) {
                llReducedMobility.visibility = View.GONE
                llCleaningNeeded.visibility = View.GONE
                clKeyData.visibility = View.GONE
                lockerOptionImage.setImageDrawable( forceOpenImage)
                if( isLockerInProximity && isMasterUnitInProximity ) {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                }
                else {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                }
                forceOpenSetOnClickListener()
            }
            else if( adapterPosition == 2 ) {
                llReducedMobility.visibility = View.GONE
                llCleaningNeeded.visibility = View.GONE
                lockerOptionImage.setImageDrawable( invalidateKeyImage )
                if( parcelLocker.isEmptyLocker || (deletingKeyInProgressBACKEND && !isLockerInProximity) || !checkUserRole() ) {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                    clKeyData.visibility = View.GONE
                }
                else {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                    clKeyData.visibility = View.VISIBLE
                    tvKeyPurpose.text = keyPurpose.name
                    tvKeyCreatedFor.text = createdForName + ", " + createdOnDate
                }
                deleteKeySetOnClickListener()
            }
            else {
                llReducedMobility.visibility = View.GONE
                llCleaningNeeded.visibility = View.GONE
                lockerOptionImage.setImageDrawable( deleteLockerImage )
                clKeyData.visibility = View.GONE
                if( keyPurpose == RLockerKeyPurpose.UNKNOWN && isLockerInProximity && isMasterUnitInProximity && checkUserRole() ) {
                    btnLockerAction.isEnabled = true
                    btnLockerAction.alpha = 1.0f
                    removeLockerSetOnClickListener()
                }
                else {
                    btnLockerAction.isEnabled = false
                    btnLockerAction.alpha = 0.4f
                }
            }
        }

        private fun enableOrDisableReducedMobility() {
            GlobalScope.launch(Dispatchers.Main) {
                val disableUserActionDialog = DisableUserActionsDialog()
                disableUserActionDialog.isCancelable = false
                disableUserActionDialog.show(
                    activity.supportFragmentManager, ""
                )
                try {
                    withTimeout(30000) {

                        val communicator =
                            MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)

                        if (communicator != null && communicator.connect()) {

                            val lockerSlaveInfo = when {
                                // this is for mpl with new lockers with p16
                                slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                                    slaveMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH).macRealToBytes()
                                        .reversedArray().plus(slaveMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH).hexToByteArray())
                                }
                                // this is for mpl with old lockers
                                else -> {
                                    slaveMacAddress.macRealToBytes().reversedArray().plus(byteArrayOf(0x00))
                                }
                            }

                            // data to update cleaning needed or reduced mobility is:
                            // 1) first byte to tell which flag we want to update: a) 0x01 reduced, b) 0x02 cleaning, 3) 0x03 both
                            // 2) after that it is going slave mac address in reverse order
                            // 3) plus slave index ( for single locker it is 0x00 )
                            // 4) which value will be updated:
                            //      a) 0x00 -> everything is set to 0 ( unchecked ), both cleaning and reduced mobility
                            //      b) 0x01 -> only reduced mobility is set to true
                            //      c) 0x02 -> only cleaning needed is set to true
                            //      d) 0x03 -> cleaning neeeded and reduced mobility are set to true

                            // it is best to use first flag as 0x03 .. and after that in the last flag, just to tell which value we want to update

                            val lastByte = when {
                                !reducedMobilityCheckBox.isChecked && !cleaningNeededCheckBox.isChecked -> byteArrayOf(0x00)
                                reducedMobilityCheckBox.isChecked && !cleaningNeededCheckBox.isChecked -> byteArrayOf(0x01)
                                !reducedMobilityCheckBox.isChecked && cleaningNeededCheckBox.isChecked -> byteArrayOf(0x02)
                                else -> byteArrayOf(0x03)
                            }

                            val byteArrayCleaningNeeded = byteArrayOf(0x03) + lockerSlaveInfo + lastByte

                            val response = communicator.lockerCleanOrDirty(byteArrayCleaningNeeded)

                            communicator.disconnect()
                            withContext(Dispatchers.Main) {
                                if (response)
                                    ////App.ref.toast(activity.resources.getString(R.string.update_locker_data_started))
                                else
                                    ////App.ref.toast(activity.resources.getString(R.string.app_generic_error))
                                disableUserActionDialog.dismiss()
                            }
                        }
                        else {
                            ////App.ref.toast(activity.getString(R.string.main_locker_ble_connection_error))
                            disableUserActionDialog.dismiss()
                        }
                    }
                }
                catch (e: TimeoutCancellationException) {
                    log.info("TimeOutCalcelException error: ${e}")
                    disableUserActionDialog.dismiss()
                    ////App.ref.toast(activity.getString(R.string.app_generic_error))
                }
            }
        }

        private fun isCleanOrDirtyLocker() {
            GlobalScope.launch(Dispatchers.Main) {
                val disableUserActionDialog = DisableUserActionsDialog()
                disableUserActionDialog.isCancelable = false
                disableUserActionDialog.show(
                    activity.supportFragmentManager, ""
                )
                try {
                    withTimeout(30000) {

                        val communicator =
                            MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)

                        if (communicator != null && communicator.connect()) {

                            val lockerSlaveInfo = when {
                                // this is for mpl with new lockers with p16
                                slaveMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                                    slaveMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH).macRealToBytes()
                                        .reversedArray().plus(slaveMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH).hexToByteArray())
                                }
                                // this is for mpl with old lockers
                                else -> {
                                    slaveMacAddress.macRealToBytes().reversedArray().plus(byteArrayOf(0x00))
                                }
                            }

                            // data to update cleaning needed or reduced mobility is:
                            // 1) first byte to tell which flag we want to update: a) 0x01 reduced, b) 0x02 cleaning, 3) 0x03 both
                            // 2) after that it is going slave mac address in reverse order
                            // 3) plus slave index ( for single locker it is 0x00 )
                            // 4) which value will be updated:
                            //      a) 0x00 -> everything is set to 0 ( unchecked ), both cleaning and reduced mobility
                            //      b) 0x01 -> only reduced mobility is set to true
                            //      c) 0x02 -> only cleaning needed is set to true
                            //      d) 0x03 -> cleaning neeeded and reduced mobility are set to true

                            // it is best to use first flag as 0x03 .. and after that in the last flag, just to tell which value we want to update

                            val lastByte = when {
                                !reducedMobilityCheckBox.isChecked && !cleaningNeededCheckBox.isChecked -> byteArrayOf(0x00)
                                reducedMobilityCheckBox.isChecked && !cleaningNeededCheckBox.isChecked -> byteArrayOf(0x01)
                                !reducedMobilityCheckBox.isChecked && cleaningNeededCheckBox.isChecked -> byteArrayOf(0x02)
                                else -> byteArrayOf(0x03)
                            }

                            val byteArrayCleaningNeeded = byteArrayOf(0x03) + lockerSlaveInfo + lastByte

                            val response = communicator.lockerCleanOrDirty(byteArrayCleaningNeeded)

                            communicator.disconnect()

                            log.info("Cleaning function is successfully: ${response}")

                            withContext(Dispatchers.Main) {
                                if (response) {
                                    //App.ref.toast(activity.resources.getString(R.string.update_locker_data_started))
                                } else {
                                    //App.ref.toast(activity.resources.getString(R.string.app_generic_error))
                                }
                                disableUserActionDialog.dismiss()
                            }
                        }
                        else {
                            //App.ref.toast(activity.getString(R.string.main_locker_ble_connection_error))
                            disableUserActionDialog.dismiss()
                        }
                    }
                }
                catch (e: TimeoutCancellationException) {
                    log.info("TimeOutCalcelException error: ${e}")
                    disableUserActionDialog.dismiss()
                    //App.ref.toast(activity.getString(R.string.app_generic_error))
                }
            }
        }

        private fun checkUserRole(): Boolean {
            log.info("User role: ${UserUtil.user?.role?.name}")
            if (UserUtil.user?.role == RRoleEnum.SUPER_ADMIN || UserUtil.user?.role == RRoleEnum.ADMIN)
                return true
            return false
        }

        private fun deleteKeySetOnClickListener() {
            btnLockerAction.setOnClickListener {
                val deleteKeyDialog = DeleteKeyDialog(
                    this@LockerKeyOptionsHolder,
                    isLockerInProximity,
                    isMasterUnitInProximity
                )
                deleteKeyDialog.show((context as MainActivity).supportFragmentManager, "")
            }
        }

        private fun removeLockerSetOnClickListener() {
            btnLockerAction.setOnClickListener {
                val deleteLockerSlaveDialog =
                    DeleteLockerSlaveDialog(this@LockerKeyOptionsHolder)
                deleteLockerSlaveDialog.show((context as MainActivity).supportFragmentManager, "")
            }
        }

        override fun deleteLocker() {
            progressLockerAction.visibility = View.VISIBLE
            btnLockerAction.visibility = View.GONE
            GlobalScope.launch {
                val communicator =
                    MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)
                if (communicator != null && communicator.connect()) {

                    val bleResult = communicator.deregisterSlave(slaveMacAddress)
                    if( bleResult ) {
                        modifyStatusChange(
                            slaveMacAddress,
                            ActionStatusType.PERIPHERAL_DEREGISTRATION
                        )
                    }

                    withContext(Dispatchers.Main) {

                        if (bleResult) {
                            log.info("Successfully deregister slave device ${slaveMacAddress} ")
                            lockerOptionsDialog.dismiss()
                        } else {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            log.info("Not successfully deregister slave device ${slaveMacAddress} ")
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
            }
        }

        override fun deleteKey() {
            progressLockerAction.visibility = View.VISIBLE
            btnLockerAction.visibility = View.GONE
            GlobalScope.launch {

                if( isLockerInProximity && isMasterUnitInProximity ) {
                    val communicator =
                        MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)
                    if (communicator != null && communicator.connect()) {

                        val bleResultForceOpen = communicator.forceOpenDoor(slaveMacAddress)
                        log.info("Is force open successfully after the BLE COMMAND FOR invalidate(deleting) key: ${bleResultForceOpen}, for slave, locker device ${slaveMacAddress}")

                        val bleResult = communicator.deleteKeyOnLocker(slaveMacAddress)
                        if( bleResult ) {
                            log.info("Starting to deleting locker key, on slave mac address: ${slaveMacAddress}")
                            val keyStatus = KeyStatus(
                                slaveMacAddress,
                                true
                            )
                            DataCache.setKeyStatus(keyStatus)
                            KeyStatusStore.run(slaveMacAddress, masterMacAddress,false)
                        }

                        withContext(Dispatchers.Main) {

                            if (bleResult) {
                                log.info("Successfully delete key on locker ${slaveMacAddress} ")
                                //App.ref.toast( itemView.resources.getString(R.string.update_locker_data_started))
                                lockerOptionsDialog.dismiss()
                            } else {
                                progressLockerAction.visibility = View.GONE
                                btnLockerAction.visibility = View.VISIBLE
                                log.info("Not successfully delete key on locker ${slaveMacAddress} ")
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
                }
                else {

                    val result = WSAdmin.deleteKeyOnLocker(slaveMacAddress)
                    if(result == true) {
                        DataCache.getMasterUnit(masterMacAddress, true)
                    }

                    withContext(Dispatchers.Main) {
                        if (result == true) {
                            log.info("Successfully delete key on locker ${slaveMacAddress} ")
                            lockerOptionsDialog.dismiss()
                        } else {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            log.info("Not successfully delete key on locker ${slaveMacAddress} ")
                            //App.ref.toast(R.string.app_generic_error)
                        }
                    }
                }
            }
        }

        private suspend fun modifyStatusChange( macAddress: String, status: ActionStatusType) {
            val statusKey = ActionStatusKey()
            statusKey.macAddress = macAddress
            statusKey.statusType = status
            statusKey.keyId = macAddress + status.name
            ActionStatusHandler.actionStatusDb.put(statusKey)
        }

        private fun forceOpenSetOnClickListener() {
            btnLockerAction.setOnClickListener {

                log.info("Force open ${slaveMacAddress}")
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                progressLockerAction.visibility = View.VISIBLE
                btnLockerAction.visibility = View.GONE
                GlobalScope.launch {
                    val communicator = MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)

                    if (communicator != null && communicator.connect()) {

                        val bleResult = communicator.forceOpenDoor(slaveMacAddress)

                        withContext(Dispatchers.Main) {

                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            if( bleResult )
                                log.info("Successfully send force open operation ${masterMacAddress} slave - ${slaveMacAddress} ")
                            else
                                log.info("Not successfully send force open operation ${masterMacAddress} slave - ${slaveMacAddress} ")

                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            //App.ref.toast(R.string.main_locker_ble_connection_error)
                            log.error("Error while connecting the peripheral ${slaveMacAddress}")
                        }
                    }
                    communicator?.disconnect()
                }
            }
        }

        private fun discoverSetOnClickListener() {
            btnLockerAction.setOnClickListener {
                log.info("Discover peripheral ${slaveMacAddress}")
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                progressLockerAction.visibility = View.VISIBLE
                btnLockerAction.visibility = View.GONE
                GlobalScope.launch {
                    val communicator = MPLDeviceStore.devices[masterMacAddress]?.createBLECommunicator(itemView.context)

                    if (communicator != null && communicator.connect()) {

                        val bleResult = communicator.discoverSlave(slaveMacAddress)
                        withContext(Dispatchers.Main) {

                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            if( bleResult )
                                log.info("Successfully send discover operation ${masterMacAddress} slave - ${slaveMacAddress} ")
                            else
                                log.info("Not successfully send discover operation ${masterMacAddress} slave - ${slaveMacAddress} ")

                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            progressLockerAction.visibility = View.GONE
                            btnLockerAction.visibility = View.VISIBLE
                            //App.ref.toast(R.string.main_locker_ble_connection_error)
                            log.error("Error while connecting the peripheral ${slaveMacAddress}")
                        }
                    }

                    communicator?.disconnect()
                }
            }
        }
    }

}