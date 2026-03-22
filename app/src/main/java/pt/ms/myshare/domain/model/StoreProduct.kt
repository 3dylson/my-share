package pt.ms.myshare.domain.model

data class StoreProduct(
    val productId: String,
    val name: String,
    val description: String,
    val price: String,
    val basePlanId: String?,
    val offerToken: String?
)
