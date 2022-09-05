package pt.ms.myshare.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import pt.ms.myshare.R
import pt.ms.myshare.databinding.FragmentEditProfileBinding
import pt.ms.myshare.utils.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(FragmentEditProfileBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()

    }

    override fun getFragmentTAG(): String = "EditProfileFragment"

    override fun toolbarTitle(): String = getString(R.string.edit_profile_toolbar_title)


    private fun setupUI() {
        with(binding) {
            netSalary.addTextChangedListener(MoneyTextWatcher(netSalary))
            netSalaryPercentage.addTextChangedListener(PercentageTextWatcher(netSalaryPercentage))
            stockPercentage.addTextChangedListener(PercentageTextWatcher(stockPercentage))
            cryptoPercentage.addTextChangedListener(PercentageTextWatcher(cryptoPercentage))
            savingsPercentage.addTextChangedListener(PercentageTextWatcher(savingsPercentage))
            confirmButton.bottomBtn.setupEnableWithInputValidation(getScreenInputs()) { validateForm() }
            confirmButton.bottomBtn.text = resources.getString(R.string.btn_ep_confirm_text)

            setupInputsLogic(
                getScreenInputs(),
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

    private fun validateForm(): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(binding.netSalary.text.toString()) || StringUtils.parseCurrencyValue(
                binding.netSalary.text.toString(),
                NumberFormat.getCurrencyInstance(Locale.getDefault())
            ) == BigDecimal.ZERO
        ) return false

        if (Utils.isAnyInputEmpty(getScreenInputs())) isValid = false


        return isValid
    }

    private fun getScreenInputs(): Array<EditText> {
        return arrayOf(
            binding.netSalary,
            binding.netSalaryPercentage,
            binding.stockPercentage,
            binding.cryptoPercentage,
            binding.savingsPercentage
        )
    }

}