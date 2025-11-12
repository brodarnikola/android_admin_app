package hr.sil.android.smartlockers.adminapp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.smartlockers.adminapp.App
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.FragmentAlertsBinding
import hr.sil.android.smartlockers.adminapp.databinding.FragmentPasswordRecoveryBinding
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.util.connectivity.NetworkChecker
import kotlinx.coroutines.*

class PasswordRecoveryFragment : BaseFragment() {

    val log = logger()

    val parent = Job()

    private lateinit var binding: FragmentPasswordRecoveryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPasswordRecoveryBinding.inflate(layoutInflater)

        initializeToolbarUILoginActivity(true, getString(R.string.forgot_password_title))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root // rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUi()
    }

    private fun initializeUi() {

        binding.btnPasswordRecovery.setOnClickListener {
            val bundle = bundleOf("EMAIL" to binding.etEmail.text.toString())
            log.info("Sended email is: " + bundle + " to String: " + bundle.toString())
            if (validate()) {

                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch(parent) {

                    log.info("Sended email is: AAAA proba")
                    if (NetworkChecker.isInternetConnectionAvailable()) {

                        val result = UserUtil.passwordRecovery(binding.etEmail.text.toString())

                        withContext(Dispatchers.Main) {
                            when {
                                result -> {
                                    val bundle = bundleOf("EMAIL" to binding.etEmail.text.toString())
                                    log.info("Sended email is: " + bundle + " to String: " + bundle.toString())
                                    findNavController().navigate(
                                        R.id.password_recovery_fragment_to_password_update_fragment,
                                        bundle
                                    )
                                }
                                else ->  {
                                    log.error("Error while starting to update passsword for user")
                                    //App.ref.toast(R.string.app_generic_error)
                                }
                            }
                            binding.progressBar.visibility = View.GONE
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(R.string.app_generic_no_network)
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                }
            }
        }
    }

    override fun onPause() {
        if( parent.isActive ) parent.cancel()
        //log.info("Da li ce tu uci. Is coroutine active: " + parent.isActive + " is canceled: " + parent.isCancelled + " completed: " + parent.isCompleted)
        super.onPause()
    }

    private fun validate(): Boolean {

        if( binding.etEmail.text.toString().isBlank() ) {
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvEmailError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( !(".+@.+".toRegex().matches( binding.etEmail.text.toString())) ) {
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvEmailError.text = resources.getString(R.string.message_email_invalid)
            return false
        }
        binding.tvEmailError.visibility = View.GONE
        return true
    }

}