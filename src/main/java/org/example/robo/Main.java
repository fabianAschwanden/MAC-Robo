package org.example.robo;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.robo.core.profile.ClickProfile;
import org.example.robo.service.ApplicationService;
import org.example.robo.ui.MainWindowFX;
import org.example.robo.ui.UIController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Haupteinstiegspunkt für die Click Roboter Anwendung (JavaFX).
 */
public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static ApplicationService appService;
    private static List<ClickProfile> profiles;

    public static void main(String[] args) {
        try {
            logger.info("=== Click Roboter Application Starting ===");

            // Initialisiere Application Service (Singleton)
            appService = ApplicationService.getInstance();

            // Lade Profile
            profiles = appService.getAllProfiles();
            logger.info("Loaded {} profiles", profiles.size());

            // Shutdown Hook für graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received");
                appService.shutdown();
            }));

            // Starte JavaFX Application
            Application.launch(args);

        } catch (Exception e) {
            logger.error("Fatal error during application startup", e);
            showErrorAndExit("Fatal error during startup", e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting JavaFX UI");

            // Überprüfe Accessibility
            checkAccessibility();

            // Erstelle UI Controller
            UIController uiController = new UIController(
                    appService.getClickEngine(),
                    appService.getKeyboardListener(),
                    appService.getConfigurationManager()
            );

            // Erstelle und zeige Hauptfenster
            MainWindowFX mainWindow = new MainWindowFX();
            mainWindow.show(primaryStage, uiController, profiles,
                    appService.getConfigurationManager(),
                    appService.getKeyboardListener());
            uiController.setMainWindow(mainWindow);

            logger.info("=== Click Roboter Application Started ===");
            logger.info("Hotkeys configured:");
            logger.info("  F6          = Start/Stop");
            logger.info("  F7          = Emergency Stop");
            logger.info("  F8          = Next Profile");

        } catch (Exception e) {
            logger.error("Error starting UI", e);
            showErrorAndExit("Failed to start application UI", e);
        }
    }

    /**
     * Überprüft und benachrichtig den Benutzer über Accessibility-Anforderungen.
     */
    private static void checkAccessibility() {
        try {
            // Vereinfachte Prüfung - auf echten macOS wird dies durch NativeHook geprüft
            logger.info("Accessibility check: If you see this, the app may need Accessibility permission");
            logger.info("If hotkeys don't work, please grant Accessibility permission in:");
            logger.info("  System Preferences > Security & Privacy > Accessibility > Click Roboter");
        } catch (Exception e) {
            logger.warn("Could not verify accessibility permission", e);
        }
    }

    /**
     * Zeigt einen Fehler und beendet die Anwendung.
     */
    private static void showErrorAndExit(String message, Exception exception) {
        logger.error(message, exception);
        // JavaFX Alert kann nicht vor Platform-Start verwendet werden
        System.err.println(message + (exception != null ? "\nError: " + exception.getMessage() : ""));
        System.exit(1);
    }
}
