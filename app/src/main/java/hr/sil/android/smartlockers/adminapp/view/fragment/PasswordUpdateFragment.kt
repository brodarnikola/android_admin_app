package hr.sil.android.smartlockers.adminapp.view.fragment

import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.fonts.EdittextWithFont
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.util.connectivity.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PasswordUpdateFragment : BaseFragment() {

    val log = logger()

    lateinit var tvShowPasswords: TextView
    lateinit var etPassword: EdittextWithFont
    lateinit var etRepeatPassword: EdittextWithFont
    lateinit var btnPasswordUpdate: Button
    lateinit var progressBar: ProgressBar

    lateinit var etPin: EdittextWithFont
    lateinit var tvPasswordError: TextView
    lateinit var tvRepeatPasswordError: TextView
    lateinit var tvPinError: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(
            R.layout.fragmnet_password_update, container,
            false
        )

        initializeToolbarUILoginActivity(true, getString(R.string.reset_password_title))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvShowPasswords = view.findViewById(R.id.tvShowPasswords)
        etPassword = view.findViewById(R.id.etPassword)
        etRepeatPassword = view.findViewById(R.id.etRepeatPassword)
        btnPasswordUpdate = view.findViewById(R.id.btnPasswordUpdate)
        progressBar = view.findViewById(R.id.progressBar)
        etPin = view.findViewById(R.id.etPin)

        tvPasswordError = view.findViewById(R.id.tvPasswordError)
        tvRepeatPasswordError = view.findViewById(R.id.tvRepeatPasswordError)
        tvPinError = view.findViewById(R.id.tvPinError)

        initializeUi()

    }

    private fun initializeUi() {
        val email = arguments?.getString("EMAIL") ?: ""
        log.info("Received email is: " + email)

        tvShowPasswords.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        tvShowPasswords.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    etPassword.inputType = InputType.TYPE_CLASS_TEXT
                    etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT
                }

                MotionEvent.ACTION_UP -> {
                    etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
            true
        }

        btnPasswordUpdate.setOnClickListener {
            if (validate()) {

                lifecycleScope.launch {

                    if (NetworkChecker.isInternetConnectionAvailable()) {

                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.VISIBLE
                        }

                        if (email != null && submitResetPass(email)) {

                            findNavController().navigate(R.id.password_update_fragment_to_login_fragment)
                            progressBar.visibility = View.GONE
                        } else {
                            withContext(Dispatchers.Main) {
                                //App.ref.toast(R.string.app_generic_error)
                                progressBar.visibility = View.GONE
                                log.error("Error while updating passsword from the user")
                            }
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(this@PasswordUpdateFragment.getString(R.string.app_common_internet_connection))
                            progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun validate(): Boolean {
        if (!validatePassword()) return false

        if (!validateRepeatPassword()) {
            return false
        }

        if( !validatePin() ) return false

        return true
    }

    private fun validatePassword(): Boolean {

        if( etPassword.text.toString().isBlank() ) {
            tvPasswordError.visibility = View.VISIBLE
            tvPasswordError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( etPassword.text.toString().length < 6 ) {
            tvPasswordError.visibility = View.VISIBLE
            tvPasswordError.text = resources.getString(R.string.edit_user_validation_password_min_6_characters)
            return false
        }
        tvPasswordError.visibility = View.GONE
        return true
    }

    private fun validateRepeatPassword(): Boolean {
        if( etRepeatPassword.text.toString().isBlank() ) {
            tvRepeatPasswordError.visibility = View.VISIBLE
            tvRepeatPasswordError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( etRepeatPassword.text.toString().length < 6 ) {
            tvRepeatPasswordError.visibility = View.VISIBLE
            tvRepeatPasswordError.text = resources.getString(R.string.edit_user_validation_password_min_6_characters)
            return false
        }
        else if( etRepeatPassword.text.toString() != etPassword.text.toString() ) {
            tvRepeatPasswordError.visibility = View.VISIBLE
            tvRepeatPasswordError.text = resources.getString(R.string.password_dont_match)
            return false
        }
        tvRepeatPasswordError.visibility = View.GONE
        return true
    }

    private fun validatePin(): Boolean {
        if( etPin.text.toString().isBlank() ) {
            tvPinError.visibility = View.VISIBLE
            tvPinError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        tvPinError.visibility = View.GONE
        return true
    }

    private suspend fun submitResetPass(email: String): Boolean {
            return UserUtil.passwordReset(
                email = email,
                password = etPassword.text.toString(),
                passwordCode = etPin.text.toString())

    }

}