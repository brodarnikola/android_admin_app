package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.lifecycleScope
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.model.MPLDeviceType
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.remote.model.*
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.core.util.macCleanToReal
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentManageUserDetailsBinding
import hr.sil.android.smartlockers.adminapp.events.MPLDevicesUpdatedEvent
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.dialog.DeleteUserDialog
import hr.sil.android.smartlockers.adminapp.view.dialog.NoEPaperTypeNoPreviewDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode 


private const val ARG_1 = "param1"

private const val ARG_2 = "userId"

class ManageUserDetailsFragment : BaseFragment() {

    var name: String = ""
    var email: String = ""
    var userAccessId: Int = 0

    val E_PAPER_TYPE_NONE = 0 // in aggrement with backend we are sending 0

    private val MAX_ROW_LENGTH = 15
    private val NO_CHARATER_IN_ROW_IN_EPAPER = 0
    private val ROW_SWITCH_POSITION = MAX_ROW_LENGTH + 1
    private val FIRST_INDEX_IN_LIST_SPLITED_BY_SPACE = 0
    private val LESS_THAN_TOTAL_ALLOWED_CHARACTERS = 31

    private val STARTING_FROM_FIRST_CHARACTER = 0

    private val SECOND_ROW_LAST_INDEX = MAX_ROW_LENGTH * 2

    private val INCREASE_BY_ONE_BECAUSE_OF_EMPTY_SPACE = 1

    private val PASSWORD_LENGTH_GREATHER_THEN_ZERO = 0
    private val PASSWORD_LENGTH_SMALLER_THEN_SIX = 6

    private val ROW_POSITION = 0
    private val INDEX_POSITION = 1
    private var cursorPosition = kotlin.IntArray(2)

    private var programaticSetText = false
    private var isOnStartFinished = false

    private var groupNameErrorColor = 0

    private var device: MPLDevice? = null
    var endUserId: Int = 0
    var groupId: Int = 0
    var groupName: String = ""
    var userName: String = ""
    var macAddress: String = ""
    var index: Int = 0
    var accessId: Int = 0
    var status: MPLDeviceStatus = MPLDeviceStatus.UNKNOWN
    var groupOwnerStatus: GroupOwnerStatus = GroupOwnerStatus.NAN
    var groupActionStatus: GroupActionStatus = GroupActionStatus.NAN
    lateinit var radioButtonInActive: RadioButton
    lateinit var radioButtonActive: RadioButton
    lateinit var radioButtonGroup: RadioGroup

    var initializedCheckedUserStatus = GroupOwnerStatusSelected.INACTIVE

    enum class GroupOwnerStatusSelected {
        ACTIVE, INACTIVE
    }

    var ePaperPreviewNotAvailable: Drawable? = null

    val log = logger()

    private lateinit var binding: FragmentManageUserDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        endUserId = arguments?.getInt("endUserId", 0) ?: 0
        groupId = arguments?.getInt("groupId", 0) ?: 0
        groupName = arguments?.getString("groupName", "") ?: ""
        userName = arguments?.getString("userName", "") ?: ""
        macAddress = arguments?.getString("macAddress", "") ?: ""
        index = arguments?.getInt("index", 0) ?: 0
        accessId = arguments?.getInt("accessId", 0) ?: 0
        groupOwnerStatus = GroupOwnerStatus.values()[arguments?.getInt("groupOwnerStatus", 0) ?: 0]
        groupActionStatus =
            GroupActionStatus.values()[arguments?.getInt("groupActionStatus", 0) ?: 0]
        device = MPLDeviceStore.devices[macAddress.macCleanToReal()]
        log.info("Group id is ${groupId}, mac is $macAddress, endUserId is: ${endUserId}")
        log.info("Group owner status is: ${groupOwnerStatus}, groupActionStatus is: ${groupActionStatus}")

