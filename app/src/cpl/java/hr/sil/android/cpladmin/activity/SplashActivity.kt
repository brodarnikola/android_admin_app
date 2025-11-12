package hr.sil.android.cpladmin.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.sil.android.smartlockers.adminapp.BuildConfig
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.preferences.PreferenceStore
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import hr.sil.android.smartlockers.adminapp.view.activity.LoginActivity
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private val log = logger()
    val SPLASH_START = "SPLASH_START"
    private val SPLASH_DISPLAY_LENGTH = 3000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //tvAppVersion.setText(resources.getString(R.string.nav_settings_app_version,
        //    resources.getString(R.string.app_version)))

        preloadAndStartMain()
    }

    private fun preloadAndStartMain() {
        lifecycleScope.launch {
            val beginTimestamp = System.currentTimeMillis()
            val duration = System.currentTimeMillis() - beginTimestamp
            log.info("App Start length:" + duration)
            if (duration < SPLASH_DISPLAY_LENGTH) {
                delay(SPLASH_DISPLAY_LENGTH - duration)
            }

            withContext(Dispatchers.Main) {
                startApp()
                finish()
            }
        }
    }

    private suspend fun startApp() {
        val startupClass: Class<*>
        startupClass = if (!PreferenceStore.userHash.isNullOrBlank()) {
            if (UserUtil.login(SettingsHelper.usernameLogin)) {
                MainActivity::class.java

            } else {
                if( !SettingsHelper.userRegisterOrLogin ) {
                    setupSystemLanguage()
                }
                LoginActivity::class.java
            }

        } else {
            if( !SettingsHelper.userRegisterOrLogin ) {
                setupSystemLanguage()
            }
            LoginActivity::class.java
        }
        log.info("SplashActivity", "This is second start")

        //progressBarSplash.visibility = View.GONE

        val startIntent = Intent(this@SplashActivity, startupClass)
        startActivity(startIntent)
        finish()
    }

    private fun setupSystemLanguage() {
        val systemLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            getResources().getConfiguration().getLocales().get(0).language.toString()
        } else{
            getResources().getConfiguration().locale.language.toString()
        }

        log.info("System language is: ${systemLanguage}")
        log.info("Shared preference language is: ${SettingsHelper.languageName}")

        if( systemLanguage == "de" ) {
            SettingsHelper.languageName = "DE"
        }
        else if( systemLanguage == "fr" ) {
            SettingsHelper.languageName = "FR"
        }
        else if( systemLanguage == "it" ) {
            SettingsHelper.languageName = "IT"
        }
        else {
            SettingsHelper.languageName = "EN"
        }
    }




}
