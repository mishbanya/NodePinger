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

@KoinViewModel
class MainScreenViewModel : ViewModel() {

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
                // Здесь должна быть логика пинга ноды по CID
                // Например, можно использовать HTTP клиент для отправки запроса к ноде
                // И получать результат и время ответа
                val result = "Пинг успешен для CID: $cid" // Заглушка результата
                val latency = System.currentTimeMillis() - startTime

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        result = result,
                        latency = latency.toString(),
                        error = null
                    )
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