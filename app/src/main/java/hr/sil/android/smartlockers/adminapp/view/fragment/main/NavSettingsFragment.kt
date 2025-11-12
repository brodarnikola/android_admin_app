package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.cache.DataCache
import hr.sil.android.smartlockers.adminapp.core.model.RUpdateAdminInfo
import hr.sil.android.smartlockers.adminapp.core.remote.model.RLanguage
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentSettingsBinding
import hr.sil.android.smartlockers.adminapp.store.MPLDeviceStore
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import hr.sil.android.smartlockers.adminapp.view.adapter.LanguageAdapter
import hr.sil.android.smartlockers.adminapp.view.dialog.LogoutDialog
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext 

class NavSettingsFragment : BaseFragment() {

    val log = logger()
    lateinit var selectedLanguage: RLanguage

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initializeToolbarUIMainActivity(true, resources.getString(R.string.nav_settings_title).uppercase(), true, false, requireContext())
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding = FragmentSettingsBinding.inflate(layoutInflater)

        return binding.root // rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spinerLanguageSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    selectedLanguage = adapterView?.getItemAtPosition(position) as RLanguage
                    log.info("Selected language id is: ${selectedLanguage.id}, code: ${selectedLanguage.code}, name: ${selectedLanguage.name}")
                }
            }

        val ivLogout: ImageView? = this.activity?.findViewById(R.id.ivLogout)
        ivLogout?.setOnClickListener {
            val logoutDialog = LogoutDialog()
            logoutDialog.show(
                (requireContext() as MainActivity).supportFragmentManager, ""
            )
        }

        binding.btnSubmit.setOnClickListener {
            if (validate()) {
                binding.btnSubmit.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    val updateInfo = RUpdateAdminInfo().apply {
                        this.name = binding.nameEditText.text.toString()
                        this.languageId = selectedLanguage.id
                    }

                    if (binding.newPasswordEditText.text.isNotEmpty() ) {
                        updateInfo.password = binding.newPasswordEditText.text.toString()
                    }

                    if (UserUtil.userUpdate(updateInfo)) {
                        withContext(Dispatchers.Main) {
                            App.ref.languageCode = selectedLanguage

                            SettingsHelper.languageName = selectedLanguage.code

                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            this@NavSettingsFragment.activity?.finish()
                            this@NavSettingsFragment.activity?.overridePendingTransition(0, 0)
                            this@NavSettingsFragment.activity?.startActivity(intent)
                            val successNotice = binding.root.resources!!.getString(
                                R.string.successfully_saved_data
                            )
                            //App.ref.toast(successNotice)

                            SettingsHelper.setLocale(requireContext())

                            binding.btnSubmit.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }

        binding.tvShowPasswords.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.oldPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT
                    binding.newPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT
                }

                MotionEvent.ACTION_UP -> {
                    binding.oldPasswordEditText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.newPasswordEditText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
            true
        }

        binding.tvShowPasswords.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun validate(): Boolean {

        if( !validateName() ) {
            return false
        }

        if( !validateOldPassword() ) {
            return false
        }

        if( !validateNewPassword() ) {
            return false
        }

        return true
    }

    private fun validateOldPassword(): Boolean {
        if ( binding.oldPasswordEditText.text.toString().isBlank() && !binding.newPasswordEditText.text.isBlank() ) {
            binding.tvOldPasswordError.visibility = View.VISIBLE
            binding.tvOldPasswordError.setText(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( binding.oldPasswordEditText.text.toString() != SettingsHelper.userPasswordWithoutEncryption && binding.oldPasswordEditText.text.isNotBlank() ) {
            binding.tvOldPasswordError.visibility = View.VISIBLE
            binding.tvOldPasswordError.setText(R.string.edit_user_validation_current_password_invalid)
            return false
        }
        binding.tvOldPasswordError.visibility = View.GONE
        return true
    }

    private fun validateNewPassword(): Boolean {
        if ( binding.newPasswordEditText.text.isBlank() && !binding.oldPasswordEditText.text.toString().isBlank()  ) {
            binding.tvNewPasswordError.visibility = View.VISIBLE
            binding.tvNewPasswordError.setText(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        binding.tvNewPasswordError.visibility = View.GONE
        return true
    }

    private fun validateName(): Boolean {
        if ( binding.nameEditText.text.toString().isBlank() || binding.nameEditText.length() > 100 ) {
            binding.tvNameError.visibility = View.VISIBLE
            return false
        }
        binding.tvNameError.visibility = View.GONE
        return true
    }

    override fun onStart() {
        super.onStart()
        binding.nameEditText.setText(UserUtil.user?.name)
        binding.settingsEmail.setText(UserUtil.user?.email)
        binding.tvVersion.text = resources.getString(
            R.string.nav_settings_app_version,
            resources.getString(R.string.app_version)
        )

        lifecycleScope.launch {
            val languagesList = DataCache.getLanguages().toList()
            withContext(Dispatchers.Main) {
                binding.spinerLanguageSelection.adapter = LanguageAdapter(languagesList)
                if (context != null) {

                    val languageName = SettingsHelper.languageName
                    binding.spinerLanguageSelection.setSelection(languagesList.indexOfFirst { it.code == languageName })

                    binding.tvRegisteredDevicesNumber.text = " " + MPLDeviceStore.mDevices.values.filter { it.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED  && it.masterUnitId != -1 }.size

                    /* val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val languageName = preferences.getString(SELECTED_LANGUAGE, "EN")
                    languageSelection.setSelection(languagesList.indexOfFirst { it.code == languageName }) */
                }
            }
        }

    }
}