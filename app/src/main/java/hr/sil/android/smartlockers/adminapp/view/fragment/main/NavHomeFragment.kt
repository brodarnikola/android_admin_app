package hr.sil.android.smartlockers.adminapp.view.fragment.main

//import hr.sil.android.smartlockers.adminapp.events.DevicesUpdateEvent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.model.RMasterUnitType
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.data.HomeScreenViewModel
import hr.sil.android.smartlockers.adminapp.data.ItemHomeScreen
import hr.sil.android.smartlockers.adminapp.databinding.FragmentMainScreenBinding
import hr.sil.android.smartlockers.adminapp.events.MPLDevicesUpdatedEvent
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.MPLSplAdapter
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import hr.sil.android.view_util.extensions.hideKeyboard
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class NavHomeFragment : BaseFragment() {

    private val viewModel by navGraphViewModels<HomeScreenViewModel>(R.id.nav_graph_main)

    val log = logger()
    private lateinit var mplSplAdapter: MPLSplAdapter
    private val fragmentLoaderHandler = Handler()

    private var filterSearchImage: Drawable? = null
    private var filterDeleteTextImage: Drawable? = null

    private var filterText: String = ""
    private var filterTextEdited: Boolean = false

    private lateinit var binding: FragmentMainScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        val rootView = inflater.inflate(
//            R.layout.fragment_main_screen, container,
//            false
//        )

        binding = FragmentMainScreenBinding.inflate(layoutInflater)

        initializeToolbarUIMainActivity(false, "", false, false, requireContext())

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return binding.root // rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val mplSplList = getItemsForRecyclerView()

        binding.devicesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        mplSplAdapter = MPLSplAdapter(mplSplList, { partItem: ItemHomeScreen.Child ->
            setDeviceItemClickListener(partItem)
        }, activity as MainActivity)
        binding.devicesRecyclerView.adapter = mplSplAdapter

        (binding.devicesRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false

        if (viewModel.listState != null) {
            log.info("da li ce tu uci BBBBB")
            binding.devicesRecyclerView.layoutManager?.onRestoreInstanceState(viewModel.listState)
            viewModel.listState = null
        }
    }

    override fun onDestroyView() {
        //viewModel.listState = devicesRecyclerView.layoutManager?.onSaveInstanceState()
        super.onDestroyView()
    }

    private fun getItemsForRecyclerView(): MutableList<ItemHomeScreen> {
        val items = mutableListOf<ItemHomeScreen>()

        var filteredItems = MPLDeviceStore.devices.values.toList()
        filteredItems = applyFilter(filteredItems)

        val (splList, mplList) = filteredItems
            .filter { it.masterUnitType != RMasterUnitType.UNKNOWN }
            .partition { it.masterUnitType == RMasterUnitType.SPL || it.type == MPLDeviceType.SPL || it.type == MPLDeviceType.SPL_PLUS || it.masterUnitType == RMasterUnitType.SPL_PLUS }

        if (mplList.isNotEmpty()) {
            val headerHomeScreen = ItemHomeScreen.Header()
            headerHomeScreen.headerTitle = getString(R.string.nav_home_mpl_title)
            items.add(headerHomeScreen)
            items.addAll(mplList.map { ItemHomeScreen.Child(it) })
        }

        if (splList.isNotEmpty()) {
            val headerHomeScreen = ItemHomeScreen.Header()
            headerHomeScreen.headerTitle = getString(R.string.nav_home_spl_title)
            items.add(headerHomeScreen)
            items.addAll(splList.map { ItemHomeScreen.Child(it) })
        }

        return items
    }

    private fun applyFilter(devices: List<MPLDevice>): List<MPLDevice> {
        return if (filterText.isEmpty()) {
            filterTextEdited = false
            binding.ivRemoveFilterText.setImageDrawable(filterSearchImage)
            devices
        } else {
            filterTextEdited = true
            binding.ivRemoveFilterText.setImageDrawable(filterDeleteTextImage)
            val filters = filterText
            devices.filter { device ->
                device.macAddress.lowercase().contains(filters.lowercase())
                        || device.masterUnitAddress.lowercase().contains(filters.lowercase())
                        || device.masterUnitName?.lowercase()?.contains(filters.lowercase()) ?: true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        filterSearchImage = getDrawableAttrValue(R.attr.thmHomeScreenEdittextSearchImage)
        filterDeleteTextImage = getDrawableAttrValue(R.attr.thmHomeScreenEdittextDeleteTextImage)
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


    override fun onResume() {
        super.onResume()

        App.ref.eventBus.register(this)

        binding.filterEdittext.afterTextChangeDelay(250) { text ->
            filterText = text.trim().toUpperCase(Locale.getDefault())
            renderDeviceItems()
        }
        binding.filterEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE)

        binding.ivRemoveFilterText.setOnClickListener {
            clearFilter()
            requireActivity().hideKeyboard()
        }

        log.info("Resuming fragment")
    }

    private fun clearFilter() {
        binding.filterEdittext.setText("")
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

    private fun renderDeviceItems() {
        val mplSplList = getItemsForRecyclerView()
        handleParcelVisibility(mplSplList)
        if (mplSplList.isNotEmpty())
            mplSplAdapter.updateDevices(mplSplList)
    }

    private fun handleParcelVisibility(mplSplList: MutableList<ItemHomeScreen>) {
        val isMPLEmpty = mplSplList.isEmpty()
        if (isMPLEmpty) {
            binding.devicesRecyclerView.visibility = View.GONE
            binding.noDevicesFound.visibility = View.VISIBLE
            if (filterTextEdited) {
                binding.noDevicesFound.setText(resources.getString(R.string.wrong_inserted_text_home_screen))
            } else {
                binding.noDevicesFound.setText(resources.getString(R.string.main_no_parcel_lockers))
            }
        } else {
            binding.devicesRecyclerView.visibility = View.VISIBLE
            binding.noDevicesFound.visibility =
                View.INVISIBLE
        }
    }

    private fun setDeviceItemClickListener(partItem: ItemHomeScreen.Child) {
        viewModel.listState = binding.devicesRecyclerView.layoutManager?.onSaveInstanceState()
        clearFilter()
        val bundle = bundleOf("masterMac" to partItem.mplOrSplDevice?.macAddress)
        log.info("Sended masterMacAddress is: " + bundle + " to String: " + bundle.toString())
        findNavController().navigate(
            R.id.home_screen_fragment_to_device_details_fragment,
            bundle
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        //if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            renderDeviceItems()
        //}
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceBackendNotify(event: DevicesUpdateEvent) {
        log.info("Refresh from backend Event!")
        renderDeviceItems()
    }*/


}