package hr.sil.android.smartlockers.adminapp.view.fragment.main

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.sil.android.smartlockers.adminapp.BuildConfig
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.remote.WSAdmin
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.databinding.FragmentNavAlertsDetailsBinding
import hr.sil.android.smartlockers.adminapp.view.fragment.BaseFragment
import kotlinx.coroutines.*


class NavAlertsDetailsFragment : BaseFragment() {

    val log = logger()
    var messageLogId: Int = 0

    private lateinit var binding: FragmentNavAlertsDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        initializeToolbarUIMainActivity(true, resources.getString(R.string.nav_alarms_title), false, false, requireContext())
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        binding = FragmentNavAlertsDetailsBinding.inflate(layoutInflater)

        return binding.root // rootView
        // return inflater.inflate(R.layout.fragment_nav_alerts_details, container, false)
    }

    override fun onStart() {
        super.onStart()

        messageLogId = arguments?.getInt("messageLogId", 0) ?: 0

        lifecycleScope.launch {
            val firstAlarmMes = WSAdmin.getMessageDataForThisID(messageLogId) ?: ""
            log.info("Html string is: ${firstAlarmMes}")
            withContext(Dispatchers.Main) {

                val webSettings = binding.ttcScroll.settings

                webSettings.javaScriptEnabled = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    webSettings.safeBrowsingEnabled = true  // api 26
                }

                binding.ttcScroll.loadDataWithBaseURL("", firstAlarmMes, "text/html", "UTF-8", "")

                binding.ttcScroll.webChromeClient = WebChromeClient()

                binding.ttcScroll.webViewClient = object : WebViewClient() {

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        handler?.cancel()
                    }

                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        binding.progressBar.visibility = View.GONE

                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url: String = request?.url.toString()
                        if (url.equals("mailto:info@schlauebox.ch")) {

                            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${BuildConfig.APP_BASE_EMAIL}"))
                            startActivity(Intent.createChooser(emailIntent, ""))
                            return true
                        }
                        else if( url.equals("mailto:support@smartlockertec.com" )  ) {
                            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${BuildConfig.APP_BASE_EMAIL}"))
                            startActivity(Intent.createChooser(emailIntent, ""))
                        }
                        else if( url.equals("mailto:support@flexilocker.com" ) ) {
                            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${BuildConfig.APP_BASE_EMAIL}"))
                            startActivity(Intent.createChooser(emailIntent, ""))
                        }
                        else if( url.equals("mailto:support@zwick.it" ) ) {
                            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${BuildConfig.APP_BASE_EMAIL}"))
                            startActivity(Intent.createChooser(emailIntent, ""))
                        }
                        return false
                    }
                }
            }
        }
    }

}
