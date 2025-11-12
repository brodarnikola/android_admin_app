package hr.sil.android.smartlockers.adminapp.fonts

import android.graphics.Typeface
import hr.sil.android.smartlockers.adminapp.App
import java.util.concurrent.ConcurrentHashMap

object AppFonts {

    enum class FontName(val attr: Int, val path: String, val ext: String) {
        UNKNOWN(-1, "", ""),
        METROPOLIS(1,"metropolis", "otf"),
        RAJDHANI(2,"rajdhani", "ttf"),
        MONTSERRAT(3,"montserrat", "ttf"),
        BARLOW(4,"barlow", "ttf");

        companion object {
            fun getByAttr(attr: Int?) = values().firstOrNull { it.attr == attr } ?: UNKNOWN
            fun getByPath(path: String?) = values().firstOrNull { it.path == path } ?: UNKNOWN
        }
    }

    enum class FontType(val attr: Int, val path: String) {
        UNKNOWN(-1, ""),
        REGULAR(1,"regular"),
        MEDIUM(2, "medium"),
        BOLD(3, "bold");

        companion object {
            fun getByAttr(attr: Int?) = values().firstOrNull { it.attr == attr } ?: UNKNOWN
            fun getByPath(path: String?) = values().firstOrNull { it.path == path } ?: UNKNOWN
        }
    }

    private val fontCache = ConcurrentHashMap<String, Typeface>()

    fun getFont(name: FontName, type: FontType): Typeface? {
        if (name == AppFonts.FontName.UNKNOWN) return null
        val cleanType = if (type != AppFonts.FontType.UNKNOWN) type else AppFonts.FontType.REGULAR

        val path = "fonts/${name.path}-${cleanType.path}.${name.ext}"
        return fontCache.getOrPut(path) { Typeface.createFromAsset(App.ref.assets, path) }
    }

    fun getFontByAttr(attrName: String, attrType: String): Typeface? {
        val attrNameInt = attrName.toIntOrNull()
        val attrTypeInt = attrType.toIntOrNull()

        val name =
            if (attrNameInt != null) AppFonts.FontName.getByAttr(attrNameInt)
            else AppFonts.FontName.getByPath(attrName)

        val type =
            if (attrTypeInt != null) AppFonts.FontType.getByAttr(attrTypeInt)
            else AppFonts.FontType.getByPath(attrType)

        return getFont(name, type)
    }
}