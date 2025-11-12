package hr.sil.android.smartlockers.adminapp.view.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import hr.sil.android.smartlockers.adminapp.R
import hr.sil.android.smartlockers.adminapp.view.activity.BaseActivity.ValidationResult
import kotlinx.coroutines.*

open class BaseFragment : Fragment() {

    fun EditText.afterTextChangeDelay(duration: Long, run: (String) -> Unit) {
        var job: Job? = null
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                job?.cancel()
                job = GlobalScope.launch(Dispatchers.Main) {
                    try {
                        delay(duration)
                        run.invoke(this@afterTextChangeDelay.text.toString())
                    } catch (e: Exception) {
                        //ignore
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }


    protected fun validateSetError(
        emailInputLayout: TextInputLayout?,
        result: ValidationResult
    ): ValidationResult {
        val errorText = if (!result.isValid()) result.getText(requireContext()) else null
        emailInputLayout?.error = errorText
        return result
    }

    fun validateEditText(
        emailInputLayout: TextInputLayout?,
        editText: EditText,
        validate: (value: String) -> ValidationResult
    ): Boolean {
        val result = validate(editText.text.toString())
        validateSetError(emailInputLayout, result)

        val isValid = result.isValid()
        return isValid
    }

    protected fun validateEmail(emailInputLayout: TextInputLayout?, emailParam: EditText): Boolean {
        return validateEditText(emailInputLayout, emailParam) { email ->
            when {
                email.isBlank() -> ValidationResult.INVALID_EMAIL_BLANK
                !(".+@.+".toRegex().matches(email)) -> ValidationResult.INVALID_EMAIL
                else -> ValidationResult.VALID
            }
        }
    }

    fun initializeToolbarUIMainActivity(
        displayToolbarArrow: Boolean = false,
        toolbarTitleText: String = "",
        insideSettingsScreen: Boolean,
        insideManageUserDetailsScreen: Boolean,
        context: Context
    ) {

        val ivToolbarLogo: ImageView? = this.activity?.findViewById(R.id.ivToolbarLogo)
        val toolbar: Toolbar? = this.activity?.findViewById(R.id.toolbarMain)
        val toolbarTitle: TextView? = this.activity?.findViewById(R.id.toolbar_title)
        val ivLogout: ImageView? = this.activity?.findViewById(R.id.ivLogout)

        if (insideSettingsScreen || insideManageUserDetailsScreen) {
            ivLogout?.visibility = View.VISIBLE
            val toolbarRightCorneImage = if (insideManageUserDetailsScreen) {
                getDrawableAttrValue(R.attr.thmManageUserDeleteUserImage, context)
            } else {
                getDrawableAttrValue(R.attr.thmToolbarLogoutImage, context)
            }
            if (toolbarRightCorneImage != null)
                ivLogout?.setImageDrawable(toolbarRightCorneImage)
        } else {
            ivLogout?.visibility = View.GONE
        }

        if (displayToolbarArrow) {
            ivToolbarLogo?.visibility = View.GONE

            toolbarTitle?.visibility = View.VISIBLE
            toolbarTitle?.text = toolbarTitleText

            if (insideSettingsScreen || insideManageUserDetailsScreen)
                toolbar?.setPadding(toolbar.paddingLeft, toolbar.paddingTop, toolbar.paddingRight, toolbar.paddingBottom)
            else
                toolbar?.setPadding( 0, toolbar.paddingTop, toolbar.paddingRight, toolbar.paddingBottom)
        } else {
            ivToolbarLogo?.visibility = View.VISIBLE
            toolbarTitle?.visibility = View.GONE

            toolbar?.setPadding(
                toolbar.paddingRight,
                toolbar.paddingTop,
                toolbar.paddingRight,
                toolbar.paddingBottom
            )
        }
    }

    private fun getDrawableAttrValue(attr: Int, context: Context): Drawable? {
        val attrArray = intArrayOf(attr)
        val typedArray = context.obtainStyledAttributes(attrArray)
        val result = try {
            typedArray.getDrawable(0)
        } catch (exc: Exception) {
            null
        }
        typedArray.recycle()
        return result
    }

    fun initializeToolbarUILoginActivity(
        displayToolbarArrow: Boolean = false,
        toolbarTitleText: String = ""
    ) {

        val toolbar: Toolbar? = this.activity?.findViewById(R.id.toolbar)
        val toolbarTitle: TextView? = this.activity?.findViewById(R.id.toolbar_title)

        toolbarTitle?.text = toolbarTitleText

        if( displayToolbarArrow )
            toolbar?.setPadding(0, toolbar.paddingTop, toolbar.paddingRight, toolbar.paddingBottom);
        else
            toolbar?.setPadding(toolbar.paddingRight, toolbar.paddingTop, toolbar.paddingRight, toolbar.paddingBottom)
    }

}