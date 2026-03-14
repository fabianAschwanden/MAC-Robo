package org.example.robo.util;

import java.nio.file.Paths;

/**
 * Globale Konstanten für die Click Roboter Anwendung.
 */
public class Constants {

    // Click-Frequenz Limits
    public static final int MIN_FREQUENCY_HZ = 1;
    public static final int MAX_FREQUENCY_HZ = 100;
    public static final int DEFAULT_FREQUENCY_HZ = 10;

    // Mausposition Defaults
    public static final int DEFAULT_MOUSE_X = 500;
    public static final int DEFAULT_MOUSE_Y = 400;

    // Verzögerung zwischen Klicks
    public static final long DEFAULT_DELAY_MS = 0;

    // Konfigurationsdatei
    public static final String CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".robo").toString();
    public static final String PROFILES_FILE = Paths.get(CONFIG_DIR, "profiles.json").toString();
    public static final String LOGS_DIR = Paths.get(CONFIG_DIR, "logs").toString();

    // Default Profil ID
    public static final String DEFAULT_PROFILE_ID = "default";

    // Hotkeys (macOS Keycodes)
    public static final int EMERGENCY_STOP_KEY = 98; // F7 key
    public static final int START_STOP_HOTKEY = 18; // CMD+SHIFT+1
    public static final int PROFILE_TOGGLE_HOTKEY = 19; // CMD+SHIFT+2

    // UI
    public static final int MAIN_WINDOW_WIDTH = 450;
    public static final int MAIN_WINDOW_HEIGHT = 350;
    public static final int MAIN_WINDOW_MIN_WIDTH = 400;
    public static final int MAIN_WINDOW_MIN_HEIGHT = 300;

    // Timing
    public static final long STATUS_UPDATE_INTERVAL_MS = 100;
    public static final long MOUSE_POSITION_UPDATE_INTERVAL_MS = 200;

    // Logging
    public static final String APP_NAME = "Click Roboter";
    public static final String APP_VERSION = "1.0-MVP";

    // Accessibility
    public static final String ACCESSIBILITY_DIALOG_TITLE = "Zugriffsgenehmigung erforderlich";
    public static final String ACCESSIBILITY_DIALOG_MSG =
        "Die Click Roboter App benötigt Zugriff auf die Barrierefreiheits-Features von macOS.\n" +
        "Bitte geben Sie die Berechtigung unter:\n" +
        "Systemeinstellungen > Sicherheit & Datenschutz > Barrierefreiheit";

    private Constants() {
        // Verhindere Instanziierung
    }
}

