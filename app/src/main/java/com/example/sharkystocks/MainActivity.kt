package com.example.sharkystocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.sharkystocks.data.repository.StockRepository
import com.example.sharkystocks.navigation.NavGraph
import com.example.sharkystocks.screens.SplashScreen
import com.example.sharkystocks.ui.theme.sharkystocksTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy { StockRepository(this) }
    private val viewModel: StockViewModel by viewModels {
        StockViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            sharkystocksApp(viewModel = viewModel)
        }
    }
}

@Composable
fun sharkystocksApp(viewModel: StockViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    sharkystocksTheme {
        if (showSplash) {
            SplashScreen(
                onSplashFinished = { showSplash = false }
            )
        } else {
            NavGraph(viewModel = viewModel)
        }
    }
}