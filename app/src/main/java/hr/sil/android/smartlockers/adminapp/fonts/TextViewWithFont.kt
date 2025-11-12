package hr.sil.android.smartlockers.adminapp.fonts.view.fonts

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.fonts.AppFonts

/**
 * @author mfatiga
 */
internal class TextViewWithFont : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val attr = context.obtainStyledAttributes(attrs, R.styleable.TextViewWithFont, defStyle, 0)
        val attrName = attr.getString(R.styleable.TextViewWithFont_font_name) ?: ""
        val attrType = attr.getString(R.styleable.TextViewWithFont_font_type) ?: ""
        attr.recycle()

        val font = AppFonts.getFontByAttr(attrName, attrType)
        if (font != null) typeface = font
    }
}