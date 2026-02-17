package org.example.robo.core.input;

/**
 * Callback-Interface für Hotkey-Recording-Ergebnisse.
 */
@FunctionalInterface
public interface HotkeyRecordingCallback {
    /**
     * Wird aufgerufen, wenn ein Hotkey aufgezeichnet wurde oder Timeout eintritt.
     *
     * @param binding die aufgezeichnete Hotkey-Bindung, oder null bei Timeout/Abbruch
     */
    void onHotkeyRecorded(HotkeyBinding binding);
}

