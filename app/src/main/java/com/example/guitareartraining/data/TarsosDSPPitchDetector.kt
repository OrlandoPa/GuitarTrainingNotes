package com.example.guitareartraining.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import com.example.guitareartraining.domain.PitchDetector

class TarsosDSPPitchDetector : PitchDetector {

    private var dispatcher: AudioDispatcher? = null
    private var pitchListener: ((Float) -> Unit)? = null
    private var isListening = false
    private var audioThread: Thread? = null

    init {
        // TarsosDSP initialization code ready
    }

    override fun setOnPitchDetectedListener(listener: (Float) -> Unit) {
        this.pitchListener = listener
    }

    @SuppressLint("MissingPermission")
    override fun startListening() {
        if (isListening) return
        isListening = true

        val sampleRate = 22050
        val bufferSize = 1024
        val overlap = 0

        try {
            dispatcher = be.tarsos.dsp.io.android.AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, overlap)
        } catch (e: Exception) {
            android.util.Log.e("TarsosDSP", "Error starting dispatcher", e)
            return
        }

        val pdh = PitchDetectionHandler { result: PitchDetectionResult, _: AudioEvent ->
            val pitchInHz = result.pitch
            // Only trigger listener if we detect an actual pitch (no noise) and it has high probability
            android.util.Log.d("TarsosDSP", "Pitch detected (Hz): $pitchInHz, Prob: ${result.probability}")
            if (pitchInHz > 20f && result.probability > 0.7f) {
                pitchListener?.invoke(pitchInHz)
            }
        }

        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.YIN,
            sampleRate.toFloat(),
            bufferSize,
            pdh
        )
        
        dispatcher?.addAudioProcessor(pitchProcessor)

        audioThread = Thread(dispatcher, "Audio Dispatcher")
        audioThread?.start()
    }

    override fun stopListening() {
        if (!isListening) return
        isListening = false
        
        dispatcher?.stop()
        dispatcher = null
        audioThread?.interrupt()
        audioThread = null
    }
}
