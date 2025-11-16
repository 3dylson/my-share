package pt.ms.myshare.ui.edit_profile

data class EditProfileState(
    val username: String = "",
    val netSalary: String = "",
    val netSalaryPercentage: String = "",
    val stockPercentage: String = "",
    val cryptoPercentage: String = "",
    val savingsPercentage: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
