package hr.sil.android.smartlockers.adminapp.view.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.RMplSectionUserAccess
import hr.sil.android.smartlockers.adminapp.data.RMplUserAccess

class MplUserSelectionAdapter (var peripherals: List<RMplSectionUserAccess>, val masterMacUserAccess: RMplUserAccess, val ctx: Context) : RecyclerView.Adapter<MplUserSelectionAdapter.NotesViewHolder>()    {


    val registeredUser: String = "REGISTERED"

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bindItem(peripherals[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {

        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_mpl_user_section, parent, false)
        return NotesViewHolder(itemView)
    }

    override fun getItemCount() = peripherals.size

    var selectedMplPLace: Int = 0

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val log = logger()
        var mpl_button_section_1: ImageView = itemView.findViewById(R.id.mpl_button_section_1)
        var mpl_button_section_2: ImageView = itemView.findViewById(R.id.mpl_button_section_2)
        val tvMPL_name_1: TextView = itemView.findViewById(R.id.tvMPL_name_1)
        val tvMPL_email_1: TextView = itemView.findViewById(R.id.tvMPL_email_1)
        val tvMPL_name_2: TextView = itemView.findViewById(R.id.tvMPL_name_2)
        val tvMPL_email_2: TextView = itemView.findViewById(R.id.tvMPL_email_2)
        val rlMPL_1: RelativeLayout = itemView.findViewById(R.id.rlMPL_1)
        val rlMPL_2: RelativeLayout = itemView.findViewById(R.id.rlMPL_2)

        private val rectangleSelected = getDrawableAttrValue(R.attr.thmAssingUserRectangleSelected)
        private val rectangleUnselected = getDrawableAttrValue(R.attr.thmAssingUserRectangleUnselected)

        private val circleSelected = getDrawableAttrValue(R.attr.thmAssingUserCircleSelected)
        private val circleUnselected = getDrawableAttrValue(R.attr.thmAssingUserCircleUnselected)
        private val circleNotAvailable = getDrawableAttrValue(R.attr.thmAssingUserCircleUnavailable)

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

        fun bindItem(device: RMplSectionUserAccess) {

            tvMPL_name_1.text = device.leftMPLSection.name
            tvMPL_email_1.text = device.leftMPLSection.email

            tvMPL_name_2.text = device.rightMPLSection.name
            tvMPL_email_2.text = device.rightMPLSection.email

            // MPLDeviceStatus.REGISTERED, for some reason when I insert this here, it does not want to work
            if (device.leftMPLSection.status.name.equals(registeredUser)) {
                mpl_button_section_1.background =
                    circleNotAvailable //ContextCompat.getDrawable(ctx, R.drawable.btn_unavailable)
                rlMPL_1.background =
                    rectangleUnselected// ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
            } else if (device.leftMPLSection.isSelected == true) {
                mpl_button_section_1.background =
                    circleSelected //ContextCompat.getDrawable(ctx, R.drawable.btn_selected)
                rlMPL_1.background =
                    rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_yellow_corners)
                rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_white_corners)
                tvMPL_name_1.text = masterMacUserAccess.name
                tvMPL_email_1.text = masterMacUserAccess.email
            } else {
                mpl_button_section_1.background =
                    circleUnselected //ContextCompat.getDrawable(ctx, R.drawable.btn_unselected)
                rlMPL_1.background =
                    rectangleUnselected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
            }


