package ru.mishbanya.nodepinger.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.mishbanya.nodepinger.domain.vm.MainScreenViewModel

@Composable
fun MainScreen(
    modifier: Modifier,
    viewModel: MainScreenViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var cid by remember { mutableStateOf("") }

        TextField(
            value = cid,
            onValueChange = { cid = it },
            label = { Text("Enter CID") },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.pingNode(cid) },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(5.dp)
            ) {
                Text("Ping Node")
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility (uiState.latency != null) {
                Text(
                    text = "Latency: ${uiState.latency} ms"
                )
            }

            AnimatedVisibility (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(5.dp)
                )
                .fillMaxWidth()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.result != null -> {
                    Text(
                        text = "Result: ${uiState.result}",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