        var log = logger()
        arguments?.let {
            name = it.getString(ARG_1) ?: ""
            userAccessId = it.getInt(ARG_2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageUserDetailsBinding.inflate(layoutInflater)
        return binding.root
//        val rootView = inflater.inflate(R.layout.fragment_manage_user_details, container, false)
//        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ePaperUpdate = view.findViewById<Button>(R.id.ePaperUpdate)
        val ePaperUpdateProgress = view.findViewById<ProgressBar>(R.id.ePaperUpdateProgress)
        val ePaperPreview = view.findViewById<Button>(R.id.ePaperPreview)
        radioButtonGroup = view.findViewById<RadioGroup>(R.id.radioGroupUser)
        radioButtonActive = view.findViewById<RadioButton>(R.id.userActive)
        radioButtonInActive = view.findViewById<RadioButton>(R.id.userInActive)
        log.info("GroupActionStatus is: ${groupActionStatus}")
        log.info("GroupOwnerStatus is: ${groupOwnerStatus}")

        if ((groupOwnerStatus == GroupOwnerStatus.INVITED || groupOwnerStatus == GroupOwnerStatus.NAN) || groupActionStatus != GroupActionStatus.NAN) {
            log.info("User is invited")
            for (i in 0 until radioButtonGroup.childCount) {
                radioButtonGroup.getChildAt(i).isEnabled = false
            }
            radioButtonInActive.isChecked = true
        } else {
            log.info("User is active ${GroupOwnerStatus.ACTIVE}")
            radioButtonActive.isEnabled = true
            radioButtonInActive.isEnabled = true
            radioButtonActive.isChecked = groupOwnerStatus == GroupOwnerStatus.ACTIVE
            radioButtonInActive.isChecked = groupOwnerStatus == GroupOwnerStatus.INACTIVE

            if (radioButtonActive.isChecked)
                initializedCheckedUserStatus = GroupOwnerStatusSelected.ACTIVE
            else
                initializedCheckedUserStatus = GroupOwnerStatusSelected.INACTIVE
        }

        ePaperUpdate.visibility = if (device?.isInProximity == true ) View.VISIBLE else View.GONE
        if( device?.type == MPLDeviceType.MASTER || ( device?.masterUnitType == RMasterUnitType.MPL && device?.installationType == InstalationType.DEVICE ) ) {

            if( binding.groupNameTitle != null )
                binding.groupNameTitle.visibility = View.VISIBLE
            if( binding.registerLayoutGroupName != null )
                binding.registerLayoutGroupName.visibility = View.VISIBLE

            if( binding.tvGroupNameDescription != null )
                binding.tvGroupNameDescription.visibility = View.VISIBLE
            ePaperUpdate.text = getString(R.string.main_locker_epaper_update)
            if( ePaperPreview != null )
                ePaperPreview.visibility = View.VISIBLE
        }
        else {
            if( binding.groupNameTitle != null )
                binding.groupNameTitle.visibility = View.GONE
            if( binding.registerLayoutGroupName != null )
                binding.registerLayoutGroupName.visibility = View.GONE
            if( binding.groupNameWrong != null )
                binding.groupNameWrong.visibility = View.GONE
            if( binding.tvGroupNameDescription != null )
                binding.tvGroupNameDescription.visibility = View.GONE

            ePaperUpdate.text = getString(R.string.tablet_refresh_user_list)
            if( ePaperPreview != null )
                ePaperPreview.visibility = View.GONE
        }

        if( ePaperPreview != null ) {
            ePaperPreview.setOnClickListener {
                if (device?.ePaperTypeId != E_PAPER_TYPE_NONE) {
                    log.info("Starting ePaper download for ${device?.masterUnitId?.toLong()}...")
                    lifecycleScope.launch(Dispatchers.Default) {
                        val bitmap = WSAdmin.getEpdPreview(device?.masterUnitId?.toLong() ?: 0)
                        log.info("Response bitmap from backend is ${bitmap != null} ")

                        withContext(Dispatchers.Main) {
                            previewImage(view, bitmap)
                        }
                    }
                } else {
                    val noEpaperTypeDialog =
                        NoEPaperTypeNoPreviewDialog(resources.getString(R.string.no_epaper_preview_available))
                    noEpaperTypeDialog.show(
                        (requireContext() as MainActivity).supportFragmentManager,
                        ""
                    )
                }
            }
        }

        ePaperUpdate.setOnClickListener {

            if( device?.ePaperTypeId != E_PAPER_TYPE_NONE || device?.type == MPLDeviceType.TABLET ) {
                ePaperRefresh()
            }
            else {
                val noEpaperTypeDialog =
                    NoEPaperTypeNoPreviewDialog( resources.getString(R.string.no_epaper_update_available) )
                noEpaperTypeDialog.show(
                    (requireContext() as MainActivity).supportFragmentManager,
                    ""
                )
            }
        }

    }

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

    override fun onStart() {
        super.onStart()

        ePaperPreviewNotAvailable = getDrawableAttrValue(R.attr.thmManageUserEPaperPreviewNotAvailable)

        initializeToolbarUIMainActivity(true, userName, false, true, requireContext())
        binding.nameEditText.setText(userName)

        displayGroupNameOnStart()
        isOnStartFinished = true
        setOnTextChangeListenersForGroupName()
        updateUserDataSetOnClickListener()

        deleteUserFromSystem()
    }

    private fun deleteUserFromSystem() {
        val ivLogout: ImageView? = this.activity?.findViewById(R.id.ivLogout)
        ivLogout?.setOnClickListener {
            val deleteUserDialog = DeleteUserDialog(endUserId, macAddress)
            deleteUserDialog.show(
                (requireContext() as MainActivity).supportFragmentManager, ""
            )
        }
    }

    private fun ePaperRefresh() {
        binding.ePaperUpdate.visibility = View.GONE
        binding.ePaperUpdateProgress.visibility = View.VISIBLE
        GlobalScope.launch {
            val communicator =
                MPLDeviceStore.devices[macAddress.macCleanToReal()]?.createBLECommunicator(view?.context as Context)
            if (communicator?.connect() == true) {
                log.info("Successfully connected")
                val result = communicator.updateEpaper(true)
                withContext(Dispatchers.Main) {
                    if (!result) {
                        //App.ref.toast(requireContext().getString(R.string.main_locker_epaper_update_error))
                        log.error("Error in registration!")
                    } else {
                        if (device?.type == MPLDeviceType.MASTER) {

                            print(requireContext().getString(R.string.main_locker_epaper_update_started))
                            // IMPORTANT AI AGENT ----> show toast !!!!!!!
                            //App.ref.toast(requireContext().getString(R.string.main_locker_epaper_update_started))
                        }
                        else {
                            print(requireContext().getString(R.string.successfully_updated))
                            // IMPORTANT AI AGENT ----> show toast !!!!!!!
                            //App.ref.toast(requireContext().getString(R.string.successfully_updated))
                        }
                    }
                    binding.ePaperUpdateProgress.visibility = View.GONE
                    binding.ePaperUpdate.visibility = View.VISIBLE
                }
            } else {
                withContext(Dispatchers.Main) {
                    //App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                    log.error("Error while connecting the device")
                    binding.ePaperUpdateProgress.visibility = View.GONE
                    binding.ePaperUpdate.visibility = View.VISIBLE
                }
            }
            communicator?.disconnect()
        }
    }


    private fun previewImage(view: View, bitmap: Bitmap?) {
        log.info("Starting image preview..")
        val settingsDialog = Dialog(view.context)
        settingsDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val dialogView = layoutInflater.inflate(R.layout.image_layout, null)
        val image = dialogView.findViewById<ImageView>(R.id.previewed_image)
        val confirm = dialogView.findViewById<Button>(R.id.confirm_dialog)
        confirm.setOnClickListener {
            settingsDialog.dismiss()
        }
        if (bitmap != null) {
            image.setImageBitmap(bitmap)
        } else {
            log.info("Bitmap image is null presenting dummy image.")
            image.setImageDrawable(
                ePaperPreviewNotAvailable
                /*ContextCompat.getDrawable(
                    view.context,
                    R.drawable.e_paper_preview_not_available
                )*/
            )
        }
        settingsDialog.setContentView(dialogView)
        settingsDialog.show()
    }

    private fun updateUserDataSetOnClickListener() {

        binding.btnSaveChanges?.setOnClickListener {
            if (validate()) {
                binding.progressBarSaveChanges.visibility = View.VISIBLE
                binding.btnSaveChanges.visibility = View.GONE
                hideKeyboard(activity as? MainActivity)
                lifecycleScope.launch {
                    val groupNameTogether =
                        binding.groupNameFirstRow.text.toString().trim() + " " + binding.groupNameSecondRow.text.toString().trim()
                    val userData = RGroupNameUpdate()
                    userData.groupId = groupId
                    userData.endUserName = binding.nameEditText.text.toString()
                    userData.groupName = groupNameTogether

                    log.info("Inactivating user id: $endUserId")
                    val backendResult = WSAdmin.updateUserData(userData = userData)

                    var isGroupOwnerStatusChanged = isGroupOwnerStatusChanged()
                    if (isGroupOwnerStatusChanged) {
                        if (radioButtonActive.isChecked) {

                            val backendResultActivation =
                                WSAdmin.activateUserFromSystem(userData = RDeactiateActivateUser().apply {
                                    this.id = endUserId
                                })
                            log.info("Response from changing group owner status is: ${backendResultActivation}")
                        } else {
                            val backendResultDeactivation =
                                WSAdmin.deactivateUserFromSystem(userData = RDeactiateActivateUser().apply {
                                    this.id = endUserId
                                })
                            log.info("Response from changing group owner status is: ${backendResultDeactivation}")
                        }
                    } else {
                        log.info("User did not change, group owner status, so that way we don't need to go to backend.")
                    }

                    withContext(Dispatchers.Main) {
                        if (backendResult) {
                            //App.ref.toast(resources.getString(R.string.app_generic_success))
                            for (i in 0 until radioButtonGroup.childCount) {
                                radioButtonGroup.getChildAt(i).isEnabled = false
                            }
                        } else {
                            log.info("Update of user details failed.")
                            //App.ref.toast(resources.getString(R.string.app_generic_error))
                        }
                        binding.progressBarSaveChanges.visibility = View.GONE
                        binding.btnSaveChanges.visibility = View.VISIBLE
                    }

                }
            }
        }
    }

    private fun isGroupOwnerStatusChanged(): Boolean {
        if (radioButtonActive.isChecked && initializedCheckedUserStatus == GroupOwnerStatusSelected.ACTIVE) {
            return false
        } else if (radioButtonActive.isChecked && initializedCheckedUserStatus == GroupOwnerStatusSelected.INACTIVE) {
            return true
        } else if (radioButtonInActive.isChecked && initializedCheckedUserStatus == GroupOwnerStatusSelected.INACTIVE) {
            return false
        } else if (radioButtonInActive.isChecked && initializedCheckedUserStatus == GroupOwnerStatusSelected.ACTIVE) {
            return true
        }
        return true
    }

    fun hideKeyboard(activity: MainActivity?) {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun validate(): Boolean {
        var validated = true
        if (!validateName()) {
            validated = false
        }
        if (!validateGroupName()) {
            validated = false
        }
        return validated
    }

    private fun validateName(): Boolean {
        if (binding.nameEditText.text.isEmpty()) {
            binding.tvNameError.visibility = View.VISIBLE
            binding.tvNameError.text = getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        } else if (binding.nameEditText.text.length < 4) {
            binding.tvNameError.visibility = View.VISIBLE
            binding.tvNameError.text =
                getString(R.string.edit_user_validation_group_name_min_4_characters)
            return false
        } else {
            binding.tvNameError.visibility = View.GONE
            return true
        }
    }

    private fun validateGroupName(): Boolean {
        val groupNameTogetherLength = binding.groupNameFirstRow.text.length + binding.groupNameSecondRow.text.length
        if (binding.groupNameFirstRow.text.isEmpty() && binding.groupNameSecondRow.text.isEmpty()) {
            binding.groupNameWrong.visibility = View.VISIBLE
            binding.groupNameWrong.text = getString(R.string.edit_user_validation_blank_fields_exist)
            //groupNameTitle.setTextColor(groupNameErrorColor)
            return false
        } else if (groupNameTogetherLength < 4) {
            binding.groupNameWrong.visibility = View.VISIBLE
            binding.groupNameWrong.text =
                getString(R.string.edit_user_validation_group_name_min_4_characters)
            binding.groupNameTitle.setTextColor(groupNameErrorColor)
            return false
        } else {
            binding.groupNameWrong.visibility = View.GONE
            //groupNameTitle.setTextColor(groupNameCorrectColor)
            return true
        }
    }

    fun setOnTextChangeListenersForGroupName() {

        var togetherGroupName = ""

        binding.groupNameFirstRow.run {

            addTextChangedListener(object : TextWatcher {
                var fRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && binding.groupNameFirstRow.isFocused &&
                        binding.groupNameFirstRow.text.isNotEmpty()
                        && cursorPosition[INDEX_POSITION] <= binding.groupNameFirstRow.text.length)
                        binding.groupNameFirstRow.setSelection(cursorPosition[INDEX_POSITION])
                    else if (programaticSetText && binding.groupNameFirstRow.isFocused &&
                        binding.groupNameFirstRow.text.isNotEmpty() &&
                        cursorPosition[INDEX_POSITION] >= binding.groupNameFirstRow.text.length)
                        binding.groupNameFirstRow.setSelection(binding.groupNameFirstRow.text.length)
                }

                override fun beforeTextChanged(
                    p0: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    fRInsertedCharLength = binding.groupNameFirstRow.text.length
                    log.info("First row.. Size before text changing is $fRInsertedCharLength")
                }

                override fun onTextChanged(
                    firstRowText: CharSequence?,
                    positionIndexChanged: Int,
                    before: Int,
                    count: Int
                ) {

                    if (programaticSetText) return

                    programaticSetText = true
                    var newCharacter = ""
                    if (fRInsertedCharLength > firstRowText.toString().length && firstRowText.toString().isNotEmpty()) {
                        if (positionIndexChanged == 0)
                            newCharacter = firstRowText?.get(0).toString()
                        else
                            newCharacter = firstRowText?.get(positionIndexChanged - 1).toString()
                    } else if (firstRowText.toString().isNotEmpty())
                        newCharacter = firstRowText?.get(positionIndexChanged).toString()
                    log.info("last character is: $newCharacter")

                    if (firstRowText.toString().isEmpty()) {
                        reorderAndMoveAllTextUp()
                    } else if (!handleLastCharacterEmptySpace(
                            binding.groupNameFirstRow,
                            binding.groupNameSecondRow
                        )
                    ) {
                        if (binding.groupNameFirstRow.isFocused) {

                            togetherGroupName = parseGroupName()

                            positionCursorInCurrentEdittext(
                                fRInsertedCharLength,
                                firstRowText.toString(),
                                positionIndexChanged,
                                before,
                                count
                            )

                            displayGroupName(togetherGroupName, newCharacter)

                            if (positionIndexChanged + count - 1 == MAX_ROW_LENGTH) {
                                binding.groupNameSecondRow.isEnabled = true
                                binding.groupNameSecondRow.requestFocus()
                                if (binding.groupNameSecondRow.text.contains(" ")) {
                                    val firstWordInSecondRow = binding.groupNameSecondRow.text.split(" ")
                                    binding.groupNameSecondRow.setSelection(firstWordInSecondRow.first().length)
                                } else {
                                    binding.groupNameSecondRow.setSelection(binding.groupNameSecondRow.text.length)
                                }
                            }
                        }
                    }
                    programaticSetText = false
                }
            })
        }

        binding.groupNameSecondRow.run {

            addTextChangedListener(object : TextWatcher {

                var tRInsertedCharLength = 0

                override fun afterTextChanged(p0: Editable?) {
                    if (programaticSetText && binding.groupNameSecondRow.isFocused &&
                        binding.groupNameSecondRow.text.isNotEmpty() &&
                        cursorPosition[INDEX_POSITION] <= binding.groupNameSecondRow.text.length) {
                        binding.groupNameSecondRow.setSelection(cursorPosition[INDEX_POSITION])
                    }
                }

                override fun beforeTextChanged(
                    p0: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    tRInsertedCharLength = binding.groupNameSecondRow.text.length
                    log.info("Second row.. Size before text changing is $tRInsertedCharLength")
                }

                override fun onTextChanged(
                    secondRowText: CharSequence?,
                    positionIndexChanged: Int,
                    before: Int,
                    count: Int
                ) {
                    if (programaticSetText) return

                    var newCharacter = ""
                    if (tRInsertedCharLength > secondRowText.toString().length && secondRowText.toString().isNotEmpty()) {
                        if (positionIndexChanged == 0)
                            newCharacter = secondRowText?.get(0).toString()
                        else
                            newCharacter = secondRowText?.get(positionIndexChanged - 1).toString()
                    } else if (secondRowText.toString().isNotEmpty())
                        newCharacter = secondRowText?.get(positionIndexChanged).toString()
                    log.info("last character is: " + newCharacter)

                    programaticSetText = true
                    if (secondRowText.toString().isEmpty()) {
                        jumpFromSecondRowToFirstRow()
                    } else if (binding.groupNameSecondRow.isFocused) {

                        togetherGroupName = parseGroupName()

                        positionCursorInCurrentEdittext(
                            tRInsertedCharLength,
                            secondRowText.toString(),
                            positionIndexChanged,
                            before,
                            count
                        )

                        displayGroupName(togetherGroupName, newCharacter)
                    }
                    programaticSetText = false
                }
            })
        }
    }

