package org.example.robo.core.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation des KeyboardListener Interfaces.
 * Vereinfachte Version für MVP - wird später mit globalen Hotkeys erweitert.
 *
 * Hinweis: Für vollständige globale Hotkey-Unterstützung auf macOS wird
 * JNativeHook oder native JNI/JNA Integration erforderlich.
 */
public class KeyboardListenerImpl implements KeyboardListener {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardListenerImpl.class);

    private final Map<Integer, HotkeyAction> hotkeyMap;
    private final List<KeyboardEventListener> listeners;
    private volatile boolean isListening = false;
    private volatile boolean isRecording = false;
    private HotkeyRecordingCallback recordingCallback;
    private java.util.Timer recordingTimer;

    /**
     * Erstellt eine neue KeyboardListener Instanz.
     */
    public KeyboardListenerImpl() {
        this.hotkeyMap = new HashMap<>();
        this.listeners = new ArrayList<>();
        logger.info("KeyboardListener initialized");
    }

    @Override
    public void registerHotkey(int keyCode, HotkeyAction action) {
        hotkeyMap.put(keyCode, action);
        logger.debug("Hotkey registered: {} -> {}", keyCode, action);
    }

    @Override
    public void unregisterHotkey(int keyCode) {
        hotkeyMap.remove(keyCode);
        logger.debug("Hotkey unregistered: {}", keyCode);
    }

    @Override
    public void addKeyboardEventListener(KeyboardEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
            logger.debug("Keyboard event listener added");
        }
    }

    @Override
    public void removeKeyboardEventListener(KeyboardEventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            logger.debug("Keyboard event listener removed");
        }
    }

    @Override
    public void startListening() {
        if (isListening) {
            logger.debug("Keyboard listener already running");
            return;
        }

        try {
            isListening = true;
            logger.info("Global keyboard listener started (MVP Mode - limited functionality)");
            logger.warn("For full hotkey support, please configure hotkeys in application settings");
        } catch (Exception e) {
            logger.error("Error starting keyboard listener", e);
        }
    }

    @Override
    public void stopListening() {
        if (!isListening) {
            logger.debug("Keyboard listener already stopped");
            return;
        }

        try {
            isListening = false;
            logger.info("Global keyboard listener stopped");
        } catch (Exception e) {
            logger.error("Error stopping keyboard listener", e);
        }
    }

    @Override
    public boolean isListening() {
        return isListening;
    }

    /**
     * Manuell Hotkey Action auslösen (für Testing und interne Nutzung).
     */
    public void triggerHotkeyAction(HotkeyAction action) {
        if (action != null) {
            notifyListeners(listener -> listener.onHotkeyAction(action));
        }
    }

    /**
     * Benachrichtige alle registrierten Listener.
     */
    private void notifyListeners(ListenerAction action) {
        for (KeyboardEventListener listener : new ArrayList<>(listeners)) {
            try {
                action.execute(listener);
            } catch (Exception e) {
                logger.error("Error notifying keyboard listener", e);
            }
        }
    }

    /**
     * Funktionales Interface für Listener-Aktionen.
     */
    @FunctionalInterface
    private interface ListenerAction {
        void execute(KeyboardEventListener listener);
    }

    /**
     * Shutdown - cleanup Ressourcen
     */
    public void shutdown() {
        if (isListening) {
            stopListening();
        }
    }

    @Override
    public void startRecordingHotkey(long timeout, HotkeyRecordingCallback callback) {
        if (isRecording) {
            logger.warn("Hotkey recording already in progress");
            return;
        }

        isRecording = true;
        recordingCallback = callback;
        logger.info("Hotkey recording started (timeout: {} ms)", timeout);

        // Starte Timeout-Timer
        recordingTimer = new java.util.Timer("HotkeyRecordingTimer", true);
        recordingTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                stopRecordingHotkey();
                if (recordingCallback != null) {
                    recordingCallback.onHotkeyRecorded(null); // null = Timeout
                }
            }
        }, timeout);
    }

    @Override
    public void stopRecordingHotkey() {
        if (!isRecording) {
            return;
        }

        isRecording = false;
        if (recordingTimer != null) {
            recordingTimer.cancel();
            recordingTimer = null;
        }
        recordingCallback = null;
        logger.debug("Hotkey recording stopped");
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Wird aufgerufen, wenn eine Taste während Recording gedrückt wird.
     * Nur für interne Nutzung - würde in echter Implementierung von globalem Listener aufgerufen.
     */
    public void onKeyPressed(int keyCode, int modifiers) {
        if (isRecording && recordingCallback != null) {
            HotkeyBinding binding = new HotkeyBinding(keyCode, modifiers, null);
            stopRecordingHotkey();
            recordingCallback.onHotkeyRecorded(binding);
        }
    }
}
