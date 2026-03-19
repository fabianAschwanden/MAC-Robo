package ch.aschwanden.robo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ch.aschwanden.robo.core.engine.NativeMacOSAPI;
import ch.aschwanden.robo.core.profile.ClickProfile;
import ch.aschwanden.robo.service.ApplicationService;
import ch.aschwanden.robo.ui.MainWindowFX;
import ch.aschwanden.robo.ui.UIController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Haupteinstiegspunkt für die MACRobo Anwendung (JavaFX).
 */
public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static ApplicationService appService;
    private static List<ClickProfile> profiles;

    public static void main(String[] args) {
        try {
            logger.info("=== MACRobo Application Starting ===");

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

            logger.info("=== MACRobo Application Started ===");
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
     * Überprüft Accessibility-Berechtigung und zeigt Warnung falls sie fehlt.
     */
    private static void checkAccessibility() {
        if (!NativeMacOSAPI.hasAccessibilityPermission()) {
            logger.warn("Accessibility permission missing — mouse clicks will not work");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Bedienungshilfen-Berechtigung fehlt");
                alert.setHeaderText("Mausklicks werden nicht ausgeführt");
                alert.setContentText(
                    "Bitte erteile die Berechtigung in:\n\n" +
                    "Systemeinstellungen → Datenschutz & Sicherheit → Bedienungshilfen\n\n" +
                    "Füge dort den Terminal (oder die App) hinzu und starte die App neu."
                );
                alert.showAndWait();
            });
        } else {
            logger.info("Accessibility permission: granted");
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
