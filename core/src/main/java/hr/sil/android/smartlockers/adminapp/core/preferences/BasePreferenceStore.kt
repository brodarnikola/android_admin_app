/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.smartlockers.adminapp.core.preferences

import android.content.Context
import android.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author mfatiga
 */
abstract class BasePreferenceStore(private val appContext: () -> Context) {
    companion object {
        private const val PREF_KEY_PREFIX = "Store.Pref.Key."
    }
    private fun getKey(propertyName: String) = "$PREF_KEY_PREFIX$propertyName"

    protected fun preference(defaultValue: String?) = object : ReadWriteProperty<Any?, String?> {
        @Volatile private var isLoaded = false
        private var value: String? = defaultValue

        private fun persist(key: String, value: String?) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(appContext())
            val editor = preferences.edit()
            if (value != null) editor.putString(key, value)
            else editor.remove(key)
            editor.apply()
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
            if (!isLoaded) {
                isLoaded = true
                val preferences = PreferenceManager.getDefaultSharedPreferences(appContext())
                value = preferences.getString(getKey(property.name), defaultValue)
            }
            return value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            this.value = value
            persist(getKey(property.name), value)
        }
    }
}