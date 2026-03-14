package org.example.robo.core.profile;

/**
 * Enumeriert die verschiedenen Mausklick-Typen.
 */
public enum ClickType {
    /**
     * Linker Mausklick
     */
    LEFT("left", 1),

    /**
     * Rechter Mausklick
     */
    RIGHT("right", 2),

    /**
     * Scroll nach oben
     */
    SCROLL_UP("scroll_up", 0),

    /**
     * Scroll nach unten
     */
    SCROLL_DOWN("scroll_down", 0);

    private final String displayName;
    private final int cgEventType;

    ClickType(String displayName, int cgEventType) {
        this.displayName = displayName;
        this.cgEventType = cgEventType;
    }

    /**
     * Gibt den Anzeigenamen des Click-Typs zurück.
     *
     * @return Anzeigename
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gibt den CoreGraphics Event Type zurück (für native macOS API).
     *
     * @return CoreGraphics Event Type
     */
    public int getCGEventType() {
        return cgEventType;
    }

    /**
     * Konvertiert einen String in ClickType.
     *
     * @param value String-Wert
     * @return ClickType oder LEFT (default)
     */
    public static ClickType fromString(String value) {
        if (value == null) {
            return LEFT;
        }
        try {
            return ClickType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LEFT;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}

