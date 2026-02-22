package com.example.guitareartraining.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guitareartraining.data.TarsosDSPPitchDetector
import com.example.guitareartraining.domain.PitchDetector
import com.example.guitareartraining.domain.model.GuitarString
import com.example.guitareartraining.domain.model.NotePrompt
import com.example.guitareartraining.domain.usecase.GenerateRandomNoteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2

enum class Difficulty { NORMAL, HARD }

enum class FeedbackState {
    NEUTRAL, SUCCESS, FAILURE, ATTEMPT_FAILED
}

data class TrainingState(
    val scoreHits: Int = 0,
    val scoreMisses: Int = 0,
    val currentPrompt: NotePrompt? = null,
    val timerValue: Int = 5,
    val feedbackState: FeedbackState = FeedbackState.NEUTRAL,
    val isResting: Boolean = false,
    val isStarted: Boolean = false,
    val notesPlayedCount: Int = 0,
    val showPauseDialog: Boolean = false,
    val attemptsLeft: Int = 2,
    val difficulty: Difficulty = Difficulty.NORMAL
)

class TrainingViewModel : ViewModel() {

    // Using TarsosDSPPitchDetector for real microphone processing
    private val pitchDetector: PitchDetector = TarsosDSPPitchDetector()
    private val generateRandomNoteUseCase: GenerateRandomNoteUseCase = GenerateRandomNoteUseCase()

    private val _state = MutableStateFlow(TrainingState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null
    private var restJob: Job? = null

    init {
        pitchDetector.setOnPitchDetectedListener { pitch ->
            handlePitchDetected(pitch)
        }
    }

    fun startTraining(difficulty: Difficulty = Difficulty.NORMAL) {
        if (_state.value.isStarted) return
        
        _state.update { it.copy(isStarted = true, difficulty = difficulty) }
        pitchDetector.startListening()
        nextNote()
    }

    fun stopTraining() {
        timerJob?.cancel()
        restJob?.cancel()
        pitchDetector.stopListening()
        _state.update { TrainingState() } // Reset to initial state
    }

    fun continueAfterPause() {
        _state.update { it.copy(showPauseDialog = false, notesPlayedCount = 0) }
        startTimer()
    }

    fun stopAfterPause() {
        stopTraining()
    }

    private var consecutiveWrongPitches = 0
    private var consecutiveRightPitches = 0

    private fun nextNote() {
        consecutiveWrongPitches = 0
        consecutiveRightPitches = 0
        val prompt = generateRandomNoteUseCase()
        val isHard = _state.value.difficulty == Difficulty.HARD
        val initialTimer = if (isHard) 3 else 5
        val initialAttempts = if (isHard) 1 else 2

        _state.update {
            it.copy(
                currentPrompt = prompt,
                timerValue = initialTimer,
                feedbackState = FeedbackState.NEUTRAL,
                isResting = false,
                attemptsLeft = initialAttempts
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timerValue > 0) {
                delay(1000L)
                _state.update { it.copy(timerValue = it.timerValue - 1) }
            }
            handleFailedAttempt()
        }
    }

    private fun handleFailedAttempt() {
        timerJob?.cancel()
        if (_state.value.attemptsLeft > 1) {
            // Give one more attempt with a brief cooldown to avoid immediate double-fails
            restJob?.cancel()
            restJob = viewModelScope.launch {
                _state.update { current ->
                    current.copy(
                        attemptsLeft = current.attemptsLeft - 1,
                        feedbackState = FeedbackState.ATTEMPT_FAILED,
                        isResting = true
                    )
                }
                delay(1500L)
                val isHard = _state.value.difficulty == Difficulty.HARD
                _state.update { current ->
                    current.copy(
                        feedbackState = FeedbackState.NEUTRAL,
                        isResting = false,
                        timerValue = if (isHard) 3 else 5
                    )
                }
                consecutiveWrongPitches = 0
                consecutiveRightPitches = 0
                startTimer()
            }
        } else {
            // Failed both attempts
            _state.update { current ->
                val newCount = current.notesPlayedCount + 1
                current.copy(
                    scoreMisses = current.scoreMisses + 1,
                    feedbackState = FeedbackState.FAILURE,
                    notesPlayedCount = newCount,
                    showPauseDialog = (newCount > 0 && newCount % 10 == 0)
                )
            }
            startRest()
        }
    }

    private fun handlePitchDetected(pitchInHz: Float) {
        val prompt = _state.value.currentPrompt ?: return
        if (_state.value.isResting || _state.value.showPauseDialog || pitchInHz < 20f || _state.value.feedbackState == FeedbackState.SUCCESS) return

        val targetFreq = getTargetFrequency(prompt.guitarString, prompt.fret)
        val toleranceCents = 40.0 // 40 cents tolerance
        
        // Calculate cents difference: 1200 * log2(f1/f2)
        val centsDifference = abs(1200.0 * log2(pitchInHz / targetFreq))

        if (centsDifference <= toleranceCents) {
            consecutiveRightPitches++
            consecutiveWrongPitches = 0
            if (consecutiveRightPitches >= 2) {
                // Note matched stably!
                timerJob?.cancel() // Stop the timer
                _state.update { current ->
                    val newCount = current.notesPlayedCount + 1
                    current.copy(
                        scoreHits = current.scoreHits + 1,
                        feedbackState = FeedbackState.SUCCESS,
                        notesPlayedCount = newCount,
                        showPauseDialog = (newCount > 0 && newCount % 10 == 0)
                    )
                }
                startRest()
            }
        } else {
            consecutiveRightPitches = 0
            // If the note is clearly wrong (let's say > 80 cents difference)
            // We ignore slight pitch bends to avoid false negatives
            if (centsDifference > 80.0) {
                consecutiveWrongPitches++
                // After 5 consecutive wrong frames (~150-200ms of stable wrong pitch)
                if (consecutiveWrongPitches >= 5) {
                    handleFailedAttempt()
                }
            }
        }
    }

    private fun startRest() {
        restJob?.cancel()
        restJob = viewModelScope.launch {
            _state.update { it.copy(isResting = true) }
            delay(3000L)
            
            if (!_state.value.showPauseDialog) {
                nextNote()
            }
        }
    }

    private fun getTargetFrequency(string: GuitarString, fret: Int): Float {
        val openFreq = when(string) {
            GuitarString.E6_LOW -> 82.41f
            GuitarString.A5 -> 110.00f
            GuitarString.D4 -> 146.83f
            GuitarString.G3 -> 196.00f
            GuitarString.B2 -> 246.94f
            GuitarString.E1_HIGH -> 329.63f
        }
        return (openFreq * Math.pow(2.0, fret / 12.0)).toFloat()
    }


    override fun onCleared() {
        super.onCleared()
        pitchDetector.stopListening()
    }
}
