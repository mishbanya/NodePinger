package ru.mishbanya.nodepinger.domain.vm.model

data class MainScreenUiState(
    val isLoading: Boolean = false,
    val result: String? = null,
    val latency: String? = null,
    val error: String? = null
)

