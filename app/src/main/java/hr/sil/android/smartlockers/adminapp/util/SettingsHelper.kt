package hr.sil.android.smartlockers.adminapp.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Build.VERSION_CODES.*
import java.util.*

object SettingsHelper {

    private const val NAME = "SettingsAdmin"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences


    // list of app specific preferences

    val LANGUAGE_GERMAN = "DE"
    private val SELECTED_LANGUAGE = Pair("User_settings_Language", LANGUAGE_GERMAN)
    private val USERNAME_LOGIN = Pair("Username_login", "")

    private val USER_PASSWORD = Pair("User_password_without_encryption", "")

    private val DID_USER_REGISTER_OR_LOGIN = Pair("User_register_or_login", false)


    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }


    fun setLocale(c: Context): Context {
        return updateResources(c, getLanguage())
    }


    fun getLanguage(): String {
        return preferences.getString(SELECTED_LANGUAGE.first, LANGUAGE_GERMAN).toString()
    }

    private fun updateResources(context: Context, language: String): Context {
        var ctx = context
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        if(  Build.VERSION.SDK_INT >= O) {
            config.setLocale(locale)
            ctx = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return ctx
    }


    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var languageName: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(SELECTED_LANGUAGE.first, SELECTED_LANGUAGE.second).toString()

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(SELECTED_LANGUAGE.first, value)
        }

    var usernameLogin: String?
        get() = preferences.getString(USERNAME_LOGIN.first, USERNAME_LOGIN.second)

        set(value) = preferences.edit {
            it.putString(USERNAME_LOGIN.first, value)
        }

    var userPasswordWithoutEncryption: String
        get() = preferences.getString(USER_PASSWORD.first, USER_PASSWORD.second) ?: ""

        set(value) = preferences.edit {
            it.putString(USER_PASSWORD.first, value)
        }

    var userRegisterOrLogin: Boolean
        get() = preferences.getBoolean(DID_USER_REGISTER_OR_LOGIN.first, DID_USER_REGISTER_OR_LOGIN.second)

        set(value) = preferences.edit {
            it.putBoolean(DID_USER_REGISTER_OR_LOGIN.first, value)
        }

}