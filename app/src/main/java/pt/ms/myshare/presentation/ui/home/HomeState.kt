package pt.ms.myshare.presentation.ui.home

data class HomeState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
