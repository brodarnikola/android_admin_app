package hr.sil.android.smartlockers.adminapp.view.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.*
import hr.sil.android.smartlockers.adminapp.util.ListDiffer
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.DeactivateP16LockerDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.LockerOptionsP16Dialog
import hr.sil.android.smartlockers.adminapp.view.fragment.main.PeripheralsP16Fragment 

class PeripheralAdapterP16 (peripherals: List<P16PeripheralsModel>, val peripheralsP16Fragment: PeripheralsP16Fragment, val activity: MainActivity?, val sizeList: MutableList<String>,
                            val masterMac: String, val deviceP16Mac: String, val installationType: InstalationType? ) :
    RecyclerView.Adapter<PeripheralAdapterP16.PeripheralItemViewHolder>(), PeripheralsP16Interface {

    private val lockersList: MutableList<P16PeripheralsModel> = peripherals.toMutableList()
    val log = logger()
    var lastPosition = -1

    val AFTER_DELETING_LOCKER_SET_LOCKER_SIZE_INDEX_TO_ZERO = 0
    val CHECK_LOCKER_SIZE_INDEX_FOR_REGISTERED_BUT_EMPTY_LOCKER = 1
    val ZERO_LOCKER_SIZE_INDEX_POSITION = 0
    val FIRST_LOCKER_SIZE_INDEX_POSITION = 1
    val LAST_LOCKER_SIZE_INDEX_POSITION = 1
    val INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION = 1
    val DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION = 1
    val GREATHER_THAN_ZERO = 0

    override fun onBindViewHolder(holder: PeripheralItemViewHolder, position: Int) {
        bindItem(holder, lockersList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeripheralItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_p16_lockers, parent, false)
        return PeripheralItemViewHolder(view)
    }

    override fun getItemCount() = lockersList.size

    fun updateDevices(updatedDevices: List<P16PeripheralsModel>) {
        val listDiff = ListDiffer.getDiff(
            lockersList,
            updatedDevices,
            { old, new ->
                old.doorStatus == new.doorStatus
            })

        for (diff in listDiff) {
            when (diff) {
                is ListDiffer.DiffInserted -> {
                    lockersList.addAll(diff.elements)
                    notifyItemRangeInserted(diff.position, diff.elements.size)
                }
                is ListDiffer.DiffRemoved -> {
                    //remove devices
                    for (i in (lockersList.size - 1) downTo diff.position) {
                        lockersList.removeAt(i)
                    }
                    notifyItemRangeRemoved(diff.position, diff.count)
                }
                is ListDiffer.DiffChanged -> {
                    lockersList[diff.position] = diff.newElement
                    notifyItemChanged(diff.position)
                }
            }
        }
    }

    inner class PeripheralItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val log = logger()
        val clSubMainLayout: ConstraintLayout = itemView.findViewById(R.id.clSubMainLayout)
        val tvLockerStatus: TextView = itemView.findViewById(R.id.tvLockerStatus)
        val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        val leftArrow: ImageView = itemView.findViewById(R.id.leftArrow)
        val rightArrow: ImageView = itemView.findViewById(R.id.rightArrow)
        val addORRemoveLocker: ImageView = itemView.findViewById(R.id.addORRemoveLocker)
        val viewPagerLockerSize: CustomViewPagerP16 = itemView.findViewById(R.id.viewLockerSize)
        val sliderPagerAdapter =
            P16LockerSizeAdapter(sizeList)
        //val forceOpen: ImageView = itemView.findViewById(R.id.forceOpen
        //val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar

        val ivCleaningNeeded: ImageView = itemView.findViewById(R.id.ivCleaningNeeded)
        val ivReducedMobility: ImageView = itemView.findViewById(R.id.ivReducedMobility)
        val clBottomLayout: ConstraintLayout = itemView.findViewById(R.id.clBottomLayout)

        val lockerSelectedUnregistered = getDrawableAttrValue(R.attr.thmP16LockerSelectedUnregistered)
        val lockerSelectedEmpty =  getDrawableAttrValue(R.attr.thmP16LockerSelectedEmpty)
        val lockerSelectedNew = getDrawableAttrValue(R.attr.thmP16LockerSelectedNew)
        val lockerSelectedOccupied = getDrawableAttrValue(R.attr.thmP16LockerSelectedOccupied)

        val lockerNotSelectedUnregistered = getDrawableAttrValue(R.attr.thmP16LockerNotSelectedUnregistered)
        val lockerNotSelectedEmpty = getDrawableAttrValue(R.attr.thmP16LockerNotSelectedEmpty)
        val lockerNotSelectedOccupied = getDrawableAttrValue(R.attr.thmP16LockerNotSelectedOccupied)

        val lockerDoorOpenUnregistered = getDrawableAttrValue(R.attr.thmP16LockerDoorOpenUnregistered)
        val lockerDoorOpenEmpty = getDrawableAttrValue(R.attr.thmP16LockerDoorOpenEmpty)
        val lockerDoorOpenOccupied = getDrawableAttrValue(R.attr.thmP16LockerDoorOpenOccupied)

        val addLockerImage = getDrawableAttrValue(R.attr.thmP16AddLockerImage)
        val deleteLockerImage = getDrawableAttrValue(R.attr.thmP16RemoveLockerImage)
        val moreOptionsLockerImage = getDrawableAttrValue(R.attr.thmP16MoreOptionsLockerImage)

        val lockerOccupiedTextColor = getColorAttrValue(R.attr.thmLockerOccupiedTextColor)
        val lockerEmptyTextColor = getColorAttrValue(R.attr.thmLockerEmptyTextColor)

        val lockerSelectedSizeTextColor = getColorAttrValue(R.attr.thmLockerSelectedSizeTextColor)
        val lockerSelectedIndexTextColor = getColorAttrValue(R.attr.thmLockerSelectedIndexTextColor)

        val lockerNotSelectedSizeTextColor = getColorAttrValue(R.attr.thmLockerNotSelectedSizeTextColor)
        val lockerNotSelectedIndexTextColor = getColorAttrValue(R.attr.thmLockerNotSelectedIndexTextColor)

        //?attr/
        private fun getDrawableAttrValue(attr: Int): Drawable? {
            val attrArray = intArrayOf(attr)
            val typedArray = activity?.obtainStyledAttributes(attrArray)
            val result = try {
                typedArray?.getDrawable(0)
            } catch (exc: Exception) {
                null
            }
            typedArray?.recycle()
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
    }

    override fun onItemSelected(lockerIndex: Int, lockerP16Status: LockerP16Status, adapterPosition: Int) {

        for (previousSelectedIndex in (0 until lockersList.size)) {
            if (lockersList[previousSelectedIndex].isSelected) {
                lockersList[previousSelectedIndex].isSelected = false
                // this is without animation
                notifyItemChanged(previousSelectedIndex, lockersList[previousSelectedIndex])
                break
            }
        }

        val currentSelectedLocker = lockersList.first{it.lockerIndex == lockerIndex }/* .firstOrNull { it.lockerIndex == lockerIndex }
            ?: P16PeripheralsModel(0, 0, RLockerSize.NOTHING, LockerP16Status.UNREGISTERED, false, "", false, true,
                RLockerKeyPurpose.UNKNOWN, "", Date(), false, false,false, 0, false, false)*/

        if( currentSelectedLocker != null ) {
            currentSelectedLocker.isSelected = true
            currentSelectedLocker.lockerStatus = lockerP16Status
            currentSelectedLocker.lockerSizeIndex =
                AFTER_DELETING_LOCKER_SET_LOCKER_SIZE_INDEX_TO_ZERO
            currentSelectedLocker.lockerSizeByte = RLockerSize.NOTHING
            // this is with animation
            notifyItemChanged(adapterPosition)
        }
    }

    override fun disableEnableReducedMobility(lockerIndex: Int, adapterPosition: Int, disableEnableReducedMobility: Boolean) {
        for (previousSelectedIndex in (0 until lockersList.size)) {
            if (lockersList[previousSelectedIndex].isSelected) {
                lockersList[previousSelectedIndex].isSelected = false
                // this is without animation
                notifyItemChanged(previousSelectedIndex, lockersList[previousSelectedIndex])
                break
            }
        }

        val currentSelectedLocker = lockersList.firstOrNull { it.lockerIndex == lockerIndex }
            ?: P16PeripheralsModel(
                0, 0, RLockerSize.NOTHING, LockerP16Status.UNREGISTERED, false, "", false, true,
                RLockerKeyPurpose.UNKNOWN, "", "", false,
                RActionRequired.NULL, false,false, 0, false, false
            )

        currentSelectedLocker.isSelected = true
        currentSelectedLocker.reducedMobility = disableEnableReducedMobility
        // this is with animation
        notifyItemChanged(adapterPosition)
    }

    override fun cleanOrDirtyLocker(lockerIndex: Int, adapterPosition: Int, cleaningNeeded: RActionRequired) {
        for (previousSelectedIndex in (0 until lockersList.size)) {
            if (lockersList[previousSelectedIndex].isSelected) {
                lockersList[previousSelectedIndex].isSelected = false
                // this is without animation
                notifyItemChanged(previousSelectedIndex, lockersList[previousSelectedIndex])
                break
            }
        }

        val currentSelectedLocker = lockersList.first { it.lockerIndex == lockerIndex }
            /*?: P16PeripheralsModel(0, 0, RLockerSize.NOTHING, LockerP16Status.UNREGISTERED, false, "", false, true,
                RLockerKeyPurpose.UNKNOWN, "", Date(), false, false,false, 0, false, false)*/

        if( currentSelectedLocker != null ) {
            currentSelectedLocker.isSelected = true
            currentSelectedLocker.cleaningNeeded = cleaningNeeded
            // this is with animation
            notifyItemChanged(adapterPosition)
        }
    }

    private fun bindItem(holder: PeripheralItemViewHolder, lockerData: P16PeripheralsModel) {

        holder.tvNumber.text = "" + lockerData.lockerIndex

        if (lockerData.isSelected) {

            when {
                lockerData.lockerStatus == LockerP16Status.UNREGISTERED -> {
                    holder.clSubMainLayout.background = holder.lockerSelectedUnregistered
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.GONE
                }
                lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {
                    holder.clSubMainLayout.background = holder.lockerNotSelectedOccupied
                    holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.NONE)
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                }
                lockerData.lockerStatus == LockerP16Status.EMPTY -> {
                    holder.clSubMainLayout.background = holder.lockerSelectedEmpty
                    holder.leftArrow.visibility = View.VISIBLE
                    holder.rightArrow.visibility = View.VISIBLE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.sliderPagerAdapter.setSizeTextColor(holder.lockerSelectedSizeTextColor)
                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                    updateSelectedItemUI(lockerData, holder)
                    if( lockerData.lockerSizeIndex > CHECK_LOCKER_SIZE_INDEX_FOR_REGISTERED_BUT_EMPTY_LOCKER )
                        holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.ALL)
                    else
                        holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.RIGHT)
                    if( checkUserRole() )
                        setOnViewPagerTouchListener(holder)
                }
                else -> {

                    holder.clSubMainLayout.background = holder.lockerSelectedNew
                    holder.leftArrow.visibility = View.VISIBLE
                    holder.rightArrow.visibility = View.VISIBLE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE

                    holder.sliderPagerAdapter.setSizeTextColor(holder.lockerSelectedSizeTextColor)
                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                    updateSelectedItemUI(lockerData, holder)
                    holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.RIGHT)

                    if( !lockerData.reducedMobility )
                        holder.ivReducedMobility.alpha = 0.2f
                    else
                        holder.ivReducedMobility.alpha = 1.0f

                    holder.ivReducedMobility.setOnClickListener {
                        if( lockerData.reducedMobility ) {
                            it.alpha = 0.2f
                            lockerData.reducedMobility = false
                        }
                        else {
                            it.alpha = 1.0f
                            lockerData.reducedMobility = true
                        }
                    }

                    if( checkUserRole() )
                        setOnViewPagerTouchListener(holder)
                }
            }
            handleElementsInUI(lockerData, holder)
        } else {
            when {
                lockerData.lockerStatus == LockerP16Status.UNREGISTERED -> {
                    if( lockerData.doorStatus == 1 )
                        holder.clSubMainLayout.background = holder.lockerDoorOpenUnregistered
                    else
                        holder.clSubMainLayout.background = holder.lockerNotSelectedUnregistered
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.GONE
                }
                lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {
                    if( lockerData.doorStatus == 1 )
                        holder.clSubMainLayout.background = holder.lockerDoorOpenOccupied
                    else
                        holder.clSubMainLayout.background = holder.lockerNotSelectedOccupied
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE

                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                    holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.NONE)
                }
                lockerData.lockerStatus == LockerP16Status.NEW || lockerData.lockerStatus == LockerP16Status.EMPTY -> {
                    if( lockerData.doorStatus == 1 )
                        holder.clSubMainLayout.background = holder.lockerDoorOpenEmpty
                    else
                        holder.clSubMainLayout.background = holder.lockerNotSelectedEmpty
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.sliderPagerAdapter.setSizeTextColor(holder.lockerNotSelectedSizeTextColor)
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex

                    holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.NONE)
                }
            }
            handleElementsInUI(lockerData, holder)
        }

        holder.clSubMainLayout.setOnClickListener {

            if( checkUserRole() ) {
                holder.clSubMainLayout.isEnabled = true
                if (lastPosition != holder.adapterPosition) {

                    when {
                        lockerData.lockerStatus == LockerP16Status.EMPTY || lockerData.lockerStatus == LockerP16Status.NEW -> {
                            findNewSelectedLocker(lockerData, LockerP16Status.NOTHING, holder)
                        }
                    }
                    lastPosition = holder.adapterPosition
                }
            }
            else {
                holder.clSubMainLayout.isEnabled = false
            }
        }

        holder.addORRemoveLocker.setOnClickListener {

            if( checkUserRole() ) {
                holder.addORRemoveLocker.isEnabled = true
                when {
                    lockerData.lockerStatus == LockerP16Status.UNREGISTERED -> {

                        findNewSelectedLocker(lockerData, LockerP16Status.NEW, holder)
                        peripheralsP16Fragment.disableItem(true, LockerP16Status.NEW)

                        lastPosition = holder.adapterPosition
                    }
                    lockerData.lockerStatus == LockerP16Status.NEW -> {

                        val dialog = DeactivateP16LockerDialog(
                            this@PeripheralAdapterP16,
                            lockerData.lockerIndex,
                            LockerP16Status.UNREGISTERED,
                            holder.adapterPosition
                        )
                        dialog.show(
                            (peripheralsP16Fragment.requireContext() as MainActivity).supportFragmentManager,
                            ""
                        )
                    }
                    lockerData.lockerStatus == LockerP16Status.EMPTY || lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {

                        val dialog = LockerOptionsP16Dialog(
                            lockerData.slaveIndexP16Mac,
                            deviceP16Mac,
                            masterMac,
                            lockerData.isP16DeviceInProximity,
                            lockerData.keyPurpose,
                            lockerData.isAvailable,
                            lockerData.createdByName,
                            lockerData.createdOnDate,
                            lockerData.isMasterDeviceInProximity,
                            lockerData.deletingKeyInProgress,
                            lockerData.requestToDeletingKeyOnBackend,
                            lockerData.reducedMobility,
                            lockerData.cleaningNeeded,
                            this@PeripheralAdapterP16,
                            lockerData.lockerIndex,
                            installationType,
                            holder.adapterPosition
                        )
                        dialog.show(
                            (peripheralsP16Fragment.requireContext() as MainActivity).supportFragmentManager,
                            ""
                        )
                    }
                }
            }
            else {
                holder.addORRemoveLocker.isEnabled = false
            }
            //lastPosition = holder.adapterPosition
        }

        if( lockerData.cleaningNeeded == RActionRequired.CLEANING && installationType == InstalationType.TABLET ) {
            holder.ivCleaningNeeded.visibility = View.VISIBLE
        }
        else {
            holder.ivCleaningNeeded.visibility = View.INVISIBLE
        }
    }

    private fun checkUserRole(): Boolean {
        log.info("User role: ${UserUtil.user?.role?.name}")
        if (UserUtil.user?.role == RRoleEnum.SUPER_ADMIN || UserUtil.user?.role == RRoleEnum.ADMIN)
            return true
        return false
    }

    private fun setOnViewPagerTouchListener(holder: PeripheralItemViewHolder) {

        holder.viewPagerLockerSize.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

                when (position) {
                    ZERO_LOCKER_SIZE_INDEX_POSITION -> holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.RIGHT)
                    FIRST_LOCKER_SIZE_INDEX_POSITION -> {
                        holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.RIGHT)
                    }
                    sizeList.size - LAST_LOCKER_SIZE_INDEX_POSITION -> holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.LEFT)
                    else -> holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.ALL)
                }

                setupCorrectLockerSizeByte(position, holder)
                lockersList[holder.adapterPosition].lockerSizeIndex = position
                leftOrRightArrowClickable(lockersList[holder.adapterPosition], holder)

                peripheralsP16Fragment.disableItem(true, LockerP16Status.EMPTY)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    private fun setupCorrectLockerSizeByte(position: Int, holder: PeripheralItemViewHolder) {

        when( position ) {
            0 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.NOTHING
            1 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.XS
            2 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.S
            3 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.M
            4 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.L
            5 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.XL
        }
    }

    private fun updateSelectedItemUI(lockerData: P16PeripheralsModel, holder: PeripheralItemViewHolder) {

        holder.rightArrow.setOnClickListener {
            moveToNextLockerSize(lockerData, holder)
        }

        holder.leftArrow.setOnClickListener {
            moveToPreviousLockerSize(lockerData, holder)
        }

        leftOrRightArrowClickable(lockerData, holder)
    }

    private fun leftOrRightArrowClickable(lockerData: P16PeripheralsModel, holder: PeripheralItemViewHolder) {
        when {
            lockerData.lockerSizeIndex + INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION != sizeList.size -> {
                holder.rightArrow.isEnabled = true
                holder.rightArrow.alpha = 1.0f
            }
            else -> {
                holder.rightArrow.isEnabled = false
                holder.rightArrow.alpha = 0.5f
            }
        }

        when {
            lockerData.lockerSizeIndex - DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION > GREATHER_THAN_ZERO -> {
                holder.leftArrow.isEnabled = true
                holder.leftArrow.alpha = 1.0f
            }
            else -> {
                holder.leftArrow.isEnabled = false
                holder.leftArrow.alpha = 0.5f
            }
        }
    }

    private fun moveToPreviousLockerSize(lockerData: P16PeripheralsModel, holder: PeripheralItemViewHolder) {

        holder.rightArrow.isEnabled = true
        holder.rightArrow.alpha = 1.0f

        peripheralsP16Fragment.disableItem(true, LockerP16Status.EMPTY)
        if (lockerData.lockerSizeIndex - DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION > GREATHER_THAN_ZERO) {

            setupCorrectLockerSizeByteFromArrow(lockerData.lockerSizeIndex - DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION, holder)
            // wanted to refresh again screen, so that this characters "?" will no longer display, but it seems, there is no need for that
            //notifyItemChanged(holder.adapterPosition, lockersList[holder.adapterPosition])
            holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex - DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION
        }

        when {
            lockerData.lockerSizeIndex - DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION > GREATHER_THAN_ZERO -> {
                holder.leftArrow.isEnabled = true
                holder.leftArrow.alpha = 1.0f
            }
            else -> {
                holder.leftArrow.isEnabled = false
                holder.leftArrow.alpha = 0.5f
            }
        }
    }

    private fun moveToNextLockerSize(lockerData: P16PeripheralsModel, holder: PeripheralItemViewHolder) {

        holder.leftArrow.isEnabled = true
        holder.leftArrow.alpha = 1.0f

        peripheralsP16Fragment.disableItem(true, LockerP16Status.EMPTY)
        if (lockerData.lockerSizeIndex + INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION <= sizeList.size) {

            setupCorrectLockerSizeByteFromArrow(lockerData.lockerSizeIndex + INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION, holder)
            // wanted to refresh again screen, so that this characters "?" will no longer display, but it seems, there is no need for that
            //notifyItemChanged(holder.adapterPosition, lockersList[holder.adapterPosition])
            holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex + INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION
        }

        when {
            lockerData.lockerSizeIndex + INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION != sizeList.size -> {
                holder.rightArrow.isEnabled = true
                holder.rightArrow.alpha = 1.0f
            }
            else -> {
                holder.rightArrow.isEnabled = false
                holder.rightArrow.alpha = 0.5f
            }
        }
    }

    private fun setupCorrectLockerSizeByteFromArrow(position: Int, holder: PeripheralItemViewHolder) {

        when( position ) {
            0 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.NOTHING
            1 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.XS
            2 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.S
            3 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.M
            4 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.L
            5 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.XL
        }
    }

    private fun findNewSelectedLocker(currentLockerData: P16PeripheralsModel, lockerP16Status: LockerP16Status, holder: PeripheralItemViewHolder) {

        for (previousSelectedIndex in (0 until lockersList.size)) {
            if (lockersList[previousSelectedIndex].isSelected) {
                lockersList[previousSelectedIndex].isSelected = false
                // this is without animation
                notifyItemChanged(previousSelectedIndex, lockersList[previousSelectedIndex])
                break
            }
        }

        currentLockerData.isSelected = true

        if (lockerP16Status != LockerP16Status.NOTHING)
            currentLockerData.lockerStatus = lockerP16Status
        // this is with animation
        notifyItemChanged(holder.adapterPosition)
    }

    private fun handleElementsInUI(lockerData: P16PeripheralsModel, holder: PeripheralItemViewHolder) {
        when {
            lockerData.lockerStatus == LockerP16Status.UNREGISTERED -> {
                holder.addORRemoveLocker.setImageDrawable( holder.addLockerImage )
                holder.clBottomLayout.visibility = View.INVISIBLE
                holder.tvLockerStatus.visibility = View.INVISIBLE

                if( lockerData.isSelected ) {
                    holder.tvNumber.setTextColor(holder.lockerSelectedIndexTextColor)
                }
                else {
                    holder.tvNumber.setTextColor(holder.lockerNotSelectedIndexTextColor)
                }
            }
            lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {
                holder.addORRemoveLocker.setImageDrawable( holder.moreOptionsLockerImage )

                holder.clBottomLayout.visibility = View.VISIBLE
                if( installationType == InstalationType.TABLET && lockerData.reducedMobility )
                    holder.ivReducedMobility.visibility = View.VISIBLE
                else
                    holder.ivReducedMobility.visibility = View.GONE

                if( installationType == InstalationType.TABLET && lockerData.cleaningNeeded == RActionRequired.CLEANING )
                    holder.ivCleaningNeeded.visibility = View.VISIBLE
                else
                    holder.ivCleaningNeeded.visibility = View.INVISIBLE

                holder.tvLockerStatus.visibility = View.VISIBLE
                holder.tvLockerStatus.text = holder.itemView.resources.getString(R.string.locker_occupied)
                holder.tvLockerStatus.setTextColor(holder.lockerOccupiedTextColor)
                holder.tvNumber.setTextColor(holder.lockerNotSelectedIndexTextColor)
                // occupied locker can not be never selected
                holder.sliderPagerAdapter.setSizeTextColor(holder.lockerNotSelectedSizeTextColor)
            }
            lockerData.lockerStatus == LockerP16Status.EMPTY || lockerData.lockerStatus == LockerP16Status.NEW -> {

                if( lockerData.lockerStatus == LockerP16Status.EMPTY )
                    holder.addORRemoveLocker.setImageDrawable( holder.moreOptionsLockerImage )
                else
                    holder.addORRemoveLocker.setImageDrawable( holder.deleteLockerImage )

                holder.tvLockerStatus.text = holder.itemView.resources.getString(R.string.locker_empty)
                holder.tvLockerStatus.setTextColor(holder.lockerEmptyTextColor)
                holder.tvLockerStatus.visibility = View.VISIBLE

                holder.clBottomLayout.visibility = View.VISIBLE
                if( installationType == InstalationType.TABLET && ( (lockerData.lockerStatus == LockerP16Status.EMPTY && lockerData.reducedMobility) || lockerData.lockerStatus == LockerP16Status.NEW ) )
                    holder.ivReducedMobility.visibility = View.VISIBLE
                else
                    holder.ivReducedMobility.visibility = View.GONE

                if( installationType == InstalationType.TABLET && lockerData.cleaningNeeded == RActionRequired.CLEANING )
                    holder.ivCleaningNeeded.visibility = View.VISIBLE
                else
                    holder.ivCleaningNeeded.visibility = View.INVISIBLE

                if (lockerData.isSelected) {
                    holder.leftArrow.visibility = View.VISIBLE
                    holder.rightArrow.visibility = View.VISIBLE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.sliderPagerAdapter.setSizeTextColor(holder.lockerSelectedSizeTextColor)
                    holder.tvNumber.setTextColor(holder.lockerSelectedIndexTextColor)
                } else {
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.tvNumber.setTextColor(holder.lockerNotSelectedIndexTextColor)
                    holder.sliderPagerAdapter.setSizeTextColor(holder.lockerNotSelectedSizeTextColor)
                }
            }
        }
    }

}
