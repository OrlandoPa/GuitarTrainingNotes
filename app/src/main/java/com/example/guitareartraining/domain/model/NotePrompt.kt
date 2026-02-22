package com.example.guitareartraining.domain.model

data class NotePrompt(
    val guitarString: GuitarString,
    val noteName: String,
    val position: Int, // 1 for first position (0-11), 2 for second position (12-21)
    val fret: Int      // Internal fret number to validate exactly what the user should play
)
