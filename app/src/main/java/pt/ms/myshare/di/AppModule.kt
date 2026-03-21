package pt.ms.myshare.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pt.ms.myshare.data.repository.UserDataRepositoryImpl
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.InMemoryEntitlementRepository
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.GetDashboardDataUseCase
import pt.ms.myshare.domain.use_case.edit_profile.EditProfileUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserDataRepository(@ApplicationContext context: Context): UserDataRepository {
        return UserDataRepositoryImpl(context)
    }

    @Provides
    fun provideGetDashboardDataUseCase(repository: UserDataRepository): GetDashboardDataUseCase {
        return GetDashboardDataUseCase(repository)
    }

    @Provides
    fun provideEditProfileUseCase(): EditProfileUseCase {
        return EditProfileUseCase()
    }

    @Provides
    @Singleton
    fun provideEntitlementRepository(): EntitlementRepository {
        // TODO: Replace with Play Billing / RevenueCat-backed implementation.
        return InMemoryEntitlementRepository()
    }

    @Provides
    fun provideCalculatePlanPreviewUseCase(): CalculatePlanPreviewUseCase {
        return CalculatePlanPreviewUseCase()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
