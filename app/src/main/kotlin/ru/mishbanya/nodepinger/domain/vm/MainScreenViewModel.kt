package ru.mishbanya.nodepinger.domain.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.android.annotation.KoinViewModel
import ru.mishbanya.nodepinger.domain.vm.model.MainScreenUiState
import ru.mishbanya.nodepinger.model.repository.NodePinger

@KoinViewModel
class MainScreenViewModel(
    private val nodePinger: NodePinger
) : ViewModel() {

    private val scope = viewModelScope + Dispatchers.IO + SupervisorJob()

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    fun pingNode(cid: String) {
        if (cid.isBlank()) {
            _uiState.update { it.copy(error = "CID не может быть пустым", latency = null, result = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, latency = null, result = null) }

        scope.launch {
            try {
                val startTime = System.currentTimeMillis()

                val pingResponse = nodePinger.pingNode(cid)
                val latency = System.currentTimeMillis() - startTime

                if(pingResponse.isSuccess){
                    val result = pingResponse.getOrNull() ?: "Пинг выполнен успешно, но результат пустой"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = result,
                            latency = latency.toString(),
                            error = null
                        )
                    }
                } else {
                    val errorMessage = pingResponse.exceptionOrNull()?.localizedMessage ?: "Неизвестная ошибка при пинге"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = null,
                            latency = null,
                            error = errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        result = null,
                        latency = null,
                        error = e.localizedMessage ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }
}