package pt.ms.myshare.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pt.ms.myshare.data.billing.BillingClientWrapper
import pt.ms.myshare.data.billing.PlayBillingEntitlementRepository
import pt.ms.myshare.data.repository.AuthRepositoryImpl
import pt.ms.myshare.data.repository.PlannerRepositoryImpl
import pt.ms.myshare.data.repository.UserDataRepositoryImpl
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CheckEntitlementLimitUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
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
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository = AuthRepositoryImpl(firebaseAuth)

    @Provides
    @Singleton
    fun providePlannerRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): PlannerRepository = PlannerRepositoryImpl(context, firebaseAuth, firestore)

    @Provides
    fun provideEditProfileUseCase(): EditProfileUseCase = EditProfileUseCase()

    @Provides
    @Singleton
    fun provideBillingClientWrapper(@ApplicationContext context: Context): BillingClientWrapper = BillingClientWrapper(context)

    @Provides
    @Singleton
    fun provideEntitlementRepository(billingClientWrapper: BillingClientWrapper): EntitlementRepository = 
        PlayBillingEntitlementRepository(billingClientWrapper)

    @Provides
    @Singleton
    fun provideCalculatePlanPreviewUseCase(): CalculatePlanPreviewUseCase = CalculatePlanPreviewUseCase()

    @Provides
    fun provideCreateReviewInsightUseCase(calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase): CreateReviewInsightUseCase =
        CreateReviewInsightUseCase(calculatePlanPreviewUseCase)

    @Provides
    fun provideResolvePricingStrategyUseCase(): ResolvePricingStrategyUseCase = ResolvePricingStrategyUseCase()

    @Provides
    fun provideCheckEntitlementLimitUseCase(entitlementRepository: EntitlementRepository): CheckEntitlementLimitUseCase = 
        CheckEntitlementLimitUseCase(entitlementRepository)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