    fun displayGroupName(groupName: String, newCharacter: String) {
        if (groupName.contains(" ")) {
            validateNameWithSpaces(groupName, newCharacter)
        } else {
            validateNameWithoutSpaces(groupName)
        }
    }

    private fun displayGroupNameOnStart() {
        if (groupName.contains(" "))
            validateNameWithSpaces(groupName, "")
        else
            validateNameWithoutSpaces(groupName)
    }

    private fun validateNameWithoutSpaces(nameText: String?) {
        if (nameText != null) {
            if (nameText.length < ROW_SWITCH_POSITION) {
                if (nameText != binding.groupNameFirstRow.text.toString())
                    binding.groupNameFirstRow.setText(
                        nameText.substring(
                            STARTING_FROM_FIRST_CHARACTER,
                            nameText.length
                        )
                    )
                binding.groupNameSecondRow.setText("")
                binding.firstRowCharacterLength.text = "" + nameText.substring(
                    STARTING_FROM_FIRST_CHARACTER,
                    nameText.length
                ).length + "/" + MAX_ROW_LENGTH
                binding.secondRowCharacterLength.text =
                    "" + STARTING_FROM_FIRST_CHARACTER + "/" + MAX_ROW_LENGTH
                // disable second  row in epaper
                binding.groupNameSecondRow.isEnabled = false
            } else if (nameText.length > MAX_ROW_LENGTH && nameText.length < LESS_THAN_TOTAL_ALLOWED_CHARACTERS) {
                binding.groupNameFirstRow.setText(
                    nameText.substring(
                        STARTING_FROM_FIRST_CHARACTER,
                        MAX_ROW_LENGTH
                    )
                )
                if (nameText != binding.groupNameSecondRow.text.toString())
                    binding.groupNameSecondRow.setText(nameText.substring(MAX_ROW_LENGTH, nameText.length))
                binding.firstRowCharacterLength.text = "" + nameText.substring(
                    STARTING_FROM_FIRST_CHARACTER,
                    MAX_ROW_LENGTH
                ).length + "/" + MAX_ROW_LENGTH
                binding.secondRowCharacterLength.text = "" + nameText.substring(
                    MAX_ROW_LENGTH,
                    nameText.length
                ).length + "/" + MAX_ROW_LENGTH
                // disable second row in epaper
                binding.groupNameSecondRow.isEnabled = true
            } else if (nameText.length > SECOND_ROW_LAST_INDEX) {
                if (nameText != binding.groupNameFirstRow.text.toString())
                    binding.groupNameFirstRow.setText(
                        nameText.substring(
                            STARTING_FROM_FIRST_CHARACTER,
                            MAX_ROW_LENGTH
                        )
                    )
                if (nameText != binding.groupNameSecondRow.text.toString())
                    binding.groupNameSecondRow.setText(
                        nameText.substring(
                            MAX_ROW_LENGTH,
                            SECOND_ROW_LAST_INDEX
                        )
                    )
                binding.firstRowCharacterLength.text = "" + nameText.substring(
                    STARTING_FROM_FIRST_CHARACTER,
                    MAX_ROW_LENGTH
                ).length + "/" + MAX_ROW_LENGTH
                binding.secondRowCharacterLength.text = "" + nameText.substring(
                    MAX_ROW_LENGTH,
                    SECOND_ROW_LAST_INDEX
                ).length + "/" + MAX_ROW_LENGTH

                // disable second row in epaper
                binding.groupNameSecondRow.isEnabled = true
            }
        }
    }


