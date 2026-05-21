package pt.ms.myshare.domain.model

data class GoogleAccountConnection(
    val user: User,
    val mode: GoogleAccountConnectionMode,
    val previousUserId: String? = null,
    val currentUserId: String? = user.id
)
