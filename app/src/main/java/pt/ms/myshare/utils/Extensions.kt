package pt.ms.myshare.utils

import android.content.Context
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import pt.ms.myshare.utils.insetsCallBack.InsetsWithKeyboardAnimationCallback

fun Button.setupEnableWithInputValidation(
    textInputs: Array<EditText>,
    validateForm: () -> Boolean
) {
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            /* Not used */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            this@setupEnableWithInputValidation.isEnabled = validateForm()
        }

        override fun afterTextChanged(p0: Editable?) {
            /* Not used */
        }

    }

    textInputs.forEach {
        it.addTextChangedListener(textWatcher)
    }


}

fun Button.addResizeAnimation() {
    val insetsWithKeyboardAnimationCallback = InsetsWithKeyboardAnimationCallback(this)
    ViewCompat.setWindowInsetsAnimationCallback(this, insetsWithKeyboardAnimationCallback)
}

fun RelativeLayout.showBtnLoading() {
    val button: Button = getChildAt(0) as Button
    val progressBar: ProgressBar = getChildAt(1) as ProgressBar
    button.isEnabled = false
    button.text = StringUtils.EMPTY_STRING
    progressBar.visibility = View.VISIBLE
}

fun RelativeLayout.hideBtnLoading(buttonLabel: String) {
    val button: Button = getChildAt(0) as Button
    val progressBar: ProgressBar = getChildAt(1) as ProgressBar
    progressBar.visibility = View.GONE
    button.text = buttonLabel
    //button.isEnabled = true
}

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}