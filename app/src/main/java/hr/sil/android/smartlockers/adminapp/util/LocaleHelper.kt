package hr.sil.android.smartlockers.adminapp.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import hr.sil.android.smartlockers.adminapp.core.util.logger

import java.util.*


class LocaleHelper {


    companion object {
        private val log = logger()
        //Global function for setting language for app
        fun setLocale(context: Context?, language: String?): Context? {
            if (context != null) {
                log.info("Setting localisation to $language")
                persist(context, language)
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    updateResources(context, language)
                } else updateResourcesLegacy(context, language)
            }
            return null
        }

        fun onAttach(context: Context): Context? {
            val lang = getPersistedData(context, Locale.getDefault().language)
            return setLocale(context, lang)
        }

        fun getPersistedData(context: Context, defaultLanguage: String): String? {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(SELECTED_LANGUAGE, defaultLanguage)
        }

        private fun persist(context: Context, language: String?) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.putString(SELECTED_LANGUAGE, language)
            editor.apply()
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun updateResources(context: Context, language: String?): Context {
            val res = context.resources
            val conf = res.configuration
            val locale = Locale(language)
            Locale.setDefault(locale)
            conf.setLocale(locale)
            return context.createConfigurationContext(conf)

        }

        private fun updateResourcesLegacy(context: Context, language: String?): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val resources = context.resources
            val configuration = resources.configuration
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            return context
        }

        private val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    }


}