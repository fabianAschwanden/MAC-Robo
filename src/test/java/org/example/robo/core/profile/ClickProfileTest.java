package org.example.robo.core.profile;

import org.example.robo.util.Constants;
import org.example.robo.util.MousePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für die ClickProfile Klasse.
 */
class ClickProfileTest {

    private ClickProfile profile;

    @BeforeEach
    void setUp() {
        profile = ClickProfile.createDefault();
    }

    @Test
    void testCreateDefault() {
        assertNotNull(profile);
        assertEquals(Constants.DEFAULT_PROFILE_ID, profile.getId());
        assertEquals(Constants.DEFAULT_FREQUENCY_HZ, profile.getClickFrequency());
        assertEquals(ClickType.LEFT, profile.getClickType());
        assertEquals(-1, profile.getNumberOfClicks()); // unbegrenzt
    }

    @Test
    void testValidateFrequency() {
        // Test zu nierige Frequenz
        profile.setClickFrequency(0);
        assertEquals(Constants.MIN_FREQUENCY_HZ, profile.getClickFrequency());

        // Test zu hohe Frequenz
        profile.setClickFrequency(200);
        assertEquals(Constants.MAX_FREQUENCY_HZ, profile.getClickFrequency());

        // Test gültige Frequenz
        profile.setClickFrequency(50);
        assertEquals(50, profile.getClickFrequency());
    }

    @Test
    void testSetPosition() {
        MousePosition newPos = new MousePosition(100, 200);
        profile.setPosition(newPos);
        assertEquals(newPos, profile.getPosition());
    }

    @Test
    void testClickIntervalCalculation() {
        profile.setClickFrequency(10);
        assertEquals(100, profile.getClickIntervalMs()); // 1000ms / 10 Hz = 100ms

        profile.setClickFrequency(100);
        assertEquals(10, profile.getClickIntervalMs()); // 1000ms / 100 Hz = 10ms

        profile.setClickFrequency(1);
        assertEquals(1000, profile.getClickIntervalMs()); // 1000ms / 1 Hz = 1000ms
    }

    @Test
    void testClickTypeConversion() {
        assertEquals(ClickType.LEFT, ClickType.fromString("LEFT"));
        assertEquals(ClickType.RIGHT, ClickType.fromString("RIGHT"));
        assertEquals(ClickType.LEFT, ClickType.fromString("invalid"));
        assertEquals(ClickType.LEFT, ClickType.fromString(null));
    }

    @Test
    void testProfileEquality() {
        ClickProfile profile2 = new ClickProfile(
                profile.getId(),
                "Different Name",
                "Different Description",
                profile.getClickFrequency(),
                profile.getPosition(),
                profile.getClickType(),
                profile.getNumberOfClicks(),
                profile.getDelayBetweenClicks(),
                profile.getCreatedAt(),
                profile.getLastModified()
        );

        // Profile sind gleich wenn die ID gleich ist
        assertEquals(profile, profile2);
    }

    @Test
    void testNegativeNumberOfClicks() {
        profile.setNumberOfClicks(-1);
        assertEquals(-1, profile.getNumberOfClicks()); // unbegrenzt

        profile.setNumberOfClicks(0);
        assertEquals(-1, profile.getNumberOfClicks()); // konvertiert zu unbegrenzt
    }
}