    private fun validateNameWithSpaces(groupName: String?, newCharacter: String) {

        var firstRowCounterLength = 0
        var secondRowCounterLength = 0

        var firstRowText = ""
        var secondRowText = ""

        if (groupName != null) {
            val emptySpaces = groupName.split(" ")
            //val lastCharacter = nameText.substring(nameText.length - 1, nameText.length)
            //for (index in 0..emptySpaces.size - 1) {
            for (index in 0 until emptySpaces.size) {

                if (emptySpaces[index].length > MAX_ROW_LENGTH) {

                    if (index == FIRST_INDEX_IN_LIST_SPLITED_BY_SPACE) {
                        firstRowText += emptySpaces[index].substring(
                            STARTING_FROM_FIRST_CHARACTER,
                            MAX_ROW_LENGTH
                        )
                        firstRowCounterLength += emptySpaces[index].substring(
                            STARTING_FROM_FIRST_CHARACTER,
                            MAX_ROW_LENGTH
                        ).length

                        if (emptySpaces[index].length > SECOND_ROW_LAST_INDEX) {

                            secondRowText += emptySpaces[index].substring(
                                MAX_ROW_LENGTH,
                                SECOND_ROW_LAST_INDEX
                            )
                            secondRowCounterLength += emptySpaces[index].substring(
                                MAX_ROW_LENGTH,
                                SECOND_ROW_LAST_INDEX
                            ).length

                        } else {
                            secondRowText += emptySpaces[index].substring(
                                MAX_ROW_LENGTH,
                                emptySpaces[index].length
                            ) + " "
                            secondRowCounterLength += emptySpaces[index].substring(
                                MAX_ROW_LENGTH,
                                emptySpaces[index].length
                            ).length
                        }
                    } else if (secondRowCounterLength <= ROW_SWITCH_POSITION) {

                        secondRowText += emptySpaces[index].substring(
                            STARTING_FROM_FIRST_CHARACTER,
                            MAX_ROW_LENGTH
                        )
                        secondRowCounterLength += secondRowText.length + INCREASE_BY_ONE_BECAUSE_OF_EMPTY_SPACE
                    }
                } else if (firstRowCounterLength < ROW_SWITCH_POSITION && (firstRowCounterLength + emptySpaces[index].length) < ROW_SWITCH_POSITION && secondRowCounterLength <= NO_CHARATER_IN_ROW_IN_EPAPER) {
                    if (emptySpaces.size - 1 == index) {
                        firstRowCounterLength += emptySpaces[index].length
                        firstRowText += emptySpaces[index]
                    } else {
                        firstRowText += emptySpaces[index] + " "
                        firstRowCounterLength = firstRowText.length
                    }
                } else if (secondRowCounterLength < ROW_SWITCH_POSITION) {

                    if (emptySpaces.size - 1 == index) {
                        secondRowCounterLength += emptySpaces[index].length
                        secondRowText += emptySpaces[index]
                    } else {
                        secondRowText += emptySpaces[index] + " "
                        secondRowCounterLength = secondRowText.length
                    }
                }
            }
        }

        when {
            binding.groupNameFirstRow.isFocused || !isOnStartFinished -> {
                firstRowText = currentEdittextRemoveLastCharacterEmptySpace(
                    firstRowText,
                    firstRowCounterLength,
                    newCharacter
                )
                secondRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(
                    secondRowText,
                    secondRowCounterLength
                )
            }
            binding.groupNameSecondRow.isFocused || !isOnStartFinished -> {
                firstRowText = notSelectedEdittextRemoveLastCharacterEmptySpace(
                    firstRowText,
                    firstRowCounterLength
                )
                secondRowText = currentEdittextRemoveLastCharacterEmptySpace(
                    secondRowText,
                    secondRowCounterLength,
                    newCharacter
                )
            }
        }

        if (firstRowText.length >= MAX_ROW_LENGTH) {
            firstRowText = firstRowText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
        }
        if (secondRowText.length >= MAX_ROW_LENGTH) {
            secondRowText = secondRowText.substring(STARTING_FROM_FIRST_CHARACTER, MAX_ROW_LENGTH)
        }

        if (firstRowText != binding.groupNameFirstRow.text.toString()) {
            binding.groupNameFirstRow.setText(firstRowText)
        }
        if (secondRowText != binding.groupNameSecondRow.text.toString()) {
            binding.groupNameSecondRow.setText(secondRowText)
        }


        when {
            binding.groupNameSecondRow.isFocused && secondRowText.isEmpty() -> {
                val isGrouNameThirdFocused = binding.groupNameSecondRow.isFocused
                log.info("da li ce ikad uciiii aaaaa : ${isGrouNameThirdFocused}")
                binding.groupNameFirstRow.requestFocus()
                binding.groupNameFirstRow.setSelection(binding.groupNameFirstRow.text.length)
                binding.groupNameSecondRow.isEnabled = false
            }
        }

        binding.firstRowCharacterLength.text = "" + binding.groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        binding.secondRowCharacterLength.text = "" + binding.groupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH

        // enable or disable second and third row in epaper
        binding.groupNameSecondRow.isEnabled = secondRowText.isNotEmpty()
    }

