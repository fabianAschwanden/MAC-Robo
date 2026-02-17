package org.example.robo.core.engine;

import org.example.robo.util.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für TimingController.
 */
class TimingControllerTest {

    @Test
    void testCalculateIntervalMs() {
        // 10 Hz sollte 100ms sein
        assertEquals(100, TimingController.calculateIntervalMs(10));

        // 100 Hz sollte 10ms sein
        assertEquals(10, TimingController.calculateIntervalMs(100));

        // 1 Hz sollte 1000ms sein
        assertEquals(1000, TimingController.calculateIntervalMs(1));
    }

    @Test
    void testCalculateIntervalNanos() {
        // 10 Hz sollte 100.000.000 Nanos sein
        assertEquals(100_000_000L, TimingController.calculateIntervalNanos(10));

        // 100 Hz sollte 10.000.000 Nanos sein
        assertEquals(10_000_000L, TimingController.calculateIntervalNanos(100));
    }

    @Test
    void testNanosToMillis() {
        assertEquals(100, TimingController.nanosToMillis(100_000_000L));
        assertEquals(1, TimingController.nanosToMillis(1_000_000L));
        assertEquals(1000, TimingController.nanosToMillis(1_000_000_000L));
    }

    @Test
    void testValidateFrequency() {
        // Zu niedriger Wert sollte auf MIN geklemmt werden
        assertEquals(Constants.MIN_FREQUENCY_HZ, TimingController.validateFrequency(0));
        assertEquals(Constants.MIN_FREQUENCY_HZ, TimingController.validateFrequency(-10));

        // Zu hoher Wert sollte auf MAX geklemmt werden
        assertEquals(Constants.MAX_FREQUENCY_HZ, TimingController.validateFrequency(200));
        assertEquals(Constants.MAX_FREQUENCY_HZ, TimingController.validateFrequency(1000));

        // Gültige Werte sollten unverändert bleiben
        assertEquals(50, TimingController.validateFrequency(50));
        assertEquals(1, TimingController.validateFrequency(1));
        assertEquals(100, TimingController.validateFrequency(100));
    }
}

