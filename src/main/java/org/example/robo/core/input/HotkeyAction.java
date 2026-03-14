package org.example.robo.core.input;

/**
 * Enumeriert die möglichen Hotkey-Aktionen.
 */
public enum HotkeyAction {
    /**
     * Startet oder stoppt die Klick-Automatisierung
     */
    START_STOP("Start/Stop"),

    /**
     * Notfall-Abbruch für sofortige Beendigung
     */
    EMERGENCY_STOP("Emergency Stop"),

    /**
     * Wechselt zum nächsten Profil
     */
    NEXT_PROFILE("Next Profile");

    private final String displayName;

    HotkeyAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

