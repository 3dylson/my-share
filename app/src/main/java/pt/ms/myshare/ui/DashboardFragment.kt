package pt.ms.myshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import pt.ms.myshare.R
import pt.ms.myshare.data.InvestAmount
import pt.ms.myshare.databinding.FragmentDashboardBinding
import pt.ms.myshare.utils.BaseFragment
import pt.ms.myshare.utils.PreferenceUtils
import pt.ms.myshare.utils.StringUtils
import pt.ms.myshare.utils.TimeUtils
import pt.ms.myshare.utils.logs.FirebaseUtils
import java.time.LocalDate

/**
 * The default destination in the navigation.
 */
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    private val today = LocalDate.now()

    private lateinit var rvCategoryGrid: RecyclerView
    private lateinit var categoryAdapter: CategoryGridAdapter

    override fun getFragmentTAG(): String = "DashboardFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adView: AdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        binding.tvToday.text = TimeUtils.monthDayAndYear(today)

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
                        R.string.id_amount_for_stocks,
                    ),
                    R.drawable.ic_baseline_show_chart,
                ),
                InvestAmount(
                    getString(R.string.crypto_label),
                    PreferenceUtils.getAmountToInvest(
                        requireContext(),
                        R.string.id_amount_for_crypto,
                    ),
                    R.drawable.ic_baseline_currency_bitcoin,
                ),
                InvestAmount(
                    getString(R.string.savings_label),
                    PreferenceUtils.getAmountToInvest(
                        requireContext(),
                        R.string.id_amount_for_savings,
                    ),
                    R.drawable.savings_48px,
                ),
            ),
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        // Add menu items here
        menuInflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        return when (menuItem.itemId) {
            R.id.EditProfileFragment -> {
                FirebaseUtils.logButtonClickEvent(
                    getString(R.string.edit_profile_toolbar_title),
                    getFragmentTAG(),
                )
                navToEditProfile()
            }
            // R.id.action_settings -> openSettings()
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
        val dashboardMsg = getString(R.string.dashboard_message)

        helloMsg += if (!username.isNullOrBlank()) {
            StringUtils.SPACE + username + StringUtils.COMMA + StringUtils.PARAGRAPH + dashboardMsg
        } else {
            StringUtils.COMMA + StringUtils.PARAGRAPH + dashboardMsg
        }

        return helloMsg
    }
}
