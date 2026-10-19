package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.WeatherAppScreen
import com.example.ui.WeatherViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.ui.graphics.Color.Black
                ) { innerPadding ->
                    WeatherAppScreen(
                        viewModel = weatherViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
