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
import com.google.firebase.functions.FirebaseFunctions
import pt.ms.myshare.data.auth.AndroidCredentialStateClearer
import pt.ms.myshare.data.auth.CredentialStateClearer
import pt.ms.myshare.data.billing.BillingAuthSession
import pt.ms.myshare.data.billing.BillingClientWrapper
import pt.ms.myshare.data.billing.FirebaseBillingAuthSession
import pt.ms.myshare.data.billing.PlayBillingEntitlementRepository
import pt.ms.myshare.data.repository.FirestoreAppUpdatePolicyRepository
import pt.ms.myshare.data.repository.AuthRepositoryImpl
import pt.ms.myshare.data.repository.PlannerRepositoryImpl
import pt.ms.myshare.data.repository.SharedUserPreferencesRepository
import pt.ms.myshare.domain.repository.AppUpdatePolicyRepository
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CheckEntitlementLimitUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.domain.use_case.ResolveAppUpdateDecisionUseCase
import pt.ms.myshare.domain.use_case.ResolveAllocationStrategyRulesUseCase
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance()

    @Provides
    @Singleton
    fun provideCredentialStateClearer(
        @ApplicationContext context: Context
    ): CredentialStateClearer = AndroidCredentialStateClearer(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        credentialStateClearer: CredentialStateClearer
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, credentialStateClearer)

    @Provides
    @Singleton
    fun provideAppUpdatePolicyRepository(
        @ApplicationContext context: Context,
        firestoreProvider: Provider<FirebaseFirestore>
    ): AppUpdatePolicyRepository = FirestoreAppUpdatePolicyRepository(context, firestoreProvider)

    @Provides
    @Singleton
    fun providePlannerRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): PlannerRepository = PlannerRepositoryImpl(context, firebaseAuth, firestore)

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context,
        firebaseAuthProvider: Provider<FirebaseAuth>,
        firestoreProvider: Provider<FirebaseFirestore>
    ): UserPreferencesRepository = SharedUserPreferencesRepository(context, firebaseAuthProvider, firestoreProvider)

    @Provides
    @Singleton
    fun provideBillingClientWrapper(@ApplicationContext context: Context): BillingClientWrapper = BillingClientWrapper(context)

    @Provides
    @Singleton
    fun provideBillingAuthSession(firebaseAuthProvider: Provider<FirebaseAuth>): BillingAuthSession =
        FirebaseBillingAuthSession(firebaseAuthProvider)

    @Provides
    @Singleton
    fun provideEntitlementRepository(
        billingClientWrapper: BillingClientWrapper,
        billingAuthSession: BillingAuthSession,
        firestoreProvider: Provider<FirebaseFirestore>,
        firebaseFunctionsProvider: Provider<FirebaseFunctions>
    ): EntitlementRepository = 
        PlayBillingEntitlementRepository(
            billingClientWrapper,
            billingAuthSession,
            firestoreProvider,
            firebaseFunctionsProvider
        )

    @Provides
    @Singleton
    fun provideCalculatePlanPreviewUseCase(
        resolveAllocationStrategyRulesUseCase: ResolveAllocationStrategyRulesUseCase
    ): CalculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(resolveAllocationStrategyRulesUseCase)

    @Provides
    fun provideCreateReviewInsightUseCase(calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase): CreateReviewInsightUseCase =
        CreateReviewInsightUseCase(calculatePlanPreviewUseCase)

    @Provides
    fun provideResolvePricingStrategyUseCase(): ResolvePricingStrategyUseCase = ResolvePricingStrategyUseCase()

    @Provides
    fun provideResolveAppUpdateDecisionUseCase(
        appUpdatePolicyRepository: AppUpdatePolicyRepository
    ): ResolveAppUpdateDecisionUseCase = ResolveAppUpdateDecisionUseCase(appUpdatePolicyRepository)

    @Provides
    fun provideCheckEntitlementLimitUseCase(entitlementRepository: EntitlementRepository): CheckEntitlementLimitUseCase = 
        CheckEntitlementLimitUseCase(entitlementRepository)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
