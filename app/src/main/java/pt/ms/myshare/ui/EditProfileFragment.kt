package pt.ms.myshare.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import pt.ms.myshare.R
import pt.ms.myshare.databinding.FragmentEditProfileBinding
import pt.ms.myshare.utils.BaseFragment
import pt.ms.myshare.utils.InputUtils
import pt.ms.myshare.utils.PreferenceUtils
import pt.ms.myshare.utils.StringUtils
import pt.ms.myshare.utils.Utils
import pt.ms.myshare.utils.addResizeAnimation
import pt.ms.myshare.utils.hideBtnLoading
import pt.ms.myshare.utils.logs.FirebaseUtils
import pt.ms.myshare.utils.setupEnableWithInputValidation
import pt.ms.myshare.utils.showBtnLoading
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
        val percentageTextWatcher = PercentageTextWatcher(
            arrayOf(
                binding.stockPercentage,
                binding.cryptoPercentage,
                binding.savingsPercentage,
            ),
        )

        val incomePercentageTextWatcher = PercentageTextWatcher(
            arrayOf(
                binding.netSalaryPercentage,
            ),
        )

        with(binding) {
            netSalary.addTextChangedListener(MoneyTextWatcher(netSalary))
            netSalaryPercentage.addTextChangedListener(incomePercentageTextWatcher)
            stockPercentage.addTextChangedListener(percentageTextWatcher)
            cryptoPercentage.addTextChangedListener(percentageTextWatcher)
            savingsPercentage.addTextChangedListener(percentageTextWatcher)
            confirmButton.bottomBtn.setupEnableWithInputValidation(getScreenInputs(true)) { validateForm() }
            confirmButton.bottomBtn.addResizeAnimation()
            confirmButton.bottomBtn.text = resources.getString(R.string.btn_ep_confirm_text)

            setupInputsLogic(
                getScreenInputs(),
                getInputsLabel(),
                nestedScrollView,
            )

            confirmButton.bottomBtn.setOnClickListener {
                onConfirmClick(it)
            }

            incomePercentageTextWatcher.isBuildingView = false
            percentageTextWatcher.isBuildingView = false

            fillInputWithSavedData()
        }
    }

    private fun fillInputWithSavedData() {
        binding.username.setText(getUsername())
        InputUtils.getInputsData(getScreenInputs(), requireContext())
    }

    private fun getUsername() = PreferenceUtils.getUsername(requireContext(), R.string.id_username)

    private fun saveInputValues() {
        val context = requireContext()
        InputUtils.saveInputsData(getScreenInputs(), context)
        PreferenceUtils.setUsername(binding.username, context)
        val amountToInvest = getAmountToInvest()
        PreferenceUtils.setAmountToInvest(amountToInvest, context)
        PreferenceUtils.setAmountForStocks(getAmountForStocks(amountToInvest), context)
        PreferenceUtils.setAmountForCrypto(getAmountForCrypto(amountToInvest), context)
        PreferenceUtils.setAmountForSavings(getAmountForSavings(amountToInvest), context)
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
        var enableConfirmBtn: Boolean

        if (TextUtils.isEmpty(binding.netSalary.text.toString()) || StringUtils.parseCurrencyValue(
                binding.netSalary.text.toString(),
                NumberFormat.getCurrencyInstance(PreferenceUtils.getCurrency()),
            ) == BigDecimal.ZERO
        ) {
            return false
        }

        if (InputUtils.isAnyInputEmpty(getScreenInputs())) return false

        val usernameChanged = getUsername() != binding.username.text.toString().trim()

        enableConfirmBtn =
            usernameChanged || InputUtils.inputDataChanged(getScreenInputs(), requireContext())

        return enableConfirmBtn
    }

    private fun getScreenInputs(isUsernameIncluded: Boolean? = false): Array<EditText> {
        return if (isUsernameIncluded == true) {
            arrayOf(
                binding.netSalary,
                binding.netSalaryPercentage,
                binding.stockPercentage,
                binding.cryptoPercentage,
                binding.savingsPercentage,
                binding.username,
            )
        } else {
            arrayOf(
                binding.netSalary,
                binding.netSalaryPercentage,
                binding.stockPercentage,
                binding.cryptoPercentage,
                binding.savingsPercentage,
            )
        }
    }

    private fun getInputsLabel(): Array<TextView> {
        return arrayOf(
            binding.netSalaryLabel,
            binding.netSalaryPercentageLabel,
            binding.stockPercentageLabel,
            binding.cryptoPercentageLabel,
            binding.savingsPercentageLabel,
        )
    }

    private fun onConfirmClick(button: View) {
        InputUtils.hideKeyboard(requireView())
        binding.confirmButton.root.showBtnLoading()
        saveInputValues()
        binding.confirmButton.root.hideBtnLoading(getString(R.string.btn_ep_confirm_text))
        Snackbar.make(
            button,
            getString(R.string.snackbar_saved_text),
            Snackbar.LENGTH_SHORT,
        ).setAnchorView(button).show()
        FirebaseUtils.logButtonClickEvent(
            binding.confirmButton.bottomBtn.text.toString(),
            getFragmentTAG(),
        )
    }
}
