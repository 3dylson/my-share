package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.presentation.ui.home.HomeState
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(): Flow<HomeState> {
        return userDataRepository.getUserData().map { userProfile ->
            val username = if (userProfile.username.isNotBlank()) userProfile.username else "there"
            HomeState(userName = username)
        }
    }
}
