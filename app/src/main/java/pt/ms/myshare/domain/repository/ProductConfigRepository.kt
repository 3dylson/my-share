package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pt.ms.myshare.domain.model.ProductExperienceConfig

interface ProductConfigRepository {
    val config: StateFlow<ProductExperienceConfig>

    suspend fun refresh()
}
