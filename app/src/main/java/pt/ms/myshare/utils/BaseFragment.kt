package pt.ms.myshare.utils

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import pt.ms.myshare.MainActivity

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

open class BaseFragment<viewBinding : ViewBinding>(private val inflate: Inflate<viewBinding>) :
    Fragment(), MenuProvider {

    private val toolbar: ActionBar?
        get() = (requireActivity() as AppCompatActivity).supportActionBar

    private val collapsingToolbar: CollapsingToolbarLayout
        get() = (requireActivity() as MainActivity).collapsingToolbar

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


    open fun toolbarTitle(): String = StringUtils.EMPTY_STRING


    private fun setToolbarTitle(title: String) {
        collapsingToolbar.title = title
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