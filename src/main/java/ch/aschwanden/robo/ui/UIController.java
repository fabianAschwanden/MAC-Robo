package ch.aschwanden.robo.ui;

import ch.aschwanden.robo.core.engine.ClickEngine;
import ch.aschwanden.robo.core.engine.ClickEngineListener;
import ch.aschwanden.robo.config.ConfigurationManager;
import ch.aschwanden.robo.core.input.KeyboardEventListener;
import ch.aschwanden.robo.core.input.HotkeyAction;
import ch.aschwanden.robo.core.input.KeyboardListener;
import ch.aschwanden.robo.core.input.HotkeyBinding;
import ch.aschwanden.robo.core.profile.ClickProfile;
import ch.aschwanden.robo.core.profile.ClickType;
import ch.aschwanden.robo.core.profile.RobustSelector;
import ch.aschwanden.robo.core.profile.SpeedMode;
import ch.aschwanden.robo.core.profile.WebEventType;
import ch.aschwanden.robo.core.profile.WebRecordingStep;
import ch.aschwanden.robo.core.capture.CaptureEvent;
import ch.aschwanden.robo.core.capture.CaptureServer;
import ch.aschwanden.robo.util.Constants;
import ch.aschwanden.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javafx.application.Platform;

/**
 * Controller für die UI und Verbindung zur Business Logic.
 */
