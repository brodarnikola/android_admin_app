package hr.sil.android.smartlockers.adminapp.compose.view.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.util.UidVerifier
import hr.sil.android.smartlockers.adminapp.compose.view.ui.utils.BaseViewModel
import hr.sil.android.smartlockers.adminapp.core.util.logger
import hr.sil.android.smartlockers.adminapp.util.SettingsHelper
import hr.sil.android.smartlockers.adminapp.util.backend.UserUtil
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.isBlank

import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.compose.view.ui.utils.UiEvent
import hr.sil.android.smartlockers.adminapp.view.activity.LoginActivity
import hr.sil.android.smartlockers.adminapp.view.activity.MainActivity

class LoginViewModel : BaseViewModel<LoginScreenUiState, LoginScreenEvent>() {

    val log = logger()

    override fun initialState(): LoginScreenUiState {
        return LoginScreenUiState()
    }

    init {
        log.info("collecting event: start new viewmodel")
    }

    override fun onEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.OnLogin -> {

                viewModelScope.launch {
                    _state.update { it.copy(loading = true) }

                    if (UserUtil.login(
                            event.email,
                            event.password
                        )
                    ) {
                        SettingsHelper.userPasswordWithoutEncryption = event.password.trim()
                        SettingsHelper.userRegisterOrLogin = true

                        sendUiEvent(LoginScreenUiEvent.NavigateToMainActivityScreen)

                    } else {
                        sendUiEvent(
                            UiEvent.ShowToast(
                                event.context.getString(R.string.wrong_username_or_password)
                            )
                        )
                        sendUiEvent(
                            UiEvent.ShowToast(
                                "Email and password don't match, or your account has been disabled.",
                                Toast.LENGTH_SHORT
                            )
                        )
                    }
                }
            }
        }
    }

    fun getEmailError(email: String, context: Context): String {
//        var emailError = ""
//        if (email.isBlank()) {
//            emailError = context.getString(R.string.forgot_password_error)
//        } else if (!email.isEmailValid()) {
//            emailError = context.getString(R.string.pickup_parcel_email_error)
//        }
//
//        return emailError
        return ""
    }

    fun getPasswordError(password: String, context: Context): String {
        var passwordError = ""
        if (password.isBlank()) {
            //passwordError = context.getString(R.string.settings_password_error)
        }

        return passwordError
    }

    //fun getUserEmail(): String = sharedPrefsStorage.getUserEmail()
}

data class LoginScreenUiState(
    val loading: Boolean = false
)

sealed interface LoginScreenEvent {
    data class OnLogin(
        val email: String,
        val password: String,
        val context: Context,
        val activity: Activity
    ) : LoginScreenEvent
}

sealed class LoginScreenUiEvent : UiEvent {

    object NavigateToMainActivityScreen : LoginScreenUiEvent()
}