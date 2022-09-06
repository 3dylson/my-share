package pt.ms.myshare.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
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