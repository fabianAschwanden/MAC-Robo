package org.example.robo.core.profile;

/**
 * Unterscheidet zwischen fest definierter und intelligenter Wartezeit
 * innerhalb eines WebRecordingStep.
 *
 * <ul>
 *   <li>HARD – Feste Pause in Millisekunden (sleep)</li>
 *   <li>SMART – Warten bis ein DOM-Element geladen/sichtbar ist (waitForSelector)</li>
 * </ul>
 */
public enum DelayType {
    /** Feste Wartezeit in Millisekunden. */
    HARD("Feste Pause"),
    /** Warten bis das Zielelement im DOM verfügbar ist. */
    SMART("Intelligente Pause");

    private final String displayName;

    DelayType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
