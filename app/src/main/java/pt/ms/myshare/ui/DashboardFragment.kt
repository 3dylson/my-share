package pt.ms.myshare.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import pt.ms.myshare.R
import pt.ms.myshare.data.InvestAmount
import pt.ms.myshare.databinding.FragmentDashboardBinding
import pt.ms.myshare.utils.BaseFragment
import pt.ms.myshare.utils.PreferenceUtils
import pt.ms.myshare.utils.StringUtils
import pt.ms.myshare.utils.TimeUtils
import java.time.LocalDate

/**
 * The default destination in the navigation.
 */
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val today = LocalDate.now()

    private lateinit var rvCategoryGrid: RecyclerView
    private lateinit var categoryAdapter: CategoryGridAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.tvToday.text = TimeUtils.monthDayAndYear(today)
        }
        rvCategoryGrid = binding.categoryGrid
        categoryAdapter = CategoryGridAdapter()
        rvCategoryGrid.adapter = categoryAdapter
        val adapter = rvCategoryGrid.adapter as CategoryGridAdapter


        adapter.submitList(
            arrayListOf(
                InvestAmount(
                    getString(R.string.stocks_label),
                    PreferenceUtils.getAmountToInvest(
                        requireContext(),
                        R.string.id_amount_for_stocks
                    )
                ),
                InvestAmount(
                    getString(R.string.crypto_label),
                    PreferenceUtils.getAmountToInvest(
                        requireContext(),
                        R.string.id_amount_for_crypto
                    )
                ),
                InvestAmount(
                    getString(R.string.savings_label),
                    PreferenceUtils.getAmountToInvest(
                        requireContext(),
                        R.string.id_amount_for_savings
                    )
                )
            )
        )

        /*binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        // Add menu items here
        menuInflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        return when (menuItem.itemId) {
            R.id.EditProfileFragment -> navToEditProfile()
            R.id.action_settings -> openSettings()
            else -> true
        }
    }

    private fun navToEditProfile(): Boolean {
        findNavController().navigate(R.id.action_DashboardFragment_to_editProfileFragment)
        return true
    }

    private fun openSettings(): Boolean {
        val intent = Intent(requireActivity(), SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    override fun toolbarTitle(): String {
        val username = PreferenceUtils.getUsername(requireContext(), R.string.id_username)
        var helloMsg = getString(R.string.hello_message)

        if (username != null && username.isNotBlank()) {
            helloMsg += StringUtils.SPACE
            helloMsg += username
        }

        return helloMsg
    }

}