package hr.sil.android.smartlockers.adminapp.fonts

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RadioButton
import hr.sil.android.smartlockers.adminapp.R

internal class RadioButtonWithFont : RadioButton {

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
        val attr = context.obtainStyledAttributes(attrs, R.styleable.RadioButtonWithFont, defStyle, 0)
        val attrName = attr.getString(R.styleable.RadioButtonWithFont_font_name) ?: ""
        val attrType = attr.getString(R.styleable.RadioButtonWithFont_font_type) ?: ""
        attr.recycle()

        val font = AppFonts.getFontByAttr(attrName, attrType)
        if (font != null) typeface = font
    }
}


