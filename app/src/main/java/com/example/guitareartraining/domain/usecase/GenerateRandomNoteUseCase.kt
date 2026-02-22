package com.example.guitareartraining.domain.usecase

import com.example.guitareartraining.domain.model.GuitarString
import com.example.guitareartraining.domain.model.NotePrompt
import kotlin.random.Random

class GenerateRandomNoteUseCase {
    
    // Squier Fender Stratocaster has 21 frets
    private val maxFrets = 21

    operator fun invoke(): NotePrompt {
        // 1. Choose a random string
        val randomStringNumber = Random.nextInt(1, 7) // 1 to 6
        val guitarString = GuitarString.fromNumber(randomStringNumber) 
            ?: throw IllegalStateException("Invalid string number generated")

        // 2. Choose a random position
        // Position 1: Frets 0 to 11
        // Position 2: Frets 12 to 21
        val randomPosition = Random.nextInt(1, 3) // 1 or 2
        
        // 3. Choose a random fret within the bounds of that position
        val randomFret = if (randomPosition == 1) {
            Random.nextInt(0, 12) // 0 to 11
        } else {
            Random.nextInt(12, maxFrets + 1) // 12 to 21
        }

        // 4. Calculate note name
        val noteName = GuitarString.getNoteName(guitarString, randomFret)

        return NotePrompt(
            guitarString = guitarString,
            noteName = noteName,
            position = randomPosition,
            fret = randomFret
        )
    }
}