    private fun currentEdittextRemoveLastCharacterEmptySpace(
        currentText: String,
        currentRowLength: Int,
        newCharacter: String
    ): String {

        if (currentRowLength > NO_CHARATER_IN_ROW_IN_EPAPER && newCharacter != " ") {
            val lastCharacterInRow = currentText.substring(currentText.length - 1)
            if (lastCharacterInRow == " ") {
                val correctRowString = currentText.trim()
                return correctRowString
            } else {
                return currentText
            }
        }
        return currentText
    }

    private fun notSelectedEdittextRemoveLastCharacterEmptySpace(
        notSelectedText: String,
        notSelectedRowLength: Int
    ): String {
        if (notSelectedRowLength > NO_CHARATER_IN_ROW_IN_EPAPER) {
            val lastCharacterInRow = notSelectedText.substring(notSelectedText.length - 1)
            if (lastCharacterInRow == " ") {
                val correctRowString = notSelectedText.trim()
                return correctRowString
            } else
                return notSelectedText
        }
        return notSelectedText
    }

    private fun parseGroupName(): String {

        var groupName = ""

        if (binding.groupNameSecondRow.text.isEmpty()) groupName = binding.groupNameFirstRow.text.toString()
        else if (binding.groupNameSecondRow.text.isNotEmpty()) {
            if (binding.groupNameFirstRow.text.last().toString() == " ")
                groupName = binding.groupNameFirstRow.text.toString() + binding.groupNameSecondRow.text.toString()
            else
                groupName =
                    binding.groupNameFirstRow.text.toString() + " " + binding.groupNameSecondRow.text.toString()
        }
        return groupName
    }

