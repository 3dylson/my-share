package pt.ms.myshare.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import pt.ms.myshare.R
import pt.ms.myshare.databinding.FragmentEditProfileBinding
import pt.ms.myshare.utils.*
import pt.ms.myshare.utils.textWatcher.MoneyTextWatcher
import pt.ms.myshare.utils.textWatcher.PercentageTextWatcher
import java.math.BigDecimal
import java.text.NumberFormat

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
            confirmButton.bottomBtn.addResizeAnimation()
            confirmButton.bottomBtn.text = resources.getString(R.string.btn_ep_confirm_text)

            setupInputsLogic(
                getScreenInputs(),
                getInputsLabel(),
                nestedScrollView
            )

            confirmButton.bottomBtn.setOnClickListener {
                confirmButton.root.showBtnLoading()
                saveInputValues()
                confirmButton.root.hideBtnLoading(resources.getString(R.string.btn_ep_confirm_text))
                Snackbar.make(it, getString(R.string.snackbar_saved_text), Snackbar.LENGTH_SHORT)
                    .setAnchorView(it).show()
            }

            fillInputWithSavedData()

        }
    }

    private fun fillInputWithSavedData() {
        InputUtils.getInputsData(getScreenInputs(), requireContext())
    }

    private fun saveInputValues() {
        InputUtils.saveInputsData(getScreenInputs(), requireContext())
        val amountToInvest = getAmountToInvest()
        InputUtils.saveAmountToInvest(amountToInvest, requireContext())
        InputUtils.saveAmountForStocks(getAmountForStocks(amountToInvest), requireContext())
        InputUtils.saveAmountForCrypto(getAmountForCrypto(amountToInvest), requireContext())
        InputUtils.saveAmountForSavings(getAmountForSavings(amountToInvest), requireContext())
    }

    private fun getAmountForSavings(amountToInvest: Int): Int {
        val percentage =
            StringUtils.parsePercentageValue(binding.savingsPercentage.text.toString()).toFloat()
        return Utils.getPercentOfNumber(amountToInvest, percentage)
    }

    private fun getAmountForCrypto(amountToInvest: Int): Int {
        val percentage =
            StringUtils.parsePercentageValue(binding.cryptoPercentage.text.toString()).toFloat()
        return Utils.getPercentOfNumber(amountToInvest, percentage)
    }

    private fun getAmountForStocks(amountToInvest: Int): Int {
        val percentage =
            StringUtils.parsePercentageValue(binding.stockPercentage.text.toString()).toFloat()
        return Utils.getPercentOfNumber(amountToInvest, percentage)
    }

    private fun getAmountToInvest(): Int {
        val value = StringUtils.getRawInputText(binding.netSalary.text.toString()).toInt()
        val percentage =
            StringUtils.parsePercentageValue(binding.netSalaryPercentage.text.toString()).toFloat()
        return Utils.getPercentOfNumber(value, percentage)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(binding.netSalary.text.toString()) || StringUtils.parseCurrencyValue(
                binding.netSalary.text.toString(),
                NumberFormat.getCurrencyInstance(PreferenceUtils.getCurrency())
            ) == BigDecimal.ZERO
        ) return false

        if (InputUtils.isAnyInputEmpty(getScreenInputs())) return false

        isValid = InputUtils.inputDataChanged(getScreenInputs(), requireContext())

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

    private fun getInputsLabel(): Array<TextView> {
        return arrayOf(
            binding.netSalaryLabel,
            binding.netSalaryPercentageLabel,
            binding.stockPercentageLabel,
            binding.cryptoPercentageLabel,
            binding.savingsPercentageLabel
        )
    }

}