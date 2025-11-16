package pt.ms.myshare.domain.use_case.edit_profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.presentation.ui.edit_profile.EditProfileState
import javax.inject.Inject

class GetEditProfileDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(): Flow<EditProfileState> {
        return userDataRepository.getUserData().map { userProfile ->
            EditProfileState(
                netSalary = userProfile.netSalary,
                netSalaryPercentage = userProfile.netSalaryPercentage,
                stockPercentage = userProfile.stockPercentage,
                cryptoPercentage = userProfile.cryptoPercentage,
                savingsPercentage = userProfile.savingsPercentage
            )
        }
    }
}