    private fun handleLastCharacterEmptySpace(
        currentEditText: EditText,
        nextEditText: EditText?
    ): Boolean {

        if (currentEditText.selectionEnd == ROW_SWITCH_POSITION && currentEditText.text.length == ROW_SWITCH_POSITION
            && currentEditText.text.last().toString() == " "
        ) {
            nextEditText?.isEnabled = true
            nextEditText?.requestFocus()

            if (cursorPosition[ROW_POSITION] < 3) cursorPosition[ROW_POSITION] = +1
            cursorPosition[INDEX_POSITION] = 0
            currentEditText.setText(currentEditText.text.toString().dropLast(1))
            return true
        }
        return false
    }

    private fun positionCursorInCurrentEdittext(
        currentTextLength: Int,
        currentText: String,
        positionIndexChanged: Int,
        before: Int,
        count: Int
    ) {
        when {
            currentTextLength > currentText.length -> cursorPosition[INDEX_POSITION] =
                positionIndexChanged + count
            else -> cursorPosition[INDEX_POSITION] = positionIndexChanged + before + 1
        }
    }

    private fun jumpFromSecondRowToFirstRow() {
        binding.groupNameFirstRow.requestFocus()
        binding.groupNameFirstRow.setSelection(binding.groupNameFirstRow.text.length)
        binding.groupNameSecondRow.isEnabled = false
        binding.groupNameSecondRow.setText("")
        binding.secondRowCharacterLength.text = "0/" + MAX_ROW_LENGTH
    }

    private fun reorderAndMoveAllTextUp() {
        if (binding.groupNameSecondRow.text.isNotEmpty()) {
            binding.groupNameFirstRow.setText(binding.groupNameSecondRow.text.toString())
            binding.groupNameSecondRow.setText("")
            binding.groupNameSecondRow.isEnabled = false
            binding.firstRowCharacterLength.text = "" + binding.groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
            binding.secondRowCharacterLength.text =
                "" + binding.groupNameSecondRow.text.length + "/" + MAX_ROW_LENGTH
        } else {
            binding.firstRowCharacterLength.text = "" + binding.groupNameFirstRow.text.length + "/" + MAX_ROW_LENGTH
        }
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        App.ref.eventBus.unregister(this)
        super.onPause()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        log.info("Refreshing view ${event.bleMacList.joinToString { it }}")
        if (device != null) {
            binding.ePaperUpdate.visibility = if (device?.isInProximity == true) View.VISIBLE else View.GONE
        }
    }


}