package hr.sil.android.smartlockers.adminapp.view.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.esotericsoftware.minlog.Log
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.util.NotificationHelper
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.util.connectivity.BluetoothChecker
import hr.sil.android.smartlockers.adminapp.util.connectivity.NetworkChecker
import androidx.core.content.ContextCompat.getSystemService
import com.esotericsoftware.kryo.util.IntArray
import hr.sil.android.smartlockers.adminapp.util.connectivity.LocationGPSChecker
import hr.sil.android.smartlockers.adminapp.view.activity.BaseActivity
import hr.sil.android.smartlockers.adminapp.view.activity.BaseActivity.ValidationResult




open class BaseActivity(noBleViewId: Int = 0, noWifiViewId: Int = 0, noLocationGPSViewId: Int = 0) : AppCompatActivity() {

    protected var viewLoaded = false
    private var btCheckerListenerKey: String? = null
    private var networkCheckerListenerKey: String? = null
    private var locationGPSListenerKey: String? = null
    var networkAvailable: Boolean = true
    var bluetoothAvailable: Boolean = true
    var locationGPSAvalilable: Boolean = true

    var mFirebaseAnalytics: FirebaseAnalytics? = null

    private val frame by lazy { if (noBleViewId != 0) findViewById<FrameLayout>(noBleViewId) else null }
    private val noWifiFrame by lazy { if (noWifiViewId != 0) findViewById<FrameLayout>(noWifiViewId) else null }
    val noLocationGPSFrame by lazy { if (noLocationGPSViewId != 0) findViewById<FrameLayout>(noLocationGPSViewId) else null }
    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    override fun onResume() {
        super.onResume()


        if (btCheckerListenerKey == null) {
            btCheckerListenerKey = BluetoothChecker.addListener { available ->
                uiHandler.post { onBluetoothStateUpdated(available) }
            }
        }
        if (networkCheckerListenerKey == null) {
            networkCheckerListenerKey = NetworkChecker.addListener { available ->
                uiHandler.post { onNetworkStateUpdated(available) }
            }
        }
        if (locationGPSListenerKey == null) {
            locationGPSListenerKey = LocationGPSChecker(this).addListener { available ->
                uiHandler.post { onLocationGPSStateUpdated(available) }
            }
        }
        NotificationHelper.clearNotification()
    }

    fun updateUI() {
        if (frame != null && noWifiFrame != null) {
            frame?.visibility = if (bluetoothAvailable) View.GONE else View.VISIBLE
            noWifiFrame?.visibility = if (networkAvailable) View.GONE else {
                if (!bluetoothAvailable) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            noLocationGPSFrame?.visibility = if (locationGPSAvalilable) View.GONE else {
                if (!bluetoothAvailable || !networkAvailable) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        Log.info("onPause()")

        btCheckerListenerKey?.let { BluetoothChecker.removeListener(it) }
        btCheckerListenerKey = null
        networkCheckerListenerKey?.let { NetworkChecker.removeListener(it) }
        networkCheckerListenerKey = null
        locationGPSListenerKey?.let { LocationGPSChecker(this).removeListener(it) }
        locationGPSListenerKey = null
    }

    /*fun setNoBleOverLay() {
        val viewGroup = contentView?.overlay as ViewGroupOverlay?
        val overlayView =
                _FrameLayout(this).apply {
                    background = ContextCompat.getDrawable(this@BaseActivity, R.drawable.bg_bluetooth)
                    alpha = 0.8f
                    textView(R.string.app_generic_no_ble) {
                        textColor = Color.WHITE
                        allCaps = true
                    }.lparams {
                        gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                    }
                }

        viewGroup?.add(overlayView)

    }*/


    open fun onBluetoothStateUpdated(available: Boolean) {}

    open fun onNetworkStateUpdated(available: Boolean) {}

    open fun onLocationGPSStateUpdated(available: Boolean) {}

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingsHelper.setLocale(base))
    }

    protected fun validateEmail(emailInputLayout: TextInputLayout?, showDialog: Boolean, emailParam: EditText): Boolean {
        return validateEditText(emailInputLayout, emailParam, showDialog) { email ->
            when {
                email.isBlank() -> ValidationResult.INVALID_EMAIL_BLANK
                !(".+@.+".toRegex().matches(email)) -> ValidationResult.INVALID_EMAIL
                else -> ValidationResult.VALID
            }
        }
    }

    enum class ValidationResult(val messageResource: Int?) {
        VALID(null),
        INVALID_EMPTY_FIELD(R.string.edit_validation_blank_fields_exist),
        INVALID_OLD_PASSWORD(R.string.edit_user_validation_current_password_invalid),
        INVALID_CURRENT_USER_DATA(R.string.edit_user_validation_current_user_invalid),
        INVALID_PASSWORDS_DO_NOT_MATCH(R.string.edit_user_validation_passwords_do_not_match),
        INVALID_PASSWORDS_DO_NOT_MATCH_PASSWORD_RECOVERY(R.string.password_dont_match),
        INVALID_PASSWORD_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_PASSWORD_MIN_6_CHARACTERS(R.string.edit_user_validation_password_min_6_characters),
        INVALID_USERNAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_USERNAME_MIN_4_CHARACTERS(R.string.edit_user_validation_username_min_4_characters),
        INVALID_FIRST_NAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_LAST_NAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_STREET_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_STREET_NO_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_ZIP_CODE_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_CITY_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_COUNTRY_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_LOCKER_SETTINGS_NAME(R.string.locker_settings_error_name_empty_warning),
        INVALID_LOCKER_SETTINGS_ADDRESS(R.string.locker_settings_error_address_empty_warning),
        INVALID_EMAIL_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_EMAIL(R.string.message_email_invalid),
        INVALID_PHONE_NO_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_TERMS_AND_CONDITIONS_NOT_CHECKED(R.string.edit_user_validation_terms_and_conditions_not_checked),
        INVALID_PRIVACY_POLICY_NOT_CHECKED(R.string.edit_user_validation_terms_and_conditions_not_checked);

        fun getText(context: Context) = if (messageResource != null) {
            context.resources.getString(messageResource)
        } else {
            null
        }

        fun isValid() = this == VALID
    }

    protected fun validateSetError(emailInputLayout: TextInputLayout?, result: ValidationResult): ValidationResult {
        val errorText = if (!result.isValid()) result.getText(this) else null
        emailInputLayout?.error = errorText
        return result
    }

    fun validateEditText(emailInputLayout: TextInputLayout?, editText: EditText, showDialog: Boolean, validate: (value: String) -> ValidationResult): Boolean {
        val result = validate(editText.text.toString())
        validateSetError(emailInputLayout, result)

        val isValid = result.isValid()
        return isValid
    }


}