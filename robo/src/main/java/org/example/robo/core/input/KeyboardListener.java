package org.example.robo.core.input;

/**
 * Interface für Keyboard Input Handling.
 * Verantwortlich für die Registrierung und Verwaltung von globalen Hotkeys.
 */
public interface KeyboardListener {

    /**
     * Registriert eine Hotkey-Kombination mit einer Aktion.
     *
     * @param keyCode der zu beobachtende Tastencode
     * @param action die auszuführende Aktion
     */
    void registerHotkey(int keyCode, HotkeyAction action);

    /**
     * Entfernt eine registrierte Hotkey-Kombination.
     *
     * @param keyCode der zu entfernende Tastencode
     */
    void unregisterHotkey(int keyCode);

    /**
     * Registriert einen Listener für Keyboard Events.
     *
     * @param listener der zu registrierende Listener
     */
    void addKeyboardEventListener(KeyboardEventListener listener);

    /**
     * Entfernt einen registrierten Listener.
     *
     * @param listener der zu entfernende Listener
     */
    void removeKeyboardEventListener(KeyboardEventListener listener);

    /**
     * Startet das globale Keyboard Listening.
     */
    void startListening();

    /**
     * Stoppt das globale Keyboard Listening.
     */
    void stopListening();

    /**
     * Prüft, ob Keyboard Listening aktiv ist.
     *
     * @return true wenn aktiv
     */
    boolean isListening();

    /**
     * Startet den Hotkey-Recording-Modus.
     * Der nächste gedrückte Taste wird erfasst und die Binding zurückgegeben.
     *
     * @param timeout Zeitlimit in Millisekunden
     * @param callback wird aufgerufen, wenn eine Taste erfasst wurde oder Timeout eintritt
     */
    void startRecordingHotkey(long timeout, HotkeyRecordingCallback callback);

    /**
     * Stoppt den Hotkey-Recording-Modus.
     */
    void stopRecordingHotkey();

    /**
     * Prüft, ob Hotkey-Recording aktiv ist.
     */
    boolean isRecording();
}
