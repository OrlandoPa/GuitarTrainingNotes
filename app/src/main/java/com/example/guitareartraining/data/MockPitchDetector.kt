package com.example.guitareartraining.data

import com.example.guitareartraining.domain.PitchDetector

class MockPitchDetector : PitchDetector {
    private var listener: ((Float) -> Unit)? = null
    private var isListening = false

    override fun startListening() {
        isListening = true
    }

    override fun stopListening() {
        isListening = false
    }

    override fun setOnPitchDetectedListener(listener: (Float) -> Unit) {
        this.listener = listener
    }

    // Custom method to simulate a pitch being detected from the UI debug buttons
    fun simulatePitch(frequency: Float) {
        if (isListening) {
            listener?.invoke(frequency)
        }
    }
}
