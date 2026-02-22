package com.example.guitareartraining.domain.model

enum class GuitarString(val number: Int, val stringName: String, val openStrNoteIndex: Int) {
    E1_HIGH(1, "1era cuerda", 4),  // E
    B2(2, "2da cuerda", 11),       // B
    G3(3, "3era cuerda", 7),       // G
    D4(4, "4ta cuerda", 2),        // D
    A5(5, "5ta cuerda", 9),        // A
    E6_LOW(6, "6ta cuerda", 4);    // E

    companion object {
        fun fromNumber(number: Int): GuitarString? = entries.find { it.number == number }
        
        // Cromatic scale without sharp/flat distinction (using sharp preference for simplicity in naming if needed,
        // though typically we just want to know the pitch class)
        val CHROMATIC_NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        
        fun getNoteName(guitarString: GuitarString, fret: Int): String {
            val noteIndex = (guitarString.openStrNoteIndex + fret) % CHROMATIC_NOTES.size
            return CHROMATIC_NOTES[noteIndex]
        }
    }
}
