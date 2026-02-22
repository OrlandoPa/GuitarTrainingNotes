package com.example.guitareartraining.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guitareartraining.presentation.FeedbackState
import com.example.guitareartraining.presentation.TrainingViewModel
import com.example.guitareartraining.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

// ─────────────────────────────────────────────
//  Main Screen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TrainingViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            if (state.isStarted) {
                TopAppBar(
                    title = {
                        Text(
                            "Guitar Notes Training",
                            fontWeight = FontWeight.Bold,
                            color = Amber
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.stopTraining() }) {
                            Icon(Icons.Default.Close, contentDescription = "Stop Training", tint = TextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface
                    ),
                    actions = {
                        ScoreChips(hits = state.scoreHits, misses = state.scoreMisses)
                    }
                )
            }
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
        ) {
            if (!state.isStarted) {
                StartScreen(onStartClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) })
            } else {
                TrainingContent(viewModel = viewModel)
            }

            // Pause Dialog
            if (state.showPauseDialog) {
                PauseDialog(
                    notesPlayed = state.notesPlayedCount,
                    onContinue = { viewModel.continueAfterPause() },
                    onStop = { viewModel.stopAfterPause() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Score chips in the top bar
// ─────────────────────────────────────────────
@Composable
private fun ScoreChips(hits: Int, misses: Int) {
    Row(modifier = Modifier.padding(end = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SuccessGreen.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
        ) {
            Text(
                text = "✓ $hits",
                color = SuccessGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ErrorRed.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
        ) {
            Text(
                text = "✗ $misses",
                color = ErrorRed,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Start Screen — the "hero" landing page
// ─────────────────────────────────────────────
@Composable
private fun StartScreen(onStartClick: () -> Unit) {
    // Subtle pulsing animation for the glow ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        DarkCard
                    )
                )
            )
    ) {
        // Decorative guitar strings in the background
        GuitarStringsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── App Icon / Logo area ──
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Amber.copy(alpha = pulseAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 200f
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(Amber, AmberDark, Amber)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎸",
                    fontSize = 52.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Title ──
            Text(
                text = "Guitar Notes Training",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Entrenamiento de notas en la guitarra.\nIdentifica notas en el diapasón.",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Start button ──
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor = DarkBackground
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    "Comenzar Entrenamiento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Feature hints ──
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureHint(emoji = "🎯", text = "Notas aleatorias en las 6 cuerdas")
                FeatureHint(emoji = "🎤", text = "Detección en tiempo real con micrófono")
                FeatureHint(emoji = "📊", text = "Seguimiento de aciertos y fallos")
            }
        }
    }
}

@Composable
private fun FeatureHint(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

// Decorative diagonal lines that look like guitar strings
@Composable
private fun GuitarStringsBackground() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
        val stringCount = 6
        val spacing = size.width / (stringCount + 1)
        for (i in 1..stringCount) {
            val x = spacing * i
            drawLine(
                color = Color.White,
                start = Offset(x - 40f, 0f),
                end = Offset(x + 40f, size.height),
                strokeWidth = if (i <= 3) 1.5f else (0.5f + i * 0.4f),
                cap = StrokeCap.Round
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Training Content — active session
// ─────────────────────────────────────────────
@Composable
fun TrainingContent(viewModel: TrainingViewModel) {
    val state by viewModel.state.collectAsState()
    val prompt = state.currentPrompt

    val feedbackColor by animateColorAsState(
        targetValue = when (state.feedbackState) {
            FeedbackState.SUCCESS -> SuccessGreen
            FeedbackState.FAILURE -> ErrorRed
            FeedbackState.ATTEMPT_FAILED -> WarningOrange
            FeedbackState.NEUTRAL -> Color.Transparent
        },
        label = "feedbackColor"
    )

    val bgGradient = when (state.feedbackState) {
        FeedbackState.SUCCESS -> Brush.verticalGradient(
            listOf(SuccessGreen.copy(alpha = 0.15f), DarkBackground)
        )
        FeedbackState.FAILURE -> Brush.verticalGradient(
            listOf(ErrorRed.copy(alpha = 0.15f), DarkBackground)
        )
        FeedbackState.ATTEMPT_FAILED -> Brush.verticalGradient(
            listOf(WarningOrange.copy(alpha = 0.12f), DarkBackground)
        )
        FeedbackState.NEUTRAL -> Brush.verticalGradient(
            listOf(DarkBackground, DarkBackground)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isResting) {
            // ── Rest / feedback state ──
            val icon: String
            val msg: String
            val color: Color

            when (state.feedbackState) {
                FeedbackState.SUCCESS -> {
                    icon = "✅"
                    msg = "¡Correcto!"
                    color = SuccessGreen
                }
                FeedbackState.ATTEMPT_FAILED -> {
                    icon = "⚠️"
                    msg = "¡Segundo intento!"
                    color = WarningOrange
                }
                else -> {
                    icon = "❌"
                    msg = "Siguiente nota…"
                    color = ErrorRed
                }
            }

            Text(icon, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = msg,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        } else if (prompt != null) {
            // ── Instruction label ──
            Text(
                text = "TOCA LA NOTA",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Big Note Name Card ──
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkCard,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = prompt.noteName,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Amber
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = prompt.guitarString.stringName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val positionText = if (prompt.position == 1) "1ra Posición (trastes 0‑11)" else "2da Posición (trastes 12‑21)"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Amber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Amber.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = positionText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberLight,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Circular Timer ──
            TimerRing(
                timerValue = state.timerValue,
                maxValue = 5,
                feedbackColor = feedbackColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Attempts indicator ──
            AttemptsIndicator(attemptsLeft = state.attemptsLeft)
        }
    }
}

// ─────────────────────────────────────────────
//  Circular timer with animated arc
// ─────────────────────────────────────────────
@Composable
private fun TimerRing(timerValue: Int, maxValue: Int, feedbackColor: Color) {
    val progress = timerValue.toFloat() / maxValue.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "timerProgress"
    )

    val ringColor = when {
        timerValue <= 1 -> ErrorRed
        timerValue <= 2 -> WarningOrange
        else -> Amber
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Track
            drawArc(
                color = DarkCard,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${timerValue}s",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = ringColor
        )
    }
}

// ─────────────────────────────────────────────
//  Attempts dot-indicator
// ─────────────────────────────────────────────
@Composable
private fun AttemptsIndicator(attemptsLeft: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Intentos:",
            fontSize = 14.sp,
            color = TextSecondary
        )
        repeat(2) { index ->
            val active = index < attemptsLeft
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (active) Amber else DarkCard)
                    .then(
                        if (active) Modifier.border(1.dp, AmberDark, CircleShape) else Modifier
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Pause Dialog
// ─────────────────────────────────────────────
@Composable
private fun PauseDialog(notesPlayed: Int, onContinue: () -> Unit, onStop: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = DarkCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "¡Llevas $notesPlayed notas!",
                fontWeight = FontWeight.Bold,
                color = Amber
            )
        },
        text = {
            Text(
                "¿Deseas continuar o detener el\nentrenamiento por ahora?",
                color = TextSecondary,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = DarkBackground)
            ) {
                Text("Continuar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onStop) {
                Text("Detener", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        }
    )
}
