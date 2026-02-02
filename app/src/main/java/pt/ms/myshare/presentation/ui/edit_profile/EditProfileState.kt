package pt.ms.myshare.presentation.ui.edit_profile

data class EditProfileState(
    val netSalary: String = "",
    val netSalaryPercentage: String = "",
    val stockPercentage: String = "",
    val cryptoPercentage: String = "",
    val savingsPercentage: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val netSalaryPercentageError: String? = null,
    val stockPercentageError: String? = null,
    val cryptoPercentageError: String? = null,
    val savingsPercentageError: String? = null
)