            if (device.rightMPLSection.status.name.equals(registeredUser)) {

                mpl_button_section_2.background =
                    circleNotAvailable //ContextCompat.getDrawable(ctx, R.drawable.btn_unavailable)
                rlMPL_2.background =
                    rectangleUnselected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
            } else if (device.rightMPLSection.isSelected == true) {
                mpl_button_section_2.background =
                    circleSelected //ContextCompat.getDrawable(ctx, R.drawable.btn_selected)
                rlMPL_2.background =
                    rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_yellow_corners)
                rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_white_corners)
                tvMPL_name_2.text = masterMacUserAccess.name
                tvMPL_email_2.text = masterMacUserAccess.email
            } else {

                mpl_button_section_2.background =
                    circleUnselected //ContextCompat.getDrawable(ctx, R.drawable.btn_unselected)
                rlMPL_2.background =
                    rectangleUnselected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
            }


            mpl_button_section_1.setOnClickListener {view ->

                if( !device.leftMPLSection.status.name.equals(registeredUser) ) {

                    for (mplLockerItems in peripherals) {

                        if( mplLockerItems.leftMPLSection.isSelected  ) {

                            mpl_button_section_1.background = circleUnselected //ContextCompat.getDrawable(ctx, R.drawable.btn_unselected)
                            mplLockerItems.leftMPLSection.isSelected = false
                            rlMPL_1.background = rectangleUnselected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
                            notifyItemChanged(peripherals.indexOf(mplLockerItems), mplLockerItems)
                            break
                        }

                        if( mplLockerItems.rightMPLSection.isSelected  ) {

                            mpl_button_section_2.background = circleUnselected // ContextCompat.getDrawable(ctx, R.drawable.btn_unselected)
                            mplLockerItems.rightMPLSection.isSelected = false
                            rlMPL_2.background = rectangleUnselected // ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
                            notifyItemChanged(peripherals.indexOf(mplLockerItems), mplLockerItems)
                            break
                        }
                    }


                    rlMPL_1.background = rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_yellow_corners)
                    rlMPL_1.background = rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_white_corners)
                    mpl_button_section_1.background = circleSelected //ContextCompat.getDrawable(ctx, R.drawable.btn_selected)
                    device.leftMPLSection.isSelected = true
                    lastIndex = device.leftMPLSection.index
                    log.info("user inserted index is left: " + lastIndex)

                    tvMPL_name_1.text = masterMacUserAccess.name
                    tvMPL_email_1.text = masterMacUserAccess.email

                    if( device.rightMPLSection.status.name.equals( registeredUser)  ) {

                        mpl_button_section_2.background = circleNotAvailable //ContextCompat.getDrawable(ctx, R.drawable.btn_unavailable)
                    }
                }
            }


            mpl_button_section_2.setOnClickListener {view ->

                if( !device.rightMPLSection.status.name.equals(registeredUser) ) {

                    for (mplLockerItems in peripherals) {

                        if( mplLockerItems.rightMPLSection.isSelected  ) {

                            mpl_button_section_2.background = circleUnselected// ContextCompat.getDrawable(ctx, R.drawable.btn_unselected)
                            mplLockerItems.rightMPLSection.isSelected = false
                            rlMPL_2.background = rectangleUnselected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
                            notifyItemChanged(peripherals.indexOf(mplLockerItems), mplLockerItems)
                            break
                        }

                        if( mplLockerItems.leftMPLSection.isSelected  ) {

                            mpl_button_section_1.background = circleUnselected //ContextCompat.getDrawable(ctx, R.drawable.btn_unselected)
                            mplLockerItems.leftMPLSection.isSelected = false
                            rlMPL_1.background = rectangleUnselected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectangle)
                            notifyItemChanged(peripherals.indexOf(mplLockerItems), mplLockerItems)
                            break
                        }
                    }

                    rlMPL_2.background = rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_yellow_corners)
                    rlMPL_2.background = rectangleSelected //ContextCompat.getDrawable(ctx, R.drawable.gray_rectang_white_corners)
                    mpl_button_section_2.background = circleSelected //ContextCompat.getDrawable(ctx, R.drawable.btn_selected)
                    device.rightMPLSection.isSelected = true
                    lastIndex = device.rightMPLSection.index
                    log.info("user inserted index is right: " + lastIndex)

                    tvMPL_name_2.text = masterMacUserAccess.name
                    tvMPL_email_2.text = masterMacUserAccess.email


                    if( device.leftMPLSection.status.name.equals( registeredUser)  ) {

                        mpl_button_section_1.background = circleNotAvailable //ContextCompat.getDrawable(ctx, R.drawable.btn_unavailable)
                    }
                }
            }
        }
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
