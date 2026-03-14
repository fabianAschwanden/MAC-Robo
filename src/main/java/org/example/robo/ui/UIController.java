package org.example.robo.ui;

import org.example.robo.core.engine.ClickEngine;
import org.example.robo.core.engine.ClickEngineListener;
import org.example.robo.config.ConfigurationManager;
import org.example.robo.core.input.KeyboardEventListener;
import org.example.robo.core.input.HotkeyAction;
import org.example.robo.core.input.KeyboardListener;
import org.example.robo.core.profile.ClickProfile;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller für die UI und Verbindung zur Business Logic.
 * Verwendet das MVC-Pattern zur Entkopplung von UI und Logik.
 */
public class UIController implements ClickEngineListener, KeyboardEventListener {
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);

    private final ClickEngine clickEngine;
    private final KeyboardListener keyboardListener;
    private MainWindowFX mainWindow;
    private final org.example.robo.core.engine.MacroRecorder macroRecorder;
    private final org.example.robo.core.engine.MacroPlayer macroPlayer;
    private final ConfigurationManager configurationManager;

    /**
     * Erstellt einen neuen UIController.
     *
     * @param clickEngine die Click Engine
     * @param keyboardListener der Keyboard Listener
     */
    public UIController(ClickEngine clickEngine, KeyboardListener keyboardListener, ConfigurationManager configurationManager) {
        this.clickEngine = clickEngine;
        this.keyboardListener = keyboardListener;

        // Erstelle einfache Recorder/Player
        this.macroRecorder = new org.example.robo.core.engine.MacroRecorderImpl(clickEngine);
        this.macroPlayer = new org.example.robo.core.engine.MacroPlayerImpl();
        this.configurationManager = configurationManager;

        // Registriere als Listener
        this.clickEngine.addClickEngineListener(this);
        this.keyboardListener.addKeyboardEventListener(this);

        logger.info("UIController initialized");
    }

    // Recorder/Player API für UI
    public void startRecording(String id, String name) {
        macroRecorder.startRecording(id, name);
    }

    public void stopRecording() {
        macroRecorder.stopRecording();
    }

    /**
     * Speichert das aktuelle Macro unter dem gegebenen Namen über den ConfigurationManager.
     * Generiert eine ID wenn nötig.
     */
    public void saveCurrentMacro(String name) {
        org.example.robo.core.profile.Macro m = macroRecorder.getCurrentMacro();
        if (m == null) {
            logger.warn("No macro to save");
            return;
        }
        if (name == null || name.isBlank()) {
            name = "Macro - " + System.currentTimeMillis();
        }
        m.setName(name);
        if (m.getId() == null || m.getId().isEmpty()) {
            m.setId("macro-" + System.currentTimeMillis());
        }
        configurationManager.saveMacro(m);
    }

    public boolean isRecording() {
        return macroRecorder.isRecording();
    }

    public org.example.robo.core.profile.Macro getCurrentMacro() {
        return macroRecorder.getCurrentMacro();
    }

    public void playCurrentMacro() {
        org.example.robo.core.profile.Macro m = macroRecorder.getCurrentMacro();
        if (m != null) {
            macroPlayer.play(m);
        }
    }

    public void stopMacroPlayback() {
        macroPlayer.stop();
    }

    public boolean isPlayingMacro() {
        return macroPlayer.isPlaying();
    }

    // Recording helper methods (used by status updater)
    public void recordMouseMove(MousePosition pos) {
        if (macroRecorder instanceof org.example.robo.core.engine.MacroRecorderImpl rec) {
            rec.recordMouseMove(pos);
        }
    }

    public void recordMouseClick(MousePosition pos, org.example.robo.core.profile.ClickType type) {
        if (macroRecorder instanceof org.example.robo.core.engine.MacroRecorderImpl rec) {
            rec.recordMouseClick(pos, type);
        }
    }

    /**
     * Setzt das MainWindow (JavaFX).
     *
     * @param mainWindow das Hauptfenster
     */
    public void setMainWindow(MainWindowFX mainWindow) {
        this.mainWindow = mainWindow;
    }

    // ===== Business Logic Methods (aufgerufen von UI) =====

    /**
     * Startet oder stoppt die Klick-Automatisierung.
     *
     * @param profile das zu verwendende Profil
     */
    public void toggleClicking(ClickProfile profile) {
        if (clickEngine.isRunning()) {
            clickEngine.stopClicking();
        } else {
            clickEngine.startClicking(profile);
        }
    }

    /**
     * Setzt die Klick-Frequenz.
     *
     * @param hz Frequenz in Hz
     */
    public void setClickFrequency(int hz) {
        clickEngine.setClickFrequency(hz);
    }

    /**
     * Setzt die Klick-Position.
     *
     * @param position neue Position
     */
    public void setClickPosition(MousePosition position) {
        clickEngine.setClickPosition(position);
    }

    /**
     * Gibt die aktuelle Mausposition zurück.
     *
     * @return aktuelle Mausposition
     */
    public MousePosition getCurrentMousePosition() {
        return clickEngine.getCurrentMousePosition();
    }

    /**
     * Gibt das aktuelle aktive Profil zurück.
     *
     * @return das aktive Profil
     */
    public ClickProfile getCurrentProfile() {
        return clickEngine.getCurrentProfile();
    }

    /**
     * Gibt an ob Klicks gerade aktiv sind.
     *
     * @return true wenn aktiv
     */
    public boolean isClickingActive() {
        return clickEngine.isRunning();
    }

    // ===== ClickEngine Listener Implementation =====

    @Override
    public void onClickExecuted(MousePosition position) {
        if (mainWindow != null) {
            mainWindow.updateMousePosition(position);
        }
    }

    @Override
    public void onEngineStarted() {
        logger.debug("Engine started - updating UI");
        if (mainWindow != null) {
            mainWindow.setEngineRunning(true);
        }
    }

    @Override
    public void onEngineStopped() {
        logger.debug("Engine stopped - updating UI");
        if (mainWindow != null) {
            mainWindow.setEngineRunning(false);
        }
    }

    @Override
    public void onError(String errorMessage) {
        logger.error("Engine error: {}", errorMessage);
        if (mainWindow != null) {
            mainWindow.showError(errorMessage);
        }
    }

    // ===== Keyboard Event Listener Implementation =====

    @Override
    public void onHotkeyAction(HotkeyAction action) {
        logger.debug("Hotkey action received: {}", action);

        switch (action) {
            case START_STOP -> {
                ClickProfile profile = getCurrentProfile();
                if (profile != null) {
                    toggleClicking(profile);
                }
            }
            case EMERGENCY_STOP -> {
                clickEngine.stopClicking();
                logger.info("Emergency stop triggered");
            }
            case NEXT_PROFILE -> {
                if (mainWindow != null) {
                    mainWindow.selectNextProfile();
                }
            }
        }
    }

    @Override
    public void onKeyboardEvent(int keyCode, int modifiers) {
        // Wird für allgemeine Keyboard Events verwendet (Phase 2+)
    }

    /**
     * Shutdown - Cleanup
     */
    public void shutdown() {
        clickEngine.removeClickEngineListener(this);
        keyboardListener.removeKeyboardEventListener(this);
    }
}
