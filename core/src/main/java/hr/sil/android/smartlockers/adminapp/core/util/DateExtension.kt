package hr.sil.android.smartlockers.adminapp.core.util

import java.text.SimpleDateFormat
import java.util.*



/**
 * Pattern: dd/MM/yyyy HH:mm:ss
 */
fun Date.formatToViewDateTimeDefaults(): String{
    val sdf= SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(this)
}

/**
 * Pattern: dd/MM/yyyy HH:mm:ss
 */
fun String.formatFromStringToDate(): Date{
    val sdf= SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return sdf.parse(this)
}

/**
 * Add field date to current date
 */
fun Date.add(field: Int, amount: Int): Date{
    val cal = Calendar.getInstance()
    cal.time=this
    cal.add(field, amount)

    this.time = cal.time.time

    cal.clear()

    return this
}

