package pt.ms.myshare.domain.repository

import pt.ms.myshare.domain.model.AppReviewPromptState

interface AppReviewPromptRepository {
    suspend fun loadState(): AppReviewPromptState
    suspend fun recordPositiveAction(): AppReviewPromptState
    suspend fun markInAppReviewRequested(requestedAtMillis: Long): AppReviewPromptState
    suspend fun markPlayStoreRateOpened(): AppReviewPromptState
}
