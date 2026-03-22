package pt.ms.myshare.presentation.ui.onboarding

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.presentation.ui.home.FakePlannerRepository
import pt.ms.myshare.presentation.ui.home.TestFakeEntitlementRepository
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var fakePlannerRepository: FakePlannerRepository
    private lateinit var fakeEntitlementRepository: TestFakeEntitlementRepository
    private lateinit var fakeWorkManager: FakeWorkManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakePlannerRepository = FakePlannerRepository()
        fakeEntitlementRepository = TestFakeEntitlementRepository()
        fakeWorkManager = FakeWorkManager()
        
        viewModel = OnboardingViewModel(
            plannerRepository = fakePlannerRepository,
            entitlementRepository = fakeEntitlementRepository,
            calculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(),
            resolvePricingStrategyUseCase = ResolvePricingStrategyUseCase(),
            workManager = fakeWorkManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `completeOnboardingWithoutPremium sets state correctly`() = runTest {
        viewModel.completeOnboardingWithoutPremium()
        advanceUntilIdle()
        
        assertTrue(fakePlannerRepository.isOnboardingCompleted())
        assertTrue(viewModel.uiState.value.onboardingCompleted)
    }

    @Test
    fun `saveReminderConfiguration saves config and schedules worker`() = runTest {
        viewModel.saveReminderConfiguration(
            time = LocalTime.of(9, 30),
            cadence = ReminderCadence.PAYDAYS
        )
        advanceUntilIdle()
        
        val config = fakePlannerRepository.loadReminderConfiguration()
        assertTrue(config.enabled)
        assertEquals(9, config.hourOfDay)
        assertEquals(30, config.minute)
        assertEquals(ReminderCadence.PAYDAYS, config.cadence)
        
        assertTrue(fakePlannerRepository.isOnboardingCompleted())
        assertTrue(fakeWorkManager.enqueuedUniqueWork.contains("myshare_reminder_worker"))
    }

    @Test
    fun `skipReminderConfiguration completes onboarding without worker`() = runTest {
        viewModel.skipReminderConfiguration()
        advanceUntilIdle()

        val config = fakePlannerRepository.loadReminderConfiguration()
        assertTrue(!config.enabled)
        assertTrue(fakePlannerRepository.isOnboardingCompleted())
        assertTrue(fakeWorkManager.enqueuedUniqueWork.isEmpty())
    }
}

// Simple Fake for WorkManager to avoid mockk or robolectric dependency in JVM domain tests
open class FakeWorkManager : WorkManager() {
    val enqueuedUniqueWork = mutableListOf<String>()

    override fun enqueueUniquePeriodicWork(
        uniqueWorkName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
        periodicWork: PeriodicWorkRequest
    ): androidx.work.Operation {
        enqueuedUniqueWork.add(uniqueWorkName)
        // Stub operation since we don't assert on Operation state
        return object : androidx.work.Operation {
            override fun getState(): androidx.lifecycle.LiveData<androidx.work.Operation.State> = throw NotImplementedError()
            override fun getResult(): com.google.common.util.concurrent.ListenableFuture<androidx.work.Operation.State.SUCCESS> = throw NotImplementedError()
        }
    }
    
    // Stub other methods ...
    override fun enqueue(requests: MutableList<out androidx.work.WorkRequest>): androidx.work.Operation = throw NotImplementedError()
    override fun beginWith(work: MutableList<androidx.work.OneTimeWorkRequest>): androidx.work.WorkContinuation = throw NotImplementedError()
    override fun beginUniqueWork(uniqueWorkName: String, existingWorkPolicy: androidx.work.ExistingWorkPolicy, work: MutableList<androidx.work.OneTimeWorkRequest>): androidx.work.WorkContinuation = throw NotImplementedError()
    override fun enqueueUniqueWork(uniqueWorkName: String, existingWorkPolicy: androidx.work.ExistingWorkPolicy, work: MutableList<androidx.work.OneTimeWorkRequest>): androidx.work.Operation = throw NotImplementedError()
    override fun cancelWorkById(id: java.util.UUID): androidx.work.Operation = throw NotImplementedError()
    override fun cancelAllWorkByTag(tag: String): androidx.work.Operation = throw NotImplementedError()
    override fun cancelUniqueWork(uniqueWorkName: String): androidx.work.Operation = throw NotImplementedError()
    override fun cancelAllWork(): androidx.work.Operation = throw NotImplementedError()
    override fun createCancelPendingIntent(id: java.util.UUID): android.app.PendingIntent = throw NotImplementedError()
    override fun pruneWork(): androidx.work.Operation = throw NotImplementedError()
    override fun getLastCancelAllTimeMillisLiveData(): androidx.lifecycle.LiveData<Long> = throw NotImplementedError()
    override fun getLastCancelAllTimeMillis(): com.google.common.util.concurrent.ListenableFuture<Long> = throw NotImplementedError()
    override fun getWorkInfoByIdLiveData(id: java.util.UUID): androidx.lifecycle.LiveData<androidx.work.WorkInfo> = throw NotImplementedError()
    override fun getWorkInfoByIdFlow(id: java.util.UUID): kotlinx.coroutines.flow.Flow<androidx.work.WorkInfo> = throw NotImplementedError()
    override fun getWorkInfoById(id: java.util.UUID): com.google.common.util.concurrent.ListenableFuture<androidx.work.WorkInfo> = throw NotImplementedError()
    override fun getWorkInfosByTagLiveData(tag: String): androidx.lifecycle.LiveData<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfosByTagFlow(tag: String): kotlinx.coroutines.flow.Flow<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfosByTag(tag: String): com.google.common.util.concurrent.ListenableFuture<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfosForUniqueWorkLiveData(uniqueWorkName: String): androidx.lifecycle.LiveData<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfosForUniqueWorkFlow(uniqueWorkName: String): kotlinx.coroutines.flow.Flow<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfosForUniqueWork(uniqueWorkName: String): com.google.common.util.concurrent.ListenableFuture<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfosLiveData(workQuery: androidx.work.WorkQuery): androidx.lifecycle.LiveData<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfosFlow(workQuery: androidx.work.WorkQuery): kotlinx.coroutines.flow.Flow<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun getWorkInfos(workQuery: androidx.work.WorkQuery): com.google.common.util.concurrent.ListenableFuture<MutableList<androidx.work.WorkInfo>> = throw NotImplementedError()
    override fun updateWork(request: androidx.work.WorkRequest): com.google.common.util.concurrent.ListenableFuture<androidx.work.WorkManager.UpdateResult> = throw NotImplementedError()
}
