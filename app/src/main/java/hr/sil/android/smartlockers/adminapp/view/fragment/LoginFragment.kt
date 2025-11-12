package hr.sil.android.smartlockers.adminapp.view.fragment


import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentLoginBinding
import hr.sil.android.smartlockers.adminapp.util.AppUtil
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.LoginActivity
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoginFragment : BaseFragment() {

    private val log = logger()
    var wrongPassword: Boolean = false

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initializeToolbarUILoginActivity(false, getString(R.string.app_generic_sign_in))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding = FragmentLoginBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUi()
    }

    private fun initializeUi() {

        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        binding.tvShowPasswords.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.btnLogin.setOnClickListener {

            log.info("Login activity,,Click cached..")
            lifecycleScope.launch(Dispatchers.Main) {

                if (AppUtil.isInternetAvailable()) {

                    if (validate()) {

                        binding.progressBar.visibility = View.VISIBLE
                        if (UserUtil.login(
                                binding.etEmail.text.toString(),
                                binding.etPassword.text.toString()
                            )
                        ) {
                            SettingsHelper.userPasswordWithoutEncryption = binding.etPassword.text.toString().trim()
                            SettingsHelper.userRegisterOrLogin = true
                            val startIntent =
                                Intent(activity as LoginActivity, MainActivity::class.java)
                            startActivity(startIntent)
                            requireActivity().finish()
                            binding.progressBar.visibility = View.GONE

                        } else {
                            log.info("Error while login device")
                            //App.ref.toast(R.string.wrong_username_or_password)
                        }
                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    //App.ref.toast(R.string.app_generic_server_error)
                }

            }
        }

        if (SettingsHelper.usernameLogin != "") {
            binding.etEmail.text = Editable.Factory.getInstance().newEditable(SettingsHelper.usernameLogin)
        }

        binding.tvShowPasswords.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT

                MotionEvent.ACTION_UP -> binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            true
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.login_fragment_to_password_recovery_fragment)
        }
    }

    private fun validate(): Boolean {
        if (!validateUsername()) return false

        if (!validateNewPassword()) {
            wrongPassword = true
            return false
        }

        wrongPassword = false
        return true
    }

    private fun validateUsername(): Boolean {

        if( binding.etEmail.text.toString().isBlank() ) {
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvEmailError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        binding.tvEmailError.visibility = View.GONE
        return true
    }

    private fun validateNewPassword(): Boolean {

        if( binding.etPassword.text.toString().isBlank() ) {
            binding.tvPasswordError.visibility = View.VISIBLE
            binding.tvPasswordError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        /*else if( etPassword.text.toString().length < 6 ) {
            tvPasswordError.visibility = View.VISIBLE
            tvPasswordError.text = resources.getString(R.string.edit_user_validation_password_min_6_characters)
            return false
        }*/
        binding.tvPasswordError.visibility = View.GONE
        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
