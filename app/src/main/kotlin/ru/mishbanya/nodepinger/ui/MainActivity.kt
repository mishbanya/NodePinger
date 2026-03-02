package ru.mishbanya.nodepinger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.mishbanya.nodepinger.ui.theme.NodePingerTheme
import ru.mishbanya.nodepinger.ui.view.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NodePingerTheme {
                Surface {
                    Scaffold(
                        modifier = Modifier.Companion
                            .fillMaxSize()
                    ) { innerPadding ->
                        MainScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}