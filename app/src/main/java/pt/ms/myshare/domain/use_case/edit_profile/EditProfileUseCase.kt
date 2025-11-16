package pt.ms.myshare.domain.use_case.edit_profile

import pt.ms.myshare.presentation.ui.edit_profile.EditProfileState

class EditProfileUseCase {
    fun validate(state: EditProfileState): ValidationResult {
        if (state.netSalary.isBlank()) {
            return ValidationResult(false, "Net salary can't be empty")
        }
        if (state.netSalaryPercentage.isBlank()) {
            return ValidationResult(false, "Net salary percentage can't be empty")
        }
        if (state.stockPercentage.isBlank()) {
            return ValidationResult(false, "Stock percentage can't be empty")
        }
        if (state.cryptoPercentage.isBlank()) {
            return ValidationResult(false, "Crypto percentage can't be empty")
        }
        if (state.savingsPercentage.isBlank()) {
            return ValidationResult(false, "Savings percentage can't be empty")
        }

        val netSalary = state.netSalary.toFloatOrNull()
        if (netSalary == null || netSalary <= 0) {
            return ValidationResult(false, "Invalid net salary")
        }

        val netSalaryPercentage = state.netSalaryPercentage.toFloatOrNull()
        if (netSalaryPercentage == null || netSalaryPercentage <= 0) {
            return ValidationResult(false, "Invalid net salary percentage")
        }

        val stockPercentage = state.stockPercentage.toFloatOrNull()
        if (stockPercentage == null || stockPercentage < 0) {
            return ValidationResult(false, "Invalid stock percentage")
        }

        val cryptoPercentage = state.cryptoPercentage.toFloatOrNull()
        if (cryptoPercentage == null || cryptoPercentage < 0) {
            return ValidationResult(false, "Invalid crypto percentage")
        }

        val savingsPercentage = state.savingsPercentage.toFloatOrNull()
        if (savingsPercentage == null || savingsPercentage < 0) {
            return ValidationResult(false, "Invalid savings percentage")
        }

        val totalPercentage = stockPercentage + cryptoPercentage + savingsPercentage
        if (totalPercentage != 100f) {
            return ValidationResult(false, "The sum of percentages must be 100")
        }

        return ValidationResult(true)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

}