public class UIController implements ClickEngineListener, KeyboardEventListener {
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);

    private final ClickEngine clickEngine;
    private final KeyboardListener keyboardListener;
    private final ConfigurationManager configurationManager;
    private MainWindowFX mainWindow;

    private final ch.aschwanden.robo.core.engine.MacroRecorder macroRecorder;
    private final ch.aschwanden.robo.core.engine.MacroPlayer macroPlayer;
    private final ch.aschwanden.robo.core.engine.WebMacroRecorderImpl webMacroRecorder;
    private CaptureServer captureServer;

    public UIController(ClickEngine clickEngine, KeyboardListener keyboardListener,
                        ConfigurationManager configurationManager) {
        this.clickEngine = clickEngine;
        this.keyboardListener = keyboardListener;
        this.configurationManager = configurationManager;
        this.macroRecorder = new ch.aschwanden.robo.core.engine.MacroRecorderImpl(clickEngine);
        this.macroPlayer = new ch.aschwanden.robo.core.engine.MacroPlayerImpl();
        this.webMacroRecorder = new ch.aschwanden.robo.core.engine.WebMacroRecorderImpl();

        this.clickEngine.addClickEngineListener(this);
        this.keyboardListener.addKeyboardEventListener(this);

        logger.info("UIController initialized");
    }

    public void setMainWindow(MainWindowFX mainWindow) {
        this.mainWindow = mainWindow;
    }

    // ===== Multi-Profile Klick-Steuerung =====

    /**
     * Startet alle aktivierten Profile oder stoppt alle laufenden.
     */
    public void toggleClicking(List<ClickProfile> profiles) {
        if (clickEngine.isRunning()) {
            clickEngine.stopClicking();
        } else {
            List<ClickProfile> enabled = profiles.stream()
                    .filter(ClickProfile::isEnabled)
                    .toList();
            if (enabled.isEmpty()) {
                if (mainWindow != null) mainWindow.showError("Bitte mindestens einen Eintrag aktivieren.");
                return;
            }
            clickEngine.startClicking(enabled);
        }
    }

    /** Startet/stoppt ein einzelnes Profil. */
    public void toggleClicking(ClickProfile profile) {
        if (clickEngine.isRunning(profile.getId())) {
            clickEngine.stopClicking(profile.getId());
        } else {
            clickEngine.startClicking(profile);
        }
    }

    /** Stoppt alle laufenden Klicks. */
    public void stopAll() {
        clickEngine.stopClicking();
    }

    // ===== Profil / Eintrag Verwaltung =====

    public void saveProfile(ClickProfile profile) {
        configurationManager.saveProfile(profile);
    }

    public void deleteProfile(String profileId) {
        clickEngine.stopClicking(profileId);
        configurationManager.deleteProfile(profileId);
    }

    public List<ClickProfile> getAllProfiles() {
        return configurationManager.getAllProfiles();
    }

    /**
     * Setzt die Position eines Eintrags auf die aktuelle Mausposition.
     */
    public MousePosition captureAndSetPosition(ClickProfile profile) {
        MousePosition pos = clickEngine.getCurrentMousePosition();
        profile.setPosition(pos);
        configurationManager.saveProfile(profile);
        return pos;
    }

    /**
     * Aktualisiert den enabled-Status und speichert.
     */
    public void setEntryEnabled(ClickProfile profile, boolean enabled) {
        profile.setEnabled(enabled);
        configurationManager.saveProfile(profile);
    }

    /**
     * Aktualisiert den Klick-Typ und speichert.
     */
    public void setClickType(ClickProfile profile, ClickType type) {
        profile.setClickType(type);
        configurationManager.saveProfile(profile);
    }

    /**
     * Parst und setzt den Speed-Wert eines Profils aus einem Anzeigestring wie "3 / sec" oder "4 sec click once".
     */
    public void setSpeed(ClickProfile profile, String speedText) {
        if (speedText == null) return;
        speedText = speedText.trim();
        if (speedText.endsWith("/ sec") || speedText.endsWith("/sec")) {
            try {
                int hz = Integer.parseInt(speedText.replaceAll("[^0-9]", "").trim());
                profile.setSpeedMode(SpeedMode.FREQUENCY);
                profile.setClickFrequency(hz);
            } catch (NumberFormatException ignored) {}
        } else if (speedText.contains("sec click once")) {
            try {
                int secs = Integer.parseInt(speedText.replaceAll("[^0-9]", "").trim());
                profile.setSpeedMode(SpeedMode.INTERVAL);
                profile.setIntervalSeconds(secs);
            } catch (NumberFormatException ignored) {}
        } else {
            try {
                int hz = Integer.parseInt(speedText.trim());
                profile.setSpeedMode(SpeedMode.FREQUENCY);
                profile.setClickFrequency(hz);
            } catch (NumberFormatException ignored) {}
        }
        configurationManager.saveProfile(profile);
    }

    // ===== Allgemeine Hilfsmethoden =====

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public MousePosition getCurrentMousePosition() {
        return clickEngine.getCurrentMousePosition();
    }

    public boolean isClickingActive() {
        return clickEngine.isRunning();
    }

    public void setClickFrequency(int hz) {
        clickEngine.setClickFrequency(hz);
    }

    public void setClickPosition(MousePosition position) {
        clickEngine.setClickPosition(position);
    }

    public ClickProfile getCurrentProfile() {
        return clickEngine.getCurrentProfile();
    }

    // ===== Macro API =====

    public void startRecording(String id, String name) {
        macroRecorder.startRecording(id, name);
    }

    public void stopRecording() {
        macroRecorder.stopRecording();
    }

    public void saveCurrentMacro(String name) {
        ch.aschwanden.robo.core.profile.Macro m = macroRecorder.getCurrentMacro();
        if (m == null) return;
        if (name == null || name.isBlank()) name = "Macro - " + System.currentTimeMillis();
        m.setName(name);
        if (m.getId() == null || m.getId().isEmpty()) m.setId("macro-" + System.currentTimeMillis());
        configurationManager.saveMacro(m);
    }

    public boolean isRecording() {
        return macroRecorder.isRecording();
    }

    public void recordMouseMove(MousePosition pos) {
        if (macroRecorder instanceof ch.aschwanden.robo.core.engine.MacroRecorderImpl rec) {
            rec.recordMouseMove(pos);
        }
    }

    public void recordMouseClick(MousePosition pos, ClickType type) {
        if (macroRecorder instanceof ch.aschwanden.robo.core.engine.MacroRecorderImpl rec) {
            rec.recordMouseClick(pos, type);
        }
    }

    public void playCurrentMacro() {
        ch.aschwanden.robo.core.profile.Macro m = macroRecorder.getCurrentMacro();
        if (m != null) macroPlayer.play(m);
    }

    public void stopMacroPlayback() {
        macroPlayer.stop();
    }

    public boolean isPlayingMacro() {
        return macroPlayer.isPlaying();
    }

    // ===== ClickEngine Listener =====

    @Override
    public void onClickExecuted(MousePosition position) {
        // Position-Updates werden in MainWindowFX nicht mehr angezeigt (inline in Tabelle)
    }

    @Override
    public void onEngineStarted() {
        logger.debug("Engine started");
        if (mainWindow != null) mainWindow.setEngineRunning(true);
    }

    @Override
    public void onEngineStopped() {
        logger.debug("Engine stopped");
        if (mainWindow != null) mainWindow.setEngineRunning(false);
    }

    @Override
    public void onError(String errorMessage) {
        logger.error("Engine error: {}", errorMessage);
        if (mainWindow != null) mainWindow.showError(errorMessage);
    }

    // ===== Web Capture Hotkey =====

    private int webCaptureHotkeyCode = -1;

    /**
     * Registriert einen neuen Hotkey für Web Capture Toggle.
     * Ein zuvor registrierter Hotkey wird zuerst entfernt.
     */
    public void setWebCaptureHotkey(HotkeyBinding binding) {
        if (webCaptureHotkeyCode >= 0) {
            keyboardListener.unregisterHotkey(webCaptureHotkeyCode);
        }
        webCaptureHotkeyCode = binding.getKeyCode();
        keyboardListener.registerHotkey(webCaptureHotkeyCode, HotkeyAction.WEB_CAPTURE_TOGGLE);
        logger.info("Web-Capture-Hotkey gesetzt: {}", binding.getHotkeyString());
    }

    public void clearWebCaptureHotkey() {
        if (webCaptureHotkeyCode >= 0) {
            keyboardListener.unregisterHotkey(webCaptureHotkeyCode);
            webCaptureHotkeyCode = -1;
        }
    }

    // ===== Web Recording API =====

    public void startWebRecording() {
        String id = "web-" + System.currentTimeMillis();
        webMacroRecorder.startRecording(id, "Web Capture " + id);
        captureServer = new CaptureServer(Constants.CAPTURE_SERVER_PORT);
        captureServer.addListener(this::onBrowserEvent);
        try {
            captureServer.start();
        } catch (java.io.IOException e) {
            logger.error("CaptureServer konnte nicht gestartet werden", e);
            if (mainWindow != null) mainWindow.showError("Capture-Server Fehler (Port " + Constants.CAPTURE_SERVER_PORT + "): " + e.getMessage());
        }
    }

    public void stopWebRecording() {
        webMacroRecorder.stopRecording();
        if (captureServer != null) {
            captureServer.stop();
            captureServer = null;
        }
    }

    private void onBrowserEvent(CaptureEvent event) {
        try {
            WebEventType type = WebEventType.valueOf(event.eventType.toUpperCase());
            RobustSelector selector = RobustSelector.of(event.cssSelector, event.xpath, event.textContent);
            webMacroRecorder.recordStep(type, selector, event.payload);
            if (mainWindow != null) {
                Platform.runLater(mainWindow::onWebStepReceived);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Unbekannter Browser-EventType: {}", event.eventType);
        }
    }

    public boolean isCaptureServerRunning() {
        return captureServer != null && captureServer.isRunning();
    }

    public int getCaptureServerPort() {
        return Constants.CAPTURE_SERVER_PORT;
    }

    public boolean isWebRecording() {
        return webMacroRecorder.isRecording();
    }

    public List<WebRecordingStep> getWebRecordedSteps() {
        return webMacroRecorder.getRecordedSteps();
    }

    public void setWebIdleThreshold(long thresholdMs) {
        webMacroRecorder.setIdleThresholdMs(thresholdMs);
    }

    public void recordWebStep(WebEventType eventType, RobustSelector selector, String payload) {
        webMacroRecorder.recordStep(eventType, selector, payload);
    }

    public void clearWebRecording() {
        webMacroRecorder.clearSteps();
    }

    // ===== Keyboard Listener =====

    @Override
    public void onHotkeyAction(HotkeyAction action) {
        logger.debug("Hotkey action: {}", action);
        switch (action) {
            case START_STOP -> toggleClicking(configurationManager.getAllProfiles());
            case EMERGENCY_STOP -> stopAll();
            case NEXT_PROFILE -> { /* nicht mehr relevant in neuer UI */ }
            case WEB_CAPTURE_TOGGLE -> { if (mainWindow != null) mainWindow.triggerWebCaptureToggle(); }
        }
    }

    @Override
    public void onKeyboardEvent(int keyCode, int modifiers) {
        // unused
    }

    public void shutdown() {
        clickEngine.removeClickEngineListener(this);
        keyboardListener.removeKeyboardEventListener(this);
    }
}
