package hr.sil.android.smartlockers.adminapp.view.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMessageDataLog
import hr.sil.android.smartlockers.adminapp.core.util.formatFromStringToDate
import hr.sil.android.smartlockers.adminapp.core.util.formatToViewDateTimeDefaults
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteOneAlarmsMessagesDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.main.NavAlertsFragment
import java.text.ParseException
import java.util.*

class NotificationAdapter (val navAlertsFragment: NavAlertsFragment, val context: Context, var splLockers: MutableList<RMessageDataLog>,
                           val clickListener: (RMessageDataLog) -> Unit) : RecyclerView.Adapter<NotificationAdapter.NotesViewHolder>() {

    val SPLITED_TITLE_MESSAGE_BIGGER_THAN_1 = 1
    val SECOND_SPLITED_TITLE_MESSAGE_FIRST_INDEX = 0

    val SPLITED_CONTENT_MESSAGE_BIGGER_THAN_1 = 1

    val FIRST_PROPERTIE_NAME = 1
    val FIRST_PROPERTIE_ADDRESS = 2
    val FIRST_PROPERTIE_LOCKER_SIZE = 3
    val FIRST_PROPERTIE_CREATED_ON = 4

    val SPLITED_DEVICE_NAME_BIGGER_THAN_1 = 1
    val SPLITED_DEVICE_ADDRESS_BIGGER_THAN_1 = 1
    val SPLITED_LOCKER_SIZE_BIGGER_THAN_1 = 1
    val SPLITED_CREATED_ON_BIGGER_THAN_1 = 1

    val GET_VALUE_AT_FIRST_INDEX = 0

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bindItem(splLockers[position], clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_alerts, parent, false)

        return NotesViewHolder(itemView)
    }


    override fun getItemCount() = splLockers.size

    fun updateNotes(splLockers: MutableList<RMessageDataLog>) {
        this.splLockers = splLockers
        notifyDataSetChanged()
    }

    fun addNote(note: RMessageDataLog) {
        splLockers.add(note)
        notifyItemChanged(splLockers.size - 1)
    }

    fun removeNote(id: RMessageDataLog) {
        splLockers.remove(id)
        notifyItemChanged(splLockers.size - 1)
    }

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var clMainLayout: ConstraintLayout = itemView.findViewById(R.id.clMainLayout)
        var date: TextView = itemView.findViewById(R.id.notification_item_date)
        val masterName: TextView = itemView.findViewById(R.id.notification_item_master_name)
        val subject: TextView = itemView.findViewById(R.id.notification_subject)
        val body: TextView = itemView.findViewById(R.id.notification_body)
        val clDeleteAlert: ConstraintLayout = itemView.findViewById(R.id.clDeleteAlert)
        val ivGoNext: ImageView = itemView.findViewById(R.id.ivGoNext)

        fun bindItem(messageLog: RMessageDataLog, clickListener: (RMessageDataLog) -> Unit) {

            date.text = formatCorrectDate(messageLog.timeCreated)
            if (messageLog.master___name != null) {
                masterName.visibility = View.VISIBLE
                masterName.text = messageLog.master___name
            }
            else
                masterName.visibility = View.GONE

            if( messageLog.body != null ) {
                body.visibility = View.VISIBLE
                body.text = messageLog.body
                ivGoNext.visibility = View.GONE
            } else {
                body.visibility = View.GONE
                ivGoNext.visibility = View.VISIBLE
                clMainLayout.setOnClickListener {
                    val bundle = bundleOf("messageLogId" to messageLog.id)
                    navAlertsFragment.findNavController().navigate(
                        R.id.alerts_to_details_alerts_fragment,
                        bundle
                    )
                }
            }

            subject.text = messageLog.subject
            clDeleteAlert.setOnClickListener {
                val keypadDialog = DeleteOneAlarmsMessagesDialog(messageLog, this@NotificationAdapter)
                keypadDialog.show((context as MainActivity).supportFragmentManager, "")
                //clickListener(messageLog)
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
            return fromDateToString
        }

        private fun parseHtmlTextFromBackend(content: TextView, parcelLocker: RMessageDataLog) {

            var correctMessage = ""

            val splitTitleMessage = parcelLocker.body.split("<span name=\"titlemessage\">")

            var titleMessage = ""


            if (splitTitleMessage.size > SPLITED_TITLE_MESSAGE_BIGGER_THAN_1) {

                val secondSplitTitleMessage = splitTitleMessage[SPLITED_TITLE_MESSAGE_BIGGER_THAN_1].split("</span><table")
                if (secondSplitTitleMessage.isNotEmpty()) {
                    titleMessage = secondSplitTitleMessage[SECOND_SPLITED_TITLE_MESSAGE_FIRST_INDEX]
                    correctMessage += titleMessage
                }
            }

            val splitContentMessage = parcelLocker.body.split("<table width=\"100%\" class=\"brc\">")
            if( splitContentMessage.size > SPLITED_CONTENT_MESSAGE_BIGGER_THAN_1 ) {
                correctMessage += "<br>"
            }

            if( splitContentMessage.size > SPLITED_CONTENT_MESSAGE_BIGGER_THAN_1 ) {
                val properties = splitContentMessage[SPLITED_CONTENT_MESSAGE_BIGGER_THAN_1].split("<th>")

                val deviceNamePropertie = properties[FIRST_PROPERTIE_NAME].split("</th>")[GET_VALUE_AT_FIRST_INDEX]
                val deviceAddressPropertie = properties[FIRST_PROPERTIE_ADDRESS].split("</th>")[GET_VALUE_AT_FIRST_INDEX]
                val lockerSizePropertie = properties[FIRST_PROPERTIE_LOCKER_SIZE].split("</th>")[GET_VALUE_AT_FIRST_INDEX]
                val createdOnPropertie = properties[FIRST_PROPERTIE_CREATED_ON].split("</th>")[GET_VALUE_AT_FIRST_INDEX]

                for (item in 0 until splitContentMessage.size) {

                    if (item == 0) {
                        continue
                    } else {

                        val splitByTr = splitContentMessage[item].split("<tr")
                        val numberOfRows = splitByTr.filter { it.contains("appData1") }.count()
                        var counter = 1
                        for (index in 0 until splitByTr.size) {

                            if (index == 0)
                                continue
                            else {
                                val deviceNameValue = splitByTr[index].split("appData1=\"")
                                if (deviceNameValue.size > SPLITED_DEVICE_NAME_BIGGER_THAN_1 ) {
                                    val correctDeviceNameValue = deviceNameValue[SPLITED_DEVICE_NAME_BIGGER_THAN_1].split("\" style=\"text-align:center;")
                                    correctMessage += deviceNamePropertie + ": " + correctDeviceNameValue[GET_VALUE_AT_FIRST_INDEX] + "<br>"
                                }

                                val deviceAddressValue = splitByTr[index].split("appData2=\"")
                                if (deviceAddressValue.size > SPLITED_DEVICE_ADDRESS_BIGGER_THAN_1) {
                                    val correctDeviceAddressValue = deviceAddressValue[SPLITED_DEVICE_ADDRESS_BIGGER_THAN_1].split("\" style=\"text-align:center;")
                                    correctMessage += deviceAddressPropertie + ": " + correctDeviceAddressValue[GET_VALUE_AT_FIRST_INDEX] + "<br>"
                                }

                                val lockerSizeValue = splitByTr[index].split("appData3=\"")
                                if (lockerSizeValue.size > SPLITED_LOCKER_SIZE_BIGGER_THAN_1) {
                                    val correctLockerSizeValue = lockerSizeValue[SPLITED_LOCKER_SIZE_BIGGER_THAN_1].split("\" style=\"text-align:center;")
                                    correctMessage += lockerSizePropertie + ": " + correctLockerSizeValue[GET_VALUE_AT_FIRST_INDEX] + "<br>"
                                }

                                val createdOnValue = splitByTr[index].split("appData4=\"")
                                if (createdOnValue.size > SPLITED_CREATED_ON_BIGGER_THAN_1) {
                                    val correctCreatedOnValue = createdOnValue[SPLITED_CREATED_ON_BIGGER_THAN_1].split("\" style=\"text-align:center;")
                                    correctMessage += createdOnPropertie + ": " + correctCreatedOnValue[GET_VALUE_AT_FIRST_INDEX]
                                }

                                when {
                                    counter == numberOfRows -> correctMessage += "<br>"
                                    counter < numberOfRows -> correctMessage += "<br><br>"
                                }
                                counter++
                            }
                        }
                    }
                }
            }

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> content.text =
                    Html.fromHtml(correctMessage, Html.FROM_HTML_MODE_COMPACT)
                else -> content.text = Html.fromHtml(correctMessage)
            }
        }
    }

}