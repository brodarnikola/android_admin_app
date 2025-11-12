package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.RAssignedGroup
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.core.util.macRealToClean
import hr.sil.android.smartlockers.adminapp.data.RMplUserAccess
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.fonts.EdittextWithFont
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.view.adapter.ManageMasterAccessAdapter
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import hr.sil.android.view_util.extensions.hideKeyboard
import kotlinx.coroutines.*
import java.util.*


class ManageUsersFragment : BaseFragment() {

    val log = logger()
    lateinit var macAddress: String
    private var device: MPLDevice? = null
    var assignedGroupsToEpaper: List<RAssignedGroup> = listOf()

    var allUsersOnThisCPL: List<RMplUserAccess> = listOf()
    var copyAllUsersOnThisCPL: List<RMplUserAccess> = listOf()

    lateinit var masterAccessAdapter: ManageMasterAccessAdapter

    private var filterSearchImage: Drawable? = null
    private var filterDeleteTextImage: Drawable? = null

    private var filterText: String = ""
    private var filterTextEdited: Boolean = false


    lateinit var noUserFound: TextView
    lateinit var clTopLayout: ConstraintLayout
    lateinit var filterEdittext: EdittextWithFont
    lateinit var ivRemoveFilterText: ImageView
    lateinit var usersRecyclerView: RecyclerView
    lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(
            R.layout.fragmet_manage_users, container,
            false
        )
        initializeToolbarUIMainActivity(
            true,
            getString(R.string.main_locker_manage_users),
            false,
            false,
            requireContext()
        )

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        macAddress = arguments?.getString("masterMac", "")?.macRealToClean() ?: ""
        log.info("Received masterMac address is: " + macAddress + " inside manage user fragment.")
        device = MPLDeviceStore.devices[macAddress.macCleanToReal()]
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noUserFound = view.findViewById(R.id.noUserFound)
        clTopLayout = view.findViewById(R.id.clTopLayout)
        filterEdittext = view.findViewById(R.id.filterEdittext)

        ivRemoveFilterText = view.findViewById(R.id.ivRemoveFilterText)
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
    }

    override fun onStart() {
        super.onStart()

        filterSearchImage = getDrawableAttrValue(R.attr.thmHomeScreenEdittextSearchImage)
        filterDeleteTextImage = getDrawableAttrValue(R.attr.thmHomeScreenEdittextDeleteTextImage)


        filterEdittext.afterTextChangeDelay(250) { text ->
            filterText = text.trim().toUpperCase(Locale.getDefault())
            displayUsersOnScreen()
        }
        filterEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE)
        ivRemoveFilterText.setOnClickListener {
            clearFilter()
            requireActivity().hideKeyboard()
        }

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            allUsersOnThisCPL = getListOfAccessRequests()
            copyAllUsersOnThisCPL = allUsersOnThisCPL
            withContext(Dispatchers.Main) {
                displayUsersOnScreen()
            }
        }
    }

    private fun clearFilter() {
        allUsersOnThisCPL = copyAllUsersOnThisCPL
        filterEdittext.setText("")
    }

    private fun applyFilter(devices: List<RMplUserAccess>): List<RMplUserAccess> {
        return if (filterText.isEmpty()) {
            filterTextEdited = false
            ivRemoveFilterText.setImageDrawable(filterSearchImage)
            devices
        } else {
            filterTextEdited = true
            ivRemoveFilterText.setImageDrawable(filterDeleteTextImage)
            val filters = filterText
            devices.filter { device ->
                device.email.toLowerCase().contains(filters.toLowerCase())
                        || device.name.toLowerCase().contains(filters.toLowerCase())
                        || device.groupName?.toLowerCase()?.contains(filters.toLowerCase()) ?: true
            }
        }
    }

    private fun displayUsersOnScreen() {
        if (progressBar != null)
            progressBar.visibility = View.GONE

        allUsersOnThisCPL = applyFilter(copyAllUsersOnThisCPL)

        if (allUsersOnThisCPL.isNotEmpty() && requireContext() != null) {
            if (usersRecyclerView != null && noUserFound != null) {
                usersRecyclerView.visibility = View.VISIBLE
                noUserFound.visibility = View.GONE
            }
            masterAccessAdapter =
                ManageMasterAccessAdapter(
                    allUsersOnThisCPL.toMutableList(),
                    macAddress,
                    requireContext(),
                    assignedGroupsToEpaper,
                    device?.masterUnitType,
                    device?.installationType
                ) { userItem: RMplUserAccess ->
                    setDeviceItemClickListener(userItem)
                }

            if (usersRecyclerView != null && masterAccessAdapter != null) {
                usersRecyclerView.layoutManager =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                usersRecyclerView.adapter = masterAccessAdapter
            }

            clTopLayout.alpha = 1.0f
            filterEdittext.isEnabled = true
        } else {
            if (usersRecyclerView != null && noUserFound != null) {
                usersRecyclerView.visibility = View.GONE
                noUserFound.visibility = View.VISIBLE
                if (filterTextEdited) {
                    noUserFound.setText(resources.getString(R.string.wrong_inserted_text_manage_user))
                } else {
                    noUserFound.setText(resources.getString(R.string.manage_users_no_users))
                }
            }
        }
    }

    //?attr/
    private fun getDrawableAttrValue(attr: Int): Drawable? {
        val attrArray = intArrayOf(attr)
        val typedArray = requireContext().obtainStyledAttributes(attrArray)
        val result = try {
            typedArray.getDrawable(0)
        } catch (exc: Exception) {
            null
        }
        typedArray.recycle()
        return result
    }

    private fun setDeviceItemClickListener(partItem: RMplUserAccess) {
        val groupOwnerStatus = if (partItem.groupOwnerStatus != null) partItem.groupOwnerStatus.value else 0
        val ownerActionStatus = if (partItem.ownerActionStatus != null) partItem.ownerActionStatus?.value else 0

        log.info("Group is: ${groupOwnerStatus}, owner action status: ${ownerActionStatus}")

        val bundle = bundleOf(
            "groupId" to partItem.groupId,
            "groupName" to partItem.groupName,
            "userName" to partItem.name,
            "index" to partItem.index,
            "accessId" to partItem.accessId,
            "status" to partItem.status,
            "macAddress" to macAddress,
            "endUserId" to partItem.endUserId,
            "groupOwnerStatus" to groupOwnerStatus,
            "groupActionStatus" to ownerActionStatus
        )
        log.info("User id: " + bundle + " to String: " + bundle.toString())
        findNavController().navigate(
            R.id.manageUserDetails,
            bundle
        )
    }

    private suspend fun getListOfAccessRequests(): MutableList<RMplUserAccess> {

        val access =
            WSAdmin.getMasterAccessRequests()?.filter { it.masterMac == macAddress }?.toMutableList()
                ?: mutableListOf()
        log.info("Master mac address is: ${macAddress} to get new user")
        assignedGroupsToEpaper = WSAdmin.getAssignedGroupsToEpaper(macAddress) ?: listOf()
        val assinedUsers = assignedGroupsToEpaper.map { RMplUserAccess(it) }
        log.info("Owner action status: ${assinedUsers.joinToString { "-" + it.name + ", " + it.ownerActionStatus + ", "  }}")
        return access.map { RMplUserAccess(it) }.plus(assinedUsers).sortedByDescending { it.status }
            .sortedBy { it.index }.toMutableList()
    }


}