package hr.sil.android.smartlockers.adminapp.compose.view.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java
import kotlin.text.isNullOrBlank

import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.compose.view.ui.theme.AppTheme
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper

class SignUpOnboardingActivity : ComponentActivity() {

    private val log = logger()
    val SPLASH_START = "SPLASH_START"
    private val SPLASH_DISPLAY_LENGTH = 3000L
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var startupBeginTimestamp: Long = 0L
    private var permissionRequestGranted = false
    private val viewModel: SignUpOnboardingViewModel by viewModels()

    private var showSplashScreenState = true
    private val dismissSplashScreenDelay = 200L

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SettingsHelper.setLocale(newBase))
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isConnected = remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                while (true) {
                    isConnected.value = isNetworkAvailable(this@SignUpOnboardingActivity)
                    delay(2000)
                }
            }
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.bg_splash),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "bg splash",
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp, start = 20.dp, end = 20.dp)
                ) {
                    CircularProgressIndicator(
                        color = colorResource(R.color.colorPrimary),
                        modifier = Modifier.size(40.dp)
                    )
                    if (!isConnected.value) {
                        NoInternetScreen()
                    } else {
                        //startApp()

                        setContent {
                            AppTheme {
                                SignUpOnboardingApp(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NoInternetScreen() {
        Text(
            text = "Please check internet connection", //stringResource(R.string.app_generic_no_network),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.colorPrimary),
            textAlign = TextAlign.Center
        )
    }

    @SuppressLint("ServiceCast")
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }

    private suspend fun checkSplashDelay() {
        val duration = System.currentTimeMillis() - startupBeginTimestamp
        if (duration < SPLASH_DISPLAY_LENGTH) {
            delay(SPLASH_DISPLAY_LENGTH - duration)
        }
    }

    private fun setupSystemLanguage() {
        val systemLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getResources().getConfiguration().getLocales().get(0).language.toString()
        } else {
            getResources().getConfiguration().locale.language.toString()
        }

        log.info("System language is: ${systemLanguage}")
        log.info("Shared preference language is: ${SettingsHelper.languageName}")

        if (systemLanguage == "de") {
            SettingsHelper.languageName = "DE"
        } else if (systemLanguage == "fr") {
            SettingsHelper.languageName = "FR"
        } else if (systemLanguage == "it") {
            SettingsHelper.languageName = "IT"
        } else {
            SettingsHelper.languageName = "EN"
        }
    }

}