package com.example.guitareartraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guitareartraining.presentation.TrainingViewModel
import com.example.guitareartraining.ui.MainScreen
import com.example.guitareartraining.ui.theme.GuitarEarTrainingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuitarEarTrainingTheme {
                val viewModel: TrainingViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
