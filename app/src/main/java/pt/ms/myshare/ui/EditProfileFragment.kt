package pt.ms.myshare.ui

import android.os.Bundle
import android.view.View
import pt.ms.myshare.R
import pt.ms.myshare.databinding.FragmentEditProfileBinding
import pt.ms.myshare.utils.BaseFragment
import pt.ms.myshare.utils.MoneyTextWatcher
import pt.ms.myshare.utils.PercentageTextWatcher

class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(FragmentEditProfileBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()

    }

    private fun setupUI() {
        with(binding) {
            netSalary.addTextChangedListener(MoneyTextWatcher(netSalary))
            netSalaryPercentage.addTextChangedListener(PercentageTextWatcher(netSalaryPercentage))
            stockPercentage.addTextChangedListener(PercentageTextWatcher(stockPercentage))
            cryptoPercentage.addTextChangedListener(PercentageTextWatcher(cryptoPercentage))
            savingsPercentage.addTextChangedListener(PercentageTextWatcher(savingsPercentage))

            setupInputsLogic(
                arrayOf(
                    netSalary,
                    netSalaryPercentage,
                    stockPercentage,
                    cryptoPercentage,
                    savingsPercentage
                ),
                arrayOf(
                    netSalaryLabel,
                    netSalaryPercentageLabel,
                    stockPercentageLabel,
                    cryptoPercentageLabel,
                    savingsPercentageLabel
                ),
                nestedScrollView
            )
        }
    }

    override fun toolbarTitle(): String = getString(R.string.edit_profile_toolbar_title)


}