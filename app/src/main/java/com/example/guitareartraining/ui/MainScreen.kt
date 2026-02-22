package com.example.guitareartraining.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.guitareartraining.presentation.FeedbackState
import com.example.guitareartraining.presentation.TrainingViewModel
import com.example.guitareartraining.ui.theme.ErrorRed
import com.example.guitareartraining.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TrainingViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guitar Ear Training") },
                navigationIcon = {
                    if (state.isStarted) {
                        IconButton(onClick = { viewModel.stopTraining() }) {
                            Icon(Icons.Default.Close, contentDescription = "Stop Training")
                        }
                    }
                },
                actions = {
                    if (state.isStarted) {
                        Row(modifier = Modifier.padding(end = 16.dp)) {
                            Text(
                                "Aciertos: ${state.scoreHits}", 
                                color = PrimaryGreen, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                "Fallos: ${state.scoreMisses}", 
                                color = ErrorRed, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    viewModel.startTraining()
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (!state.isStarted) {
                // Start Screen
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier.size(200.dp, 80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Comenzar", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Active Training Screen
                TrainingContent(viewModel = viewModel)
            }
        }
        
        // Pause Dialog
        if (state.showPauseDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Llevas ${state.notesPlayedCount} notas!") },
                text = { Text("¿Deseas continuar o detener el entrenamiento por ahora?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.continueAfterPause() }) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.stopAfterPause() }) {
                        Text("Detener", color = ErrorRed)
                    }
                }
            )
        }
    }
}

@Composable
fun TrainingContent(viewModel: TrainingViewModel) {
    val state by viewModel.state.collectAsState()
    val prompt = state.currentPrompt

    val backgroundColor by animateColorAsState(
        targetValue = when (state.feedbackState) {
            FeedbackState.SUCCESS -> PrimaryGreen.copy(alpha = 0.3f)
            FeedbackState.FAILURE -> ErrorRed.copy(alpha = 0.3f)
            FeedbackState.ATTEMPT_FAILED -> Color(0xFFFFA000).copy(alpha = 0.3f) // Orange/Yellow warning
            FeedbackState.NEUTRAL -> Color.Transparent
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isResting) {
            Text(
                text = if (state.feedbackState == FeedbackState.SUCCESS) "¡Correcto!" else "Siguiente nota en breve...",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.feedbackState == FeedbackState.SUCCESS) PrimaryGreen else MaterialTheme.colorScheme.onBackground
            )
        } else if (prompt != null) {
            // Note Prompt
            Text(
                text = "Toca la nota:",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val positionText = if (prompt.position == 1) "1ra Posición (0-11)" else "2da Posición (12-21)"
            
            Text(
                text = prompt.noteName,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "en la ${prompt.guitarString.stringName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = positionText,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Timer Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${state.timerValue}s",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Intentos restantes: ${state.attemptsLeft}",
                fontSize = 16.sp,
                color = if (state.attemptsLeft == 1) ErrorRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
        }
    }
}
