package com.example.guitareartraining.domain

interface PitchDetector {
    fun startListening()
    fun stopListening()
    fun setOnPitchDetectedListener(listener: (Float) -> Unit)
}
