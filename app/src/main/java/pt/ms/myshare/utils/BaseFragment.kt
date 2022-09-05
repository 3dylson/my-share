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
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout
import pt.ms.myshare.ui.MainActivity

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

open class BaseFragment<viewBinding : ViewBinding>(private val inflate: Inflate<viewBinding>) :
    Fragment(), MenuProvider {

    private val toolbar: ActionBar?
        get() = (requireActivity() as AppCompatActivity).supportActionBar

    private val collapsingToolbar: CollapsingToolbarLayout
        get() = getParentActivity().collapsingToolbar

    private val appBarLayout: AppBarLayout
        get() = getParentActivity().appBar

    private val shouldResizeInputPan get() = getParentActivity().shouldResize

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
                    view.setPadding(0, 0, 0, imeHeight)
                }
                windowInsets
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        setupToolbarScroll()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        setToolbarTitle(toolbarTitle())
        // override to add menu to the fragment
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    open fun getFragmentTAG(): String = "BaseFragment"

    open fun toolbarTitle(): String = StringUtils.EMPTY_STRING

    private fun setToolbarTitle(title: String) {
        collapsingToolbar.title = title
    }


    private fun setupToolbarScroll() {
        val view = binding.root

        if (view is NestedScrollView) {
            // TODO Call requires API level 23 (current min is 21):
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                    val scrollUp = scrollY <= oldScrollY
                    val scrollDown = scrollY >= oldScrollY

                    if (scrollUp || scrollDown) enableToolBarScrolling()
                    else disableToolBarScrolling()
                }
            }
        }
        /* view.viewTreeObserver
             .addOnScrollChangedListener {
                 Log.d(getFragmentTAG(), "Scroll")

             }*/
    }

    private fun disableToolBarScrolling() {
        val params: AppBarLayout.LayoutParams =
            collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
        params.scrollFlags = (SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_SNAP)
        collapsingToolbar.layoutParams = params
    }

    private fun enableToolBarScrolling() {
        val params: AppBarLayout.LayoutParams =
            collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
        params.scrollFlags = (SCROLL_FLAG_SCROLL
                or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_SNAP)
        collapsingToolbar.layoutParams = params
    }

    fun setupInputsLogic(
        textInputs: Array<EditText>,
        textInputsLabels: Array<TextView>,
        scrollView: View
    ) {
        Utils.setScrollOnFocus(textInputs, textInputsLabels, scrollView, appBarLayout)
    }

    private fun getParentActivity(): MainActivity = requireActivity() as MainActivity

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
