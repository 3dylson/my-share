package pt.ms.myshare.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pt.ms.myshare.data.repository.PlannerRepositoryImpl
import pt.ms.myshare.data.repository.SharedPreferencesEntitlementRepository
import pt.ms.myshare.data.repository.UserDataRepositoryImpl
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.GetDashboardDataUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.domain.use_case.edit_profile.EditProfileUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserDataRepository(@ApplicationContext context: Context): UserDataRepository = UserDataRepositoryImpl(context)

    @Provides
    @Singleton
    fun providePlannerRepository(@ApplicationContext context: Context): PlannerRepository = PlannerRepositoryImpl(context)

    @Provides
    fun provideGetDashboardDataUseCase(repository: UserDataRepository): GetDashboardDataUseCase = GetDashboardDataUseCase(repository)

    @Provides
    fun provideEditProfileUseCase(): EditProfileUseCase = EditProfileUseCase()

    @Provides
    @Singleton
    fun provideEntitlementRepository(@ApplicationContext context: Context): EntitlementRepository = SharedPreferencesEntitlementRepository(context)

    @Provides
    @Singleton
    fun provideCalculatePlanPreviewUseCase(): CalculatePlanPreviewUseCase = CalculatePlanPreviewUseCase()

    @Provides
    fun provideCreateReviewInsightUseCase(calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase): CreateReviewInsightUseCase =
        CreateReviewInsightUseCase(calculatePlanPreviewUseCase)

    @Provides
    fun provideResolvePricingStrategyUseCase(): ResolvePricingStrategyUseCase = ResolvePricingStrategyUseCase()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
