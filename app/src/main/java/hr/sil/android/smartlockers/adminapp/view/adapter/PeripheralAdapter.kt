package hr.sil.android.smartlockers.adminapp.view.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusHandler
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusKey
import hr.sil.android.smartlockers.adminapp.cache.status.ActionStatusType
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.formatFromStringToDate
import hr.sil.android.smartlockers.adminapp.core.util.formatToViewDateTimeDefaults
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.AddLockerInterface
import hr.sil.android.smartlockers.adminapp.data.MPLAppDeviceStatus
import hr.sil.android.smartlockers.adminapp.data.RLockerDataUiModel
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.util.ListDiffer
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.LockerOptionsDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.LockerSlaveSizeDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.WrongLockerVersionDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class PeripheralAdapter (peripherals: List<RLockerDataUiModel>, val masterMac: String,
                         val ctx: Context, val activity: MainActivity,
                         val masterUnitType: RMasterUnitType, val installationType: InstalationType,
                         val stmOrAppVersion: String) : RecyclerView.Adapter<PeripheralAdapter.PeripheralItemViewHolder>(), AddLockerInterface {

    val log = logger()

    override fun addSlaveSizeDialog(itemView: View, slaveMacAddress: String, selectedLockerSize: RLockerSize, reducedMobility: Boolean) {
        PeripheralItemViewHolder(itemView).itemPeripheralSizePopup(slaveMacAddress, selectedLockerSize, reducedMobility)
    }
    /*
    override fun deleteLockerSlaveDialog(itemView: View, slaveMacAddress: String) {
        PeripheralItemViewHolder(itemView).deleteItemPeripheral(ctx, slaveMacAddress)
    }*/


    private val devices: MutableList<RLockerDataUiModel> = peripherals.toMutableList()

    override fun onBindViewHolder(holder: PeripheralItemViewHolder, position: Int) {
        holder.bindItem(devices[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeripheralItemViewHolder {

        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_peripherals, parent, false)
        return PeripheralItemViewHolder(itemView)
    }


    override fun getItemCount() = devices.size


    fun getItemAt(position: Int) = if (positionValid(position)) devices[position] else null


    private fun positionValid(position: Int) =
        position >= 0 && position < devices.size

    fun updateDevices(updatedDevices: List<RLockerDataUiModel>) {
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
        val name: TextView = itemView.findViewById(R.id.tvSlaveMacAddress)
        //val delete: ImageButton = itemView.findViewById(R.id.ivSlaveDelete)
        val add: ImageView = itemView.findViewById(R.id.ivSlaveAddSettings)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val lockerSlaveImage: ImageView = itemView.findViewById(R.id.ivLockerSlaveImage)

        val ivReducedMobility: ImageView = itemView.findViewById(R.id.ivReducedMobility)
        val ivCleaningImages: ImageView = itemView.findViewById(R.id.ivCleaningImages)
        val clReducedMobilityCleaningImages: ConstraintLayout = itemView.findViewById(R.id.clReducedMobilityCleaningImages)

        val tvKeyPurposeValue: TextView = itemView.findViewById(R.id.tvKeyPurposeValue)
        val keyPurposeLayout: ConstraintLayout = itemView.findViewById(R.id.clKeyPurpose)
        val tvKeyCreatedForNameDate: TextView = itemView.findViewById(R.id.tvKeyCreatedForNameDate)
        val keyCreatedForLayout: ConstraintLayout = itemView.findViewById(R.id.clCreatedFor)

        val clDeletingKey: ConstraintLayout = itemView.findViewById(R.id.clDeletingKey)

        val size: TextView = itemView.findViewById(R.id.tvSlaveSize)
        val proximityStatus: TextView = itemView.findViewById(R.id.tvSlaveInProximity)
        val registrationStatus: TextView = itemView.findViewById(R.id.tvSlaveStatus)

        val clDoorStatus: ConstraintLayout = itemView.findViewById(R.id.clDoorStatus)
        val tvLockerDoor: TextView = itemView.findViewById(R.id.tvLockerDoor)

        val isConnecting: AtomicBoolean = AtomicBoolean(false)

        private val registeredNormal =
            getDrawableAttrValue(R.attr.thmSlaveNormalImageRegistered)
        private val registeredP16 =
            getDrawableAttrValue(R.attr.thmSlaveP16ImageRegistered)
        private val unregisteredNormal =
            getDrawableAttrValue(R.attr.thmSlaveNormalImageUnregistered)
        private val unregisteredP16 = getDrawableAttrValue(R.attr.thmSlaveP16ImageUnregistered)

        private val addNewLocker = getDrawableAttrValue(R.attr.thmSlaveAddImage)
        private val registeredLockerP16 = getDrawableAttrValue(R.attr.thmSlaveP16EditImage)
        private val registeredLockerNormal = getDrawableAttrValue(R.attr.thmSlaveNormalEditImage)

        val lockerMacAddressDoorOpen = getColorAttrValue(R.attr.thmLockerDoorOpenTextColor)
        val lockerMacAddressDoorClosed = getColorAttrValue(R.attr.thmDescriptionTextColor)

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

        private fun getColorAttrValue(attr: Int): Int {
            val attrArray = intArrayOf(attr)
            val typedArray =
                activity?.obtainStyledAttributes(attrArray)
            val result = typedArray?.getColor(
                0,
                Color.WHITE
            ) //try { typedArray.getColorOrThrow(0) } catch (exc: Exception) { null } ?: 0
            typedArray?.recycle()
            return result ?: 0
        }

        fun bindItem(device: RLockerDataUiModel) {

            name.text = itemView.context.getString(R.string.manage_peripherals_title, device.mac)
            if (!isConnecting.get()) {


                proximityStatus.text = if (device.isLockerInProximity) itemView.context.getString(R.string.manage_peripherals_inproximity) else itemView.context.getString(R.string.manage_peripherals_not_inproximity)

                when (device.status) {
                    MPLAppDeviceStatus.UNREGISTERED -> {
                        keyPurposeLayout.visibility = View.GONE
                        keyCreatedForLayout.visibility = View.GONE
                        clDeletingKey.visibility = View.GONE
                        add.visibility = View.VISIBLE
                        add.setImageDrawable(addNewLocker)
                        progressBar.visibility = View.GONE
                        registrationStatus.text = itemView.context.getString(R.string.manage_peripherals_unregistered)
                        ivCleaningImages.visibility = View.GONE
                        ivReducedMobility.visibility = View.GONE
                        clReducedMobilityCleaningImages.visibility = View.GONE

                        when( device.deviceType ) {
                            RLockerType.P16 -> {
                                name.setTextColor(lockerMacAddressDoorClosed)
                                size.text = itemView.context.getString(R.string.app_generic_size, "-")
                                lockerSlaveImage.setImageDrawable( unregisteredP16 )
                                clDoorStatus.visibility = View.GONE
                                add.setOnClickListener {
                                    val cleanLockerVersion =
                                        if (device.lockerSingleOrP16Version.contains("-")) {
                                            device.lockerSingleOrP16Version.split("-")[0]
                                        } else {
                                            device.lockerSingleOrP16Version
                                        }
                                    val bundle = bundleOf("slaveMac" to device.mac, "masterMac" to masterMac, "stmOrAppVersion" to stmOrAppVersion,
                                        "lockerVersion" to cleanLockerVersion, "isRegistered" to false)
                                    log.info("Device type is: " + device.deviceType)
                                    log.info("Sended masterMacAddress to peripheralsP16 fragment is: " + bundle + " to String: " + bundle.toString())
                                    findNavController(itemView).navigate(
                                        R.id.manage_peripherals_to_p16_lockers_fragment,
                                        bundle
                                    )
                                }
                            }
                            RLockerType.NORMAL -> {
                                size.text = itemView.context.getString(R.string.app_generic_size, "-")
                                lockerSlaveImage.setImageDrawable( unregisteredNormal )
                                if( device.doorStatus.size == 1 ) {
                                    if( device.doorStatus[0] == 0 ) {
                                        name.setTextColor(lockerMacAddressDoorClosed)
                                        clDoorStatus.visibility = View.GONE
                                    }
                                    else {
                                        name.setTextColor(lockerMacAddressDoorOpen)
                                        clDoorStatus.visibility = View.VISIBLE
                                    }
                                }
                                else {
                                    name.setTextColor(lockerMacAddressDoorClosed)
                                    clDoorStatus.visibility = View.GONE
                                }

                                add.setOnClickListener {
                                    val masterVersion =  if( masterUnitType == RMasterUnitType.MPL && installationType == InstalationType.DEVICE ) "3.0.0" else "3.1.0"
                                    val lockerVersion =  if( masterUnitType == RMasterUnitType.MPL && installationType == InstalationType.DEVICE ) "2.0.0" else "2.0.0"
                                    checkIfVersionOfLockerAndMasterAreCompatible(device, masterVersion, lockerVersion)
                                }
                            }
                            RLockerType.CLOUD -> {}
                        }

                    }
                    MPLAppDeviceStatus.REGISTERED -> {
                        add.visibility = View.VISIBLE
                        //delete.visibility = View.VISIBLE
                        registrationStatus.text = itemView.context.getString(R.string.manage_peripherals_registered)
                        progressBar.visibility = View.GONE

                        when( device.deviceType ) {
                            RLockerType.P16 -> {
                                clDoorStatus.visibility = View.GONE
                                keyPurposeLayout.visibility = View.GONE
                                keyCreatedForLayout.visibility = View.GONE
                                clDeletingKey.visibility = View.GONE
                                add.setImageDrawable(registeredLockerP16)
                                size.text = itemView.context.getString(R.string.app_generic_size, "-")
                                lockerSlaveImage.setImageDrawable( registeredP16 )
                                ivCleaningImages.visibility = View.GONE
                                ivReducedMobility.visibility = View.GONE
                                clReducedMobilityCleaningImages.visibility = View.GONE
                                name.setTextColor(lockerMacAddressDoorClosed)
                                //delete.setImageDrawable(lockerEditImage)
                                add.setOnClickListener {
                                    val isThisMPLDevice = if( masterUnitType == RMasterUnitType.MPL && installationType == InstalationType.DEVICE ) true else false
                                    val cleanLockerVersion =
                                        if (device.lockerSingleOrP16Version.contains("-")) {
                                            device.lockerSingleOrP16Version.split("-")[0]
                                        } else {
                                            device.lockerSingleOrP16Version
                                        }
                                    val bundle = bundleOf("slaveMac" to device.mac, "masterMac" to masterMac, "stmOrAppVersion" to stmOrAppVersion,
                                        "lockerVersion" to cleanLockerVersion, "isThisMPLDevice" to isThisMPLDevice, "isRegistered" to true)
                                    log.info("Device type is: " + device.deviceType)
                                    findNavController(itemView).navigate(
                                        R.id.manage_peripherals_to_p16_lockers_fragment,
                                        bundle
                                    )

                                }
                            }
                            RLockerType.NORMAL -> {
                                if( device.doorStatus.size == 1 ) {
                                    if( device.doorStatus[0] == 0 ) {
                                        name.setTextColor(lockerMacAddressDoorClosed)
                                        clDoorStatus.visibility = View.GONE
                                    }
                                    else {
                                        name.setTextColor(lockerMacAddressDoorOpen)
                                        clDoorStatus.visibility = View.VISIBLE
                                    }
                                }
                                else {
                                    name.setTextColor(lockerMacAddressDoorClosed)
                                    clDoorStatus.visibility = View.GONE
                                }

                                log.info("Key purpose: ${device.keyPurpose}, deleting key in progress ${device.deletingKeyInProgress}")
                                log.info("Reduced mobility: ${device.isReducedMobility}, cleaning needed ${device.cleaningNeeded}")

                                if( installationType != InstalationType.TABLET  || (!device.isReducedMobility && device.cleaningNeeded != RActionRequired.CLEANING ) ) {
                                    clReducedMobilityCleaningImages.visibility = View.GONE
                                    ivReducedMobility.visibility = View.GONE
                                    ivCleaningImages.visibility = View.GONE
                                }
                                else {
                                    clReducedMobilityCleaningImages.visibility = View.VISIBLE
                                    val reducedMobilityVisibility = if( !device.isReducedMobility ) View.GONE else View.VISIBLE
                                    val cleaningNeededVisibility = if( device.cleaningNeeded != RActionRequired.CLEANING ) View.GONE else View.VISIBLE
                                    ivReducedMobility.visibility = reducedMobilityVisibility
                                    ivCleaningImages.visibility = cleaningNeededVisibility
                                }

                                if( device.keyPurpose != RLockerKeyPurpose.UNKNOWN && !device.deletingKeyInProgress && !device.requestToDeletingKeyOnBackend ) {
                                    keyPurposeLayout.visibility = View.VISIBLE
                                    tvKeyPurposeValue.text = device.keyPurpose.toString()
                                    keyCreatedForLayout.visibility = View.VISIBLE
                                    device.createdOnDate = formatCorrectDate(device.createdOnDate)
                                    tvKeyCreatedForNameDate.text = device.createdByName + ", " + device.createdOnDate
                                    clDeletingKey.visibility = View.GONE
                                }
                                else if(  device.keyPurpose != RLockerKeyPurpose.UNKNOWN && (device.deletingKeyInProgress || device.requestToDeletingKeyOnBackend) ) {
                                    if( device.deletingKeyInProgress ) {
                                            keyPurposeLayout.visibility = View.GONE
                                            keyCreatedForLayout.visibility = View.GONE
                                        }
                                    else {
                                        keyPurposeLayout.visibility = View.VISIBLE
                                        tvKeyPurposeValue.text = device.keyPurpose.toString()
                                        keyCreatedForLayout.visibility = View.VISIBLE
                                        device.createdOnDate = formatCorrectDate(device.createdOnDate)
                                        tvKeyCreatedForNameDate.text = device.createdByName + ", " + device.createdOnDate
                                    }
                                    clDeletingKey.visibility = View.VISIBLE
                                }
                                else {
                                    keyPurposeLayout.visibility = View.GONE
                                    keyCreatedForLayout.visibility = View.GONE
                                    clDeletingKey.visibility = View.GONE
                                }
                                size.text = itemView.context.getString(R.string.app_generic_size,  device.size.name)
                                lockerSlaveImage.setImageDrawable( registeredNormal )
                                add.setImageDrawable(registeredLockerNormal)
                                //delete.setImageDrawable( lockerDeleteImage )
                                add.setOnClickListener {
                                    val lockerOptionsDialog = LockerOptionsDialog( itemView,this@PeripheralAdapter, installationType, device.mac, masterMac, device.isLockerInProximity,
                                        device.keyPurpose, device.createdByName, device.createdOnDate, device.isMasterUnitInProximity, device.deletingKeyInProgress, device.requestToDeletingKeyOnBackend, device.cleaningNeeded, device.isReducedMobility, device.size)
                                    lockerOptionsDialog.show((ctx as MainActivity).supportFragmentManager, "")
                                }
                            }
                            RLockerType.CLOUD -> {}
                        }
                    }

                    MPLAppDeviceStatus.DELETE_PENDING -> {
                        //delete.visibility = View.GONE
                        add.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        registrationStatus.text = itemView.context.getString(R.string.delete_pending)
                        when( device.deviceType ) {
                            RLockerType.P16  -> {
                                name.setTextColor(lockerMacAddressDoorClosed)
                                clDoorStatus.visibility = View.GONE
                                keyPurposeLayout.visibility = View.GONE
                                keyCreatedForLayout.visibility = View.GONE
                                clDeletingKey.visibility = View.GONE
                                size.text = itemView.context.getString(R.string.app_generic_size, "-")
                                lockerSlaveImage.setImageDrawable( registeredP16 )
                                ivCleaningImages.visibility = View.GONE
                                ivReducedMobility.visibility = View.GONE
                                clReducedMobilityCleaningImages.visibility = View.GONE
                            }
                            RLockerType.NORMAL -> {

                                if( device.doorStatus.size == 1 ) {
                                    if( device.doorStatus[0] == 0 ) {
                                        name.setTextColor(lockerMacAddressDoorClosed)
                                        clDoorStatus.visibility = View.GONE
                                    }
                                    else {
                                        name.setTextColor(lockerMacAddressDoorOpen)
                                        clDoorStatus.visibility = View.VISIBLE
                                    }
                                }
                                else {
                                    name.setTextColor(lockerMacAddressDoorClosed)
                                    clDoorStatus.visibility = View.GONE
                                }

                                if( installationType != InstalationType.TABLET  || (!device.isReducedMobility && device.cleaningNeeded != RActionRequired.CLEANING) ) {
                                    clReducedMobilityCleaningImages.visibility = View.GONE
                                    ivReducedMobility.visibility = View.GONE
                                    ivCleaningImages.visibility = View.GONE
                                }
                                else {
                                    clReducedMobilityCleaningImages.visibility = View.VISIBLE
                                    val reducedMobilityVisibility = if( !device.isReducedMobility ) View.GONE else View.VISIBLE
                                    val cleaningNeededVisibility = if( device.cleaningNeeded != RActionRequired.CLEANING ) View.GONE else View.VISIBLE
                                    ivReducedMobility.visibility = reducedMobilityVisibility
                                    ivCleaningImages.visibility = cleaningNeededVisibility
                                }

                                if( device.keyPurpose != RLockerKeyPurpose.UNKNOWN && !device.deletingKeyInProgress && !device.requestToDeletingKeyOnBackend ) {
                                    keyPurposeLayout.visibility = View.VISIBLE
                                    tvKeyPurposeValue.text = device.keyPurpose.toString()
                                    keyCreatedForLayout.visibility = View.VISIBLE
                                    device.createdOnDate = formatCorrectDate(device.createdOnDate)
                                    tvKeyCreatedForNameDate.text = device.createdByName + ", " + device.createdOnDate
                                    clDeletingKey.visibility = View.GONE
                                }
                                else if(  device.keyPurpose != RLockerKeyPurpose.UNKNOWN && (device.deletingKeyInProgress || device.requestToDeletingKeyOnBackend) ) {
                                    if( device.deletingKeyInProgress ) {
                                        keyPurposeLayout.visibility = View.GONE
                                        keyCreatedForLayout.visibility = View.GONE
                                    }
                                    else {
                                        keyPurposeLayout.visibility = View.VISIBLE
                                        tvKeyPurposeValue.text = device.keyPurpose.toString()
                                        keyCreatedForLayout.visibility = View.VISIBLE
                                        device.createdOnDate = formatCorrectDate(device.createdOnDate)
                                        tvKeyCreatedForNameDate.text = device.createdByName + ", " + device.createdOnDate
                                    }
                                    clDeletingKey.visibility = View.VISIBLE
                                }
                                else {
                                    keyPurposeLayout.visibility = View.GONE
                                    keyCreatedForLayout.visibility = View.GONE
                                    clDeletingKey.visibility = View.GONE
                                }
                                size.text = itemView.context.getString(R.string.app_generic_size, device.size.name)
                                lockerSlaveImage.setImageDrawable( registeredNormal )
                            }
                            RLockerType.CLOUD -> {}
                        }
                    }

                    MPLAppDeviceStatus.INSERT_PENDING -> {
                        keyPurposeLayout.visibility = View.GONE
                        keyCreatedForLayout.visibility = View.GONE
                        clDeletingKey.visibility = View.GONE
                        add.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        registrationStatus.text = itemView.context.getString(R.string.registration_pending)
                        ivCleaningImages.visibility = View.GONE
                        ivReducedMobility.visibility = View.GONE
                        clReducedMobilityCleaningImages.visibility = View.GONE
                        log.info("State: INSERT_PENDING")
                        when( device.deviceType ) {
                            RLockerType.P16  -> {
                                name.setTextColor(lockerMacAddressDoorClosed)
                                clDoorStatus.visibility = View.GONE
                                size.text = itemView.context.getString(R.string.app_generic_size, "-")
                                lockerSlaveImage.setImageDrawable( unregisteredP16 )
                            }
                            RLockerType.NORMAL -> {
                                if( device.doorStatus.size == 1 ) {
                                    if( device.doorStatus[0] == 0 ) {
                                        name.setTextColor(lockerMacAddressDoorClosed)
                                        clDoorStatus.visibility = View.GONE
                                    }
                                    else {
                                        name.setTextColor(lockerMacAddressDoorOpen)
                                        clDoorStatus.visibility = View.VISIBLE
                                    }
                                }
                                else {
                                    name.setTextColor(lockerMacAddressDoorClosed)
                                    clDoorStatus.visibility = View.GONE
                                }
                                size.text = itemView.context.getString(R.string.app_generic_size, "-")
                                lockerSlaveImage.setImageDrawable( unregisteredNormal )
                            }
                            RLockerType.CLOUD -> {}
                        }
                    }
                    MPLAppDeviceStatus.REJECTED -> {}
                    MPLAppDeviceStatus.NEW -> {}
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

        private fun checkIfVersionOfLockerAndMasterAreCompatible(device : RLockerDataUiModel, masterVersion: String, lockerVersion: String) {
            val stmVersionControl =
                stmOrAppVersion.compareTo(masterVersion) ?: -1
            val cleanLockerVersion =
                if (device.lockerSingleOrP16Version.contains("-")) {
                    device.lockerSingleOrP16Version.split("-")[0]
                } else {
                    device.lockerSingleOrP16Version
                }
            val comparedLockerVersion =
                cleanLockerVersion.compareTo(lockerVersion)
            when {
                stmVersionControl >= 0 && comparedLockerVersion >= 0 -> {
                    val keypadDialog = LockerSlaveSizeDialog(
                        installationType,
                        itemView,
                        this@PeripheralAdapter,
                        device.mac
                    )
                    keypadDialog.show(
                        (ctx as MainActivity).supportFragmentManager,
                        ""
                    )
                }
                stmVersionControl < 0 && comparedLockerVersion < 0 -> {
                    val keypadDialog = LockerSlaveSizeDialog(
                        installationType,
                        itemView,
                        this@PeripheralAdapter,
                        device.mac
                    )
                    keypadDialog.show(
                        (ctx as MainActivity).supportFragmentManager,
                        ""
                    )
                }
                else -> {
                    val wrongLockerVersionDialog = WrongLockerVersionDialog(
                        cleanLockerVersion,
                        stmOrAppVersion
                    )
                    wrongLockerVersionDialog.show(
                        (ctx as MainActivity).supportFragmentManager, ""
                    )
                }
            }
        }

        fun itemPeripheralSizePopup(slaveMacAddress: String, selectedLockerSize: RLockerSize, reducedMobilityBoolean: Boolean) {

            progressBar.visibility = View.VISIBLE
            add.visibility = View.GONE

            if (selectedLockerSize != RLockerSize.UNKNOWN) {
                isConnecting.set(true)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                progressBar.visibility = View.VISIBLE
                add.visibility = View.GONE
                GlobalScope.launch {
                    val communicator =
                        MPLDeviceStore.devices[masterMac]?.createBLECommunicator(itemView.context)

                    var successAddNewSlave = false

                    if (communicator != null && communicator.connect()) {

                        val reducedMobility = if( reducedMobilityBoolean ) 0x01.toByte()
                        else 0x00.toByte()

                        successAddNewSlave = if( installationType == InstalationType.TABLET ) communicator.registerSlaveCPLBasel(slaveMacAddress, selectedLockerSize, reducedMobility)
                        else communicator.registerSlave(slaveMacAddress, selectedLockerSize)
                        if (successAddNewSlave) {
                            withContext(Dispatchers.Main) {
                                log.info("Registration for slave device master successfully send. " + " slave mac address is: + ${slaveMacAddress} ")
                                modifyStatusChange(
                                    slaveMacAddress,
                                    ActionStatusType.PERIPHERAL_REGISTRATION
                                )
                            }
                            log.info("Successfully send registration request for slave device master ${masterMac} slave - ${slaveMacAddress} ")
                        }
                        else {
                            log.error("Error while registering the locker ${slaveMacAddress}")
                        }
                    } else {
                        log.error("Error while connecting the peripheral ${slaveMacAddress}")
                    }
                    withContext(Dispatchers.Main) {
                        log.info("Finish adding peripheral ${slaveMacAddress}")
                        if (successAddNewSlave) {
                            progressBar.visibility = View.GONE
                        } else {
                            progressBar.visibility = View.GONE
                        }
                    }
                    communicator?.disconnect()
                    isConnecting.set(false)
                }
            } else {
                //itemView.context.toast(R.string.select_mpl_size)
            }
        }

        private fun modifyStatusChange( macAddress: String, status: ActionStatusType) {
            val statusKey = ActionStatusKey()
            statusKey.macAddress = macAddress
            statusKey.statusType = status
            statusKey.keyId = macAddress + status.name
            ActionStatusHandler.actionStatusDb.put(statusKey)
        }
    }

}
