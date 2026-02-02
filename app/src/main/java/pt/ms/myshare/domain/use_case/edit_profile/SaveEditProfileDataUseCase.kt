package pt.ms.myshare.domain.use_case.edit_profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.presentation.ui.edit_profile.EditProfileState
import timber.log.Timber
import javax.inject.Inject

class SaveEditProfileDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(state: EditProfileState): Flow<EditProfileState> = flow {
        try {
            Timber.d("invoke() called with: $state")
            userDataRepository.saveUserData(
                state
            )
            emit(state.copy(isSaved = true, isLoading = false))
            Timber.d("invoke() success")
        } catch (e: Exception) {
            emit(state.copy(error = e.localizedMessage, isLoading = false))
            Timber.e(e, "invoke() failed")
        }
    }
}
