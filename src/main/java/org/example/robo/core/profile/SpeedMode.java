package org.example.robo.core.profile;

/**
 * Bestimmt wie die Klick-Geschwindigkeit interpretiert wird.
 */
public enum SpeedMode {
    /** N Klicks pro Sekunde — angezeigt als "N / sec" */
    FREQUENCY,

    /** Einmal alle N Sekunden klicken — angezeigt als "N sec click once" */
    INTERVAL
}
