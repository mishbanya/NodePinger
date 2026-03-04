package ru.mishbanya.nodepinger.domain.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.android.annotation.KoinViewModel
import ru.mishbanya.nodepinger.domain.vm.model.MainScreenUiState
import ru.mishbanya.nodepinger.model.repository.NodePinger
import ru.mishbanya.nodepinger.model.repository.WantSender

@KoinViewModel
class MainScreenViewModel(
    private val wantSender: WantSender,
    private val nodePinger: NodePinger
) : ViewModel() {

    private val scope = viewModelScope + Dispatchers.IO + SupervisorJob()

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    private var _pingingIntervalMillis = MutableStateFlow(1000L)
    val pingingIntervalMillis: StateFlow<Long> = _pingingIntervalMillis.asStateFlow()

    private val pingerJob = scope.launch {
        startPinging()
    }

    private fun startPinging() {
        scope.launch {
            while (isActive){
                try {
                    val latency = nodePinger.pingNode(pingingIntervalMillis.value)
                    println(latency)
                    _uiState.update { it.copy(latency = latency.toString(), latencyError = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(latencyError = true, latency = null) }
                } finally {
                    delay(pingingIntervalMillis.value)
                }
            }
        }
    }

    fun changePingInterval(newInterval: Long) {
        _pingingIntervalMillis.value = newInterval
    }

    fun sendWant(cid: String) {
        if (cid.isBlank()) {
            _uiState.update { it.copy(error = "CID не может быть пустым", result = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, result = null) }

        scope.launch {
            try {
                val pingResponse = wantSender.sendWants(cid)

                if(pingResponse.isSuccess){
                    val result = pingResponse.getOrNull() ?: "Пинг выполнен успешно, но результат пустой"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = result,
                            error = null
                        )
                    }
                } else {
                    val errorMessage = pingResponse.exceptionOrNull()?.localizedMessage ?: "Неизвестная ошибка при пинге"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = null,
                            error = errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        result = null,
                        error = e.localizedMessage ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }
}