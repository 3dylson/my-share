package pt.ms.myshare.utils

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import pt.ms.myshare.ui.MainActivity

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

open class BaseFragment<viewBinding : ViewBinding>(private val inflate: Inflate<viewBinding>) :
    Fragment(), MenuProvider {

    private val toolbar: ActionBar?
        get() = (requireActivity() as AppCompatActivity).supportActionBar

    private val collapsingToolbar: CollapsingToolbarLayout
        get() = (requireActivity() as MainActivity).collapsingToolbar

    private val appBarLayout: AppBarLayout
        get() = (requireActivity() as MainActivity).appBar

    private val shouldResizeInputPan get() = (requireActivity() as MainActivity).shouldResize

    private var _binding: viewBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        val view = binding.root

        if (shouldResizeInputPan) {
            view.setOnApplyWindowInsetsListener { _, windowInsets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                    binding.root.setPadding(0, 0, 0, imeHeight)
                }
                windowInsets
            }
        }

        return view
    }


    open fun toolbarTitle(): String = StringUtils.EMPTY_STRING


    private fun setToolbarTitle(title: String) {
        collapsingToolbar.title = title
    }

    fun setupInputsLogic(textInputs: Array<EditText>, textInputsLabels: Array<TextView>, scrollView: View) {
        Utils.setScrollOnFocus(textInputs, textInputsLabels ,scrollView, appBarLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        setToolbarTitle(toolbarTitle())
        // override to add menu to the fragment
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}