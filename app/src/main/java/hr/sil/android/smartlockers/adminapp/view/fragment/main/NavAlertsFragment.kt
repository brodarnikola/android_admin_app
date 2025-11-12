package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMessageDataLog
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.NotificationAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteAllAlarmsMessagesDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NavAlertsFragment : BaseFragment() {

    val notificationAdapter: NotificationAdapter by lazy {
        NotificationAdapter( this, requireContext(), alarmMessageList, { partItem: RMessageDataLog -> splItemClicked( partItem) })
    }

    var alarmMessageList = mutableListOf<RMessageDataLog>()

    private lateinit var binding: FragmentAlertsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initializeToolbarUIMainActivity(true, resources.getString(R.string.app_generic_alerts).uppercase(), false, false, requireContext())
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding = FragmentAlertsBinding.inflate(layoutInflater)

        return binding.root // rootView
//        return inflater.inflate(
//            R.layout.fragment_alerts, container,
//            false
//        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clClearAll.setOnClickListener {
            val keypadDialog = DeleteAllAlarmsMessagesDialog(alarmMessageList, binding.alertRecyclerView)
            keypadDialog.show((requireContext() as MainActivity).supportFragmentManager, "")
        }
    }

    private fun splItemClicked( messageLog: RMessageDataLog) {
        lifecycleScope.launch {
            WSAdmin.deleteMessageItem(messageLog.id)
            DataCache.getAlarmMessageLog(true)
            withContext(Dispatchers.Main) {
                notificationAdapter.removeNote(messageLog)
                notificationAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            alarmMessageList = WSAdmin.getMessageLog()?.data?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {

                binding.progressBar.visibility = View.GONE
                binding.alertRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.alertRecyclerView.adapter = notificationAdapter
            }
        }
    }

}