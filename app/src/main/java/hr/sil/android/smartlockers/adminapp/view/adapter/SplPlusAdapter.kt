package hr.sil.android.smartlockers.adminapp.view.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLockerSize
import hr.sil.android.smartlockers.adminapp.core.remote.model.RRoleEnum
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.*
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.DeactivateSplPlusLockerDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.P16OccupiedLockersDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.main.SplPlusFragment 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplPlusAdapter (peripherals: List<SplPlusPeripheralsModel>, val splPlusFragment: SplPlusFragment, val activity: MainActivity,
                      val sizeList: MutableList<String>, val masterMac: String, val keypadType: ParcelLockerKeyboardType?) :
    RecyclerView.Adapter<SplPlusAdapter.PeripheralItemViewHolder>(),
    PeripheralsSplPlusInterface {

    private val lockersList: MutableList<SplPlusPeripheralsModel> = peripherals.toMutableList()
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
    val SPL_DEFAULT_LOCKER_SIZE_INDEX = 5 // L -> SIZE

    override fun onBindViewHolder(holder: PeripheralItemViewHolder, position: Int) {
        bindItem(holder, lockersList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeripheralItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_spl_plus_lockers, parent, false)
        return PeripheralItemViewHolder(view)
    }

    override fun getItemCount() = lockersList.size

    inner class PeripheralItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val log = logger()
        val clSubMainLayout: ConstraintLayout = itemView.findViewById(R.id.clSubMainLayout)
        val tvLockerStatus: TextView = itemView.findViewById(R.id.tvLockerStatus)
        val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        val leftArrow: ImageView = itemView.findViewById(R.id.leftArrow)
        val rightArrow: ImageView = itemView.findViewById(R.id.rightArrow)
        val addORRemoveLocker: ImageView = itemView.findViewById(R.id.addORRemoveLocker)
        val viewPagerLockerSize: CustomViewPagerP16 = itemView.findViewById(R.id.viewLockerSize)
        val sliderPagerAdapter = P16LockerSizeAdapter(sizeList)
        val forceOpen: ImageView = itemView.findViewById(R.id.forceOpen)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        val lockerSelectedUnregistered = getDrawableAttrValue(R.attr.thmP16LockerSelectedUnregistered)
        val lockerSelectedEmpty =  getDrawableAttrValue(R.attr.thmP16LockerSelectedEmpty)
        val lockerSelectedNew = getDrawableAttrValue(R.attr.thmP16LockerSelectedNew)
        val lockerSelectedOccupied = getDrawableAttrValue(R.attr.thmP16LockerSelectedOccupied)

        val lockerNotSelectedUnregistered = getDrawableAttrValue(R.attr.thmP16LockerNotSelectedUnregistered)
        val lockerNotSelectedEmpty = getDrawableAttrValue(R.attr.thmP16LockerNotSelectedEmpty)
        val lockerNotSelectedOccupied = getDrawableAttrValue(R.attr.thmP16LockerNotSelectedOccupied)

        val addLockerImage = getDrawableAttrValue(R.attr.thmP16AddLockerImage)
        val deleteLockerImage = getDrawableAttrValue(R.attr.thmP16RemoveLockerImage)

        val lockerOccupiedTextColor = getColorAttrValue(R.attr.thmLockerOccupiedTextColor)
        val lockerEmptyTextColor = getColorAttrValue(R.attr.thmLockerEmptyTextColor)

        val lockerSelectedSizeTextColor = getColorAttrValue(R.attr.thmLockerSelectedSizeTextColor)
        val lockerSelectedIndexTextColor = getColorAttrValue(R.attr.thmLockerSelectedIndexTextColor)

        val lockerNotSelectedSizeTextColor = getColorAttrValue(R.attr.thmLockerNotSelectedSizeTextColor)
        val lockerNotSelectedIndexTextColor = getColorAttrValue(R.attr.thmLockerNotSelectedIndexTextColor)

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
                activity.obtainStyledAttributes(attrArray)
            val result = typedArray.getColor(
                0,
                Color.WHITE
            )
            typedArray.recycle()
            return result
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

        if( adapterPosition > lastIndex)
            lastIndex = adapterPosition

        val currentSelectedLocker = lockersList.firstOrNull { it.lockerIndex == lockerIndex }
            ?: SplPlusPeripheralsModel(0, 0, RLockerSize.NOTHING, LockerP16Status.UNREGISTERED, false, "", false, true)

        currentSelectedLocker.isSelected = true
        currentSelectedLocker.lockerStatus = lockerP16Status
        currentSelectedLocker.lockerSizeIndex = AFTER_DELETING_LOCKER_SET_LOCKER_SIZE_INDEX_TO_ZERO
        currentSelectedLocker.lockerSizeByte = RLockerSize.NOTHING
        // this is with animation
        notifyItemChanged(adapterPosition)
    }

    private fun bindItem(holder: PeripheralItemViewHolder, lockerData: SplPlusPeripheralsModel) {

        holder.tvNumber.text = "" + lockerData.lockerIndex
        when {
            (lockerData.lockerStatus == LockerP16Status.EMPTY || lockerData.lockerStatus == LockerP16Status.OCCUPIED)
                    && !lockerData.isDeleted  -> {
                holder.forceOpen.visibility = View.VISIBLE
                holder.forceOpen.setOnClickListener {
                    holder.progressBar.visibility = View.VISIBLE
                    holder.forceOpen.visibility = View.INVISIBLE
                    GlobalScope.launch {
                        forceOpenDoor(lockerData.lockerMac, holder)
                    }
                }
            }
            else -> holder.forceOpen.visibility = View.INVISIBLE
        }

        if (lockerData.isSelected) {

            when {
                lockerData.lockerStatus == LockerP16Status.UNREGISTERED -> {
                    holder.clSubMainLayout.background = holder.lockerSelectedUnregistered
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.GONE
                }
                lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {
                    holder.clSubMainLayout.background = holder.lockerSelectedOccupied
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                }
                lockerData.lockerStatus == LockerP16Status.EMPTY -> {
                    holder.clSubMainLayout.background =  holder.lockerSelectedEmpty
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.sliderPagerAdapter.setSizeTextColor(holder.lockerSelectedSizeTextColor)
                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    if( keypadType != ParcelLockerKeyboardType.SPL || lockerData.lockerSizeByte != RLockerSize.NOTHING ) {
                        holder.leftArrow.visibility = View.VISIBLE
                        holder.rightArrow.visibility = View.VISIBLE
                        holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                        updateSelectedItemUI(lockerData, holder)
                        if( lockerData.lockerSizeIndex > CHECK_LOCKER_SIZE_INDEX_FOR_REGISTERED_BUT_EMPTY_LOCKER )
                            holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.ALL)
                        else
                            holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.RIGHT)
                        if( checkUserRole() )
                            setOnViewPagerTouchListener(holder)
                    }
                    else {
                        holder.leftArrow.visibility = View.GONE
                        holder.rightArrow.visibility = View.GONE
                        holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.NONE)
                        holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                    }

                }
                else -> {

                    holder.clSubMainLayout.background = holder.lockerSelectedNew
                    holder.viewPagerLockerSize.visibility = View.VISIBLE
                    holder.sliderPagerAdapter.setSizeTextColor(holder.lockerSelectedSizeTextColor)

                    if( keypadType != ParcelLockerKeyboardType.SPL ) {
                        holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.RIGHT)
                        if( checkUserRole() )
                            setOnViewPagerTouchListener(holder)
                        holder.leftArrow.visibility = View.VISIBLE
                        holder.rightArrow.visibility = View.VISIBLE
                        holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                        holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                        updateSelectedItemUI(lockerData, holder)
                    }
                    else {
                        holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.NONE)
                        holder.leftArrow.visibility = View.GONE
                        holder.rightArrow.visibility = View.GONE

                        holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                        holder.viewPagerLockerSize.currentItem = SPL_DEFAULT_LOCKER_SIZE_INDEX
                        lockerData.lockerSizeByte = RLockerSize.L
                        lockerData.lockerSizeIndex = SPL_DEFAULT_LOCKER_SIZE_INDEX
                    }
                }
            }

            handleElementsInUI(lockerData, holder)
        } else {
            when {
                lockerData.lockerStatus == LockerP16Status.UNREGISTERED -> {
                    holder.clSubMainLayout.background = holder.lockerNotSelectedUnregistered
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.GONE
                }
                lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {
                    holder.clSubMainLayout.background = holder.lockerNotSelectedOccupied
                    holder.leftArrow.visibility = View.GONE
                    holder.rightArrow.visibility = View.GONE
                    holder.viewPagerLockerSize.visibility = View.VISIBLE

                    holder.viewPagerLockerSize.adapter = holder.sliderPagerAdapter
                    holder.viewPagerLockerSize.currentItem = lockerData.lockerSizeIndex
                }
                lockerData.lockerStatus == LockerP16Status.NEW || lockerData.lockerStatus == LockerP16Status.EMPTY -> {
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
                        splPlusFragment.disableItem(true, LockerP16Status.NEW)

                        lastPosition = holder.adapterPosition
                    }
                    lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {
                        val dialog = P16OccupiedLockersDialog()
                        dialog.show(
                            (splPlusFragment.requireContext() as MainActivity).supportFragmentManager,
                            ""
                        )
                    }
                    lockerData.lockerStatus == LockerP16Status.EMPTY || lockerData.lockerStatus == LockerP16Status.NEW -> {

                        val dialog = DeactivateSplPlusLockerDialog(
                            this@SplPlusAdapter,
                            lockerData.lockerIndex,
                            LockerP16Status.UNREGISTERED,
                            holder.adapterPosition
                        )
                        dialog.show(activity.supportFragmentManager, "")
                    }
                }
            }
            else {
                holder.addORRemoveLocker.isEnabled = false
            }
            //lastPosition = holder.adapterPosition
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
                        lockersList[holder.adapterPosition].lockerStatus = LockerP16Status.EMPTY
                        holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.RIGHT)
                    }
                    sizeList.size - LAST_LOCKER_SIZE_INDEX_POSITION -> holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.LEFT)
                    else -> holder.viewPagerLockerSize.setAllowedSwipeDirection(ViewPagerSwipeDirection.ALL)
                }

                setupCorrectLockerSizeByte(position, holder)
                lockersList[holder.adapterPosition].lockerSizeIndex = position
                leftOrRightArrowClickable(lockersList[holder.adapterPosition], holder)

                if( holder.adapterPosition > lastIndex)
                    lastIndex = holder.adapterPosition

                splPlusFragment.disableItem(true, LockerP16Status.EMPTY)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    private fun setupCorrectLockerSizeByte(position: Int, holder: PeripheralItemViewHolder) {

        when( position ) {
            0 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.NOTHING
            1 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.S
            2 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.L
        }
    }

    private fun updateSelectedItemUI(lockerData: SplPlusPeripheralsModel, holder: PeripheralItemViewHolder) {

        holder.rightArrow.setOnClickListener {
            moveToNextLockerSize(lockerData, holder)
        }

        holder.leftArrow.setOnClickListener {
            moveToPreviousLockerSize(lockerData, holder)
        }

        leftOrRightArrowClickable(lockerData, holder)
    }

    private fun leftOrRightArrowClickable(lockerData: SplPlusPeripheralsModel, holder: PeripheralItemViewHolder) {
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

    private fun moveToPreviousLockerSize(lockerData: SplPlusPeripheralsModel, holder: PeripheralItemViewHolder) {
        if( holder.adapterPosition > lastIndex)
            lastIndex = holder.adapterPosition

        holder.rightArrow.isEnabled = true
        holder.rightArrow.alpha = 1.0f

        splPlusFragment.disableItem(true, LockerP16Status.EMPTY)
        if (lockerData.lockerSizeIndex - DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION > GREATHER_THAN_ZERO) {

            if( lockerData.lockerStatus == LockerP16Status.NEW )
                lockerData.lockerStatus = LockerP16Status.EMPTY
            setupCorrectLockerSizeByteFromArrow(lockerData.lockerSizeIndex - DECREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION, holder)
            // wanted t refresh again screen, so that ? will no longer display, but it seems, there is no need for that
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

    private fun moveToNextLockerSize(lockerData: SplPlusPeripheralsModel, holder: PeripheralItemViewHolder) {
        if( holder.adapterPosition > lastIndex)
            lastIndex = holder.adapterPosition

        holder.leftArrow.isEnabled = true
        holder.leftArrow.alpha = 1.0f

        splPlusFragment.disableItem(true, LockerP16Status.EMPTY)
        if (lockerData.lockerSizeIndex + INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION <= sizeList.size) {

            if( lockerData.lockerStatus == LockerP16Status.NEW )
                lockerData.lockerStatus = LockerP16Status.EMPTY
            setupCorrectLockerSizeByteFromArrow(lockerData.lockerSizeIndex + INCREASE_BY_ONE_LOCKER_SIZE_INDEX_POSITION, holder)
            // wanted t refresh again screen, so that ? will no longer display, but it seems, there is no need for that
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
            1 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.S
            2 -> lockersList[holder.adapterPosition].lockerSizeByte = RLockerSize.L
        }
    }

    private fun findNewSelectedLocker(currentLockerData: SplPlusPeripheralsModel, lockerP16Status: LockerP16Status, holder: PeripheralItemViewHolder) {

        for (previousSelectedIndex in (0 until lockersList.size)) {
            if (lockersList[previousSelectedIndex].isSelected) {
                lockersList[previousSelectedIndex].isSelected = false
                // this is without animation
                notifyItemChanged(previousSelectedIndex, lockersList[previousSelectedIndex])
                break
            }
        }

        if( holder.adapterPosition > lastIndex)
            lastIndex = holder.adapterPosition

        currentLockerData.isSelected = true

        if (lockerP16Status != LockerP16Status.NOTHING)
            currentLockerData.lockerStatus = lockerP16Status
        // this is with animation
        notifyItemChanged(holder.adapterPosition)
    }

    private fun handleElementsInUI(lockerData: SplPlusPeripheralsModel, holder: PeripheralItemViewHolder) {
        when {
            lockerData.lockerStatus == LockerP16Status.UNREGISTERED -> {
                holder.addORRemoveLocker.setImageDrawable( holder.addLockerImage )
                holder.tvLockerStatus.visibility = View.INVISIBLE

                if( lockerData.isSelected ) {
                    holder.tvNumber.setTextColor(holder.lockerSelectedIndexTextColor)
                }
                else {
                    holder.tvNumber.setTextColor(holder.lockerNotSelectedIndexTextColor)
                }
            }
            lockerData.lockerStatus == LockerP16Status.OCCUPIED -> {
                holder.addORRemoveLocker.setImageDrawable( holder.deleteLockerImage )
                holder.tvLockerStatus.visibility = View.VISIBLE
                holder.tvLockerStatus.text = holder.itemView.resources.getString(R.string.locker_occupied)
                holder.tvLockerStatus.setTextColor(holder.lockerOccupiedTextColor)
                holder.tvNumber.setTextColor(holder.lockerNotSelectedIndexTextColor)
                // occupied locker can not be never selected
                holder.sliderPagerAdapter.setSizeTextColor(holder.lockerNotSelectedSizeTextColor)
            }
            lockerData.lockerStatus == LockerP16Status.EMPTY || lockerData.lockerStatus == LockerP16Status.NEW -> {
                holder.addORRemoveLocker.setImageDrawable( holder.deleteLockerImage )
                holder.tvLockerStatus.text = holder.itemView.resources.getString(R.string.locker_empty)
                holder.tvLockerStatus.setTextColor(holder.lockerEmptyTextColor)
                holder.tvLockerStatus.visibility = View.VISIBLE
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

    private suspend fun forceOpenDoor(lockerMac: String, holder: PeripheralItemViewHolder) {
        val device = MPLDeviceStore.devices[masterMac]
        val communicator = device?.createBLECommunicator(splPlusFragment.requireContext())
        if (communicator?.connect() == true) {
            log.info("Requesting force pickup for ${lockerMac} ")
            val bleResponse = communicator.forceOpenDoor(lockerMac)

            if (!bleResponse) {
                log.error(bleResponse.toString())
            } else {
                log.info("Success delivery on ${lockerMac}")
            }
            withContext(Dispatchers.Main) {
                holder.progressBar.visibility = View.INVISIBLE
                holder.forceOpen.visibility = View.VISIBLE
            }
        } else {
            withContext(Dispatchers.Main) {
                //App.ref.toast(R.string.main_locker_ble_connection_error)
                log.error("Error while connecting the device")
                holder.progressBar.visibility = View.INVISIBLE
                holder.forceOpen.visibility = View.VISIBLE
            }
        }
        communicator?.disconnect()
    }

    companion object {

        private var lastIndex: Int = 0

        fun getLastIndexChanged(): Int {
            return this.lastIndex
        }

        fun setLastIndexChanged(lastIndexChaged: Int) {
            lastIndex = lastIndexChaged
        }
    }
}
