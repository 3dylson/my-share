package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.ui.edit_profile.EditProfileState

interface UserDataRepository {
    fun getUserData(): Flow<EditProfileState>
    suspend fun saveUserData(data: EditProfileState)
}
