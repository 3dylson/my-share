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

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupToolbarScroll()
    }

    private fun getParentActivity(): MainActivity = requireActivity() as MainActivity

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
        appBarLayout.setExpanded(true)
        disableToolBarScrolling()
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
        Utils.disableToolbarScroll(collapsingToolbar)
    }

    private fun enableToolBarScrolling() {
        val params: AppBarLayout.LayoutParams =
            collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
        params.scrollFlags = (SCROLL_FLAG_SCROLL
                or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_SNAP)
        collapsingToolbar.layoutParams = params
    }

    /**
     * Sets the logic of inputs when clicked or the keyboard receives "IME_ACTION_NEXT"
     * so the scroll is done correctly.
     * @param textInputs
     * @param textInputsLabels
     * @param scrollView
     * @author @3dylson
     * */
    fun setupInputsLogic(
        textInputs: Array<EditText>,
        textInputsLabels: Array<TextView>,
        scrollView: View
    ) {
        Utils.setScrollOnFocus(textInputs, textInputsLabels, scrollView, appBarLayout)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
