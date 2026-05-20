package pt.ms.myshare.domain.model

data class User(
    val id: String? = null,
    val email: String? = null,
    val isAnonymous: Boolean = false
)
