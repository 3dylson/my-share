package pt.ms.myshare.utils

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    private val toolbar: ActionBar?
        get() = (requireActivity() as AppCompatActivity).supportActionBar

    open fun toolbarTitle(): String = StringUtils.EMPTY_STRING


    private fun setToolbarTitle(title: String) {
        toolbar?.title = title
    }

    override fun onStart() {
        super.onStart()
        if (toolbarTitle().isNotEmpty()) setToolbarTitle(toolbarTitle())
    }
}