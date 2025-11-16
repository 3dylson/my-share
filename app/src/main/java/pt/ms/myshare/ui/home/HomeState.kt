package pt.ms.myshare.ui.home

data class HomeState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
