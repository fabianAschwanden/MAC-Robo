package ch.aschwanden.robo.ui;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import ch.aschwanden.robo.config.ConfigurationManager;
import ch.aschwanden.robo.config.ConfigurationManagerImpl;
import ch.aschwanden.robo.core.engine.ClickEngine;
import ch.aschwanden.robo.core.engine.ClickEngineListener;
import ch.aschwanden.robo.core.input.HotkeyAction;
import ch.aschwanden.robo.core.input.KeyboardEventListener;
import ch.aschwanden.robo.core.input.KeyboardListener;
import ch.aschwanden.robo.core.profile.ClickProfile;
import ch.aschwanden.robo.util.MousePosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFX UI-Tests für MainWindowFX.
 * Nutzt Stubs für native Dependencies (ClickEngine, KeyboardListener),
 * um Tests ohne macOS-Accessibility-Berechtigung ausführen zu können.
 */
@ExtendWith(ApplicationExtension.class)
class MainWindowFXTest {

    private MainWindowFX mainWindow;
    private UIController uiController;

    @Start
    void start(Stage stage) {
        ConfigurationManager configManager = new ConfigurationManagerImpl();
        ClickEngine clickEngine           = new StubClickEngine();
        KeyboardListener keyboardListener  = new StubKeyboardListener();

        uiController = new UIController(clickEngine, keyboardListener, configManager);
        mainWindow   = new MainWindowFX();

        List<ClickProfile> profiles = configManager.getAllProfiles();
        mainWindow.show(stage, uiController, profiles, configManager, keyboardListener);
    }

    @AfterEach
    void tearDown() {
        // UIController-Shutdown via FX-Thread nicht nötig; Stubs haben keine Ressourcen
    }

    // ── Fenster-Titel ──────────────────────────────────────────────────────────

    @Test
    void testWindowTitle(FxRobot robot) {
        robot.interact(() ->
            assertEquals("MACRobo", ((Stage) robot.window(0)).getTitle())
        );
    }

    // ── Sidebar sichtbar ───────────────────────────────────────────────────────

    @Test
    void testAutoClickButtonExists(FxRobot robot) {
        // MACRobo-Button hat Tooltip "MACRobo"
        assertFalse(robot.lookup(".sidebar-btn").queryAll().isEmpty(),
                "Sidebar-Buttons sollten vorhanden sein");
    }

    // ── Icon-Farbwechsel beim Sidebar-Switch ───────────────────────────────────

    @Test
    void testInitialAutoClickViewActive(FxRobot robot) {
        // Beim Start ist der MACRobo-Button aktiv (CSS-Klasse sidebar-btn-active)
        long activeCount = robot.lookup(".sidebar-btn-active").queryAll().size();
        assertEquals(1, activeCount, "Genau ein Button soll initial aktiv sein");
    }

    @Test
    void testSwitchToWebCaptureChangesActiveButton(FxRobot robot) {
        // Alle Sidebar-Buttons finden und den zweiten (Web Capture) klicken
        var sidebarBtns = robot.lookup(".sidebar-btn").queryAll().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .toList();

        assertTrue(sidebarBtns.size() >= 2, "Mindestens 2 Sidebar-Buttons erwartet");

        Button webCaptureBtn = sidebarBtns.get(1);
        robot.clickOn(webCaptureBtn);

        robot.interact(() -> {
            assertTrue(webCaptureBtn.getStyleClass().contains("sidebar-btn-active"),
                    "Web-Capture-Button soll nach Klick aktiv sein");
            assertFalse(sidebarBtns.get(0).getStyleClass().contains("sidebar-btn-active"),
                    "MACRobo-Button soll nach Wechsel inaktiv sein");
        });
    }

    @Test
    void testSwitchBackToAutoClickRestoresActiveButton(FxRobot robot) {
        var sidebarBtns = robot.lookup(".sidebar-btn").queryAll().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .toList();

        Button autoClickBtn  = sidebarBtns.get(0);
        Button webCaptureBtn = sidebarBtns.get(1);

        // Zu Web Capture wechseln, dann zurück
        robot.clickOn(webCaptureBtn);
        robot.clickOn(autoClickBtn);

        robot.interact(() -> {
            assertTrue(autoClickBtn.getStyleClass().contains("sidebar-btn-active"),
                    "MACRobo-Button soll wieder aktiv sein");
            assertFalse(webCaptureBtn.getStyleClass().contains("sidebar-btn-active"),
                    "Web-Capture-Button soll wieder inaktiv sein");
        });
    }

    @Test
    void testIconGraphicChangesOnSwitch(FxRobot robot) {
        var sidebarBtns = robot.lookup(".sidebar-btn").queryAll().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .toList();

        Button autoClickBtn  = sidebarBtns.get(0);
        Button webCaptureBtn = sidebarBtns.get(1);

        var initialAutoGraphic = autoClickBtn.getGraphic();
        var initialWebGraphic  = webCaptureBtn.getGraphic();

        robot.clickOn(webCaptureBtn);

        robot.interact(() -> {
            // Icon-Canvas-Objekte sollen ausgetauscht worden sein
            assertNotSame(initialAutoGraphic, autoClickBtn.getGraphic(),
                    "MACRobo-Icon soll bei Deaktivierung neu gezeichnet werden");
            assertNotSame(initialWebGraphic, webCaptureBtn.getGraphic(),
                    "Web-Capture-Icon soll bei Aktivierung neu gezeichnet werden");
        });
    }

    // ── Start-Click Button ─────────────────────────────────────────────────────

    @Test
    void testStartClickButtonExists(FxRobot robot) {
        // Der Start-Button trägt den Text "Start Click"
        assertFalse(robot.lookup("Start Click").queryAll().isEmpty(),
                "Start-Click-Button soll vorhanden sein");
    }

    // ── Stubs ──────────────────────────────────────────────────────────────────

    private static class StubClickEngine implements ClickEngine {
        @Override public void startClicking(ClickProfile profile) {}
        @Override public void startClicking(List<ClickProfile> profiles) {}
        @Override public void stopClicking() {}
        @Override public void stopClicking(String profileId) {}
        @Override public boolean isRunning() { return false; }
        @Override public boolean isRunning(String profileId) { return false; }
        @Override public void setClickFrequency(int hz) {}
        @Override public void setClickPosition(MousePosition position) {}
        @Override public MousePosition getCurrentMousePosition() { return new MousePosition(0, 0); }
        @Override public void addClickEngineListener(ClickEngineListener l) {}
        @Override public void removeClickEngineListener(ClickEngineListener l) {}
        @Override public ClickProfile getCurrentProfile() { return null; }
    }

    private static class StubKeyboardListener implements KeyboardListener {
        @Override public void registerHotkey(int keyCode, HotkeyAction action) {}
        @Override public void unregisterHotkey(int keyCode) {}
        @Override public void addKeyboardEventListener(KeyboardEventListener l) {}
        @Override public void removeKeyboardEventListener(KeyboardEventListener l) {}
        @Override public void startListening() {}
        @Override public void stopListening() {}
        @Override public boolean isListening() { return false; }
        @Override public void startRecordingHotkey(long timeout, ch.aschwanden.robo.core.input.HotkeyRecordingCallback callback) {}
        @Override public void stopRecordingHotkey() {}
        @Override public boolean isRecording() { return false; }
    }
}
