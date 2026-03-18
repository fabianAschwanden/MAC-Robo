package org.example.robo.core.input;

/**
 * Listener Interface für Keyboard Events.
 */
public interface KeyboardEventListener {

    /**
     * Wird aufgerufen, wenn eine Hotkey-Aktion ausgelöst wurde.
     *
     * @param action die ausgelöste Aktion
     */
    void onHotkeyAction(HotkeyAction action);

    /**
     * Wird aufgerufen, wenn ein allgemeines Keyboard Event auftritt.
     *
     * @param keyCode der gedrückte Tastencode
     * @param modifiers die gedrückten Modifikatoren
     */
    void onKeyboardEvent(int keyCode, int modifiers);
}

