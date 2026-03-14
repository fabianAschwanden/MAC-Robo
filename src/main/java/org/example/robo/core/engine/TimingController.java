package org.example.robo.core.engine;

import org.example.robo.util.Constants;

/**
 * Verantwortlich für die Berechnung und Verwaltung von Klick-Timing.
 * Sichert Genauigkeit und Konsistenz bei der Klick-Ausführung.
 */
public class TimingController {

    /**
     * Berechnet das Klick-Intervall in Nanosekunden basierend auf der Frequenz in Hz.
     *
     * @param frequencyHz Frequenz in Hertz (Klicks pro Sekunde)
     * @return Intervall in Nanosekunden
     */
    public static long calculateIntervalNanos(int frequencyHz) {
        long validFrequency = Math.max(Constants.MIN_FREQUENCY_HZ, Math.min(frequencyHz, Constants.MAX_FREQUENCY_HZ));
        return 1_000_000_000L / validFrequency; // 1 Sekunde in Nanos / Frequenz
    }

    /**
     * Berechnet das Klick-Intervall in Millisekunden basierend auf der Frequenz in Hz.
     *
     * @param frequencyHz Frequenz in Hertz (Klicks pro Sekunde)
     * @return Intervall in Millisekunden
     */
    public static long calculateIntervalMs(int frequencyHz) {
        long validFrequency = Math.max(Constants.MIN_FREQUENCY_HZ, Math.min(frequencyHz, Constants.MAX_FREQUENCY_HZ));
        return 1000 / validFrequency;
    }

    /**
     * Konvertiert Nanosekunden in Millisekunden.
     *
     * @param nanos Zeit in Nanosekunden
     * @return Zeit in Millisekunden
     */
    public static long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }

    /**
     * Validiert eine Klick-Frequenz.
     *
     * @param frequencyHz zu validierende Frequenz
     * @return validierte Frequenz (im Bereich Constants.MIN_FREQUENCY_HZ bis Constants.MAX_FREQUENCY_HZ)
     */
    public static int validateFrequency(int frequencyHz) {
        return Math.max(Constants.MIN_FREQUENCY_HZ, Math.min(frequencyHz, Constants.MAX_FREQUENCY_HZ));
    }

    private TimingController() {
        // Utility class
    }
}

