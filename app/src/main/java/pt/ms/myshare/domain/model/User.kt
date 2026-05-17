package pt.ms.myshare.domain.model

data class User(
    val email: String? = null,
    val isAnonymous: Boolean = false
)
