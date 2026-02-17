package org.example.robo.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.robo.config.ConfigurationManager;
import org.example.robo.core.input.KeyboardListener;
import org.example.robo.core.profile.ClickProfile;
import org.example.robo.core.profile.ClickType;
import org.example.robo.ui.dialog.ProfileManagerDialog;
import org.example.robo.ui.dialog.SettingsDialog;
import org.example.robo.util.Constants;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Hauptfenster der Click Roboter Anwendung (JavaFX).
 * Zeigt Status, Kontrolle und Profil-Verwaltung.
 */
public class MainWindowFX {
    private static final Logger logger = LoggerFactory.getLogger(MainWindowFX.class);

    private UIController controller;
    private List<ClickProfile> profiles;
    private ConfigurationManager configurationManager;
    private KeyboardListener keyboardListener;
    private Timer statusUpdateTimer;
    private Stage primaryStage;

    // UI Komponenten
    private Label statusLabel;
    private Label mousePositionLabel;
    private Label frequencyLabel;
    private Button startStopButton;
    private Button recordButton;
    private Button playButton;
    private ComboBox<String> profileSelector;

    /**
     * Erstellt und zeigt das Hauptfenster.
     */
    public void show(Stage stage, UIController controller, List<ClickProfile> profiles,
                     ConfigurationManager configurationManager, KeyboardListener keyboardListener) {
        this.primaryStage = stage;
        this.controller = controller;
        this.profiles = profiles;
        this.configurationManager = configurationManager;
        this.keyboardListener = keyboardListener;

        // Fenster-Konfiguration
        stage.setTitle(Constants.APP_NAME + " - " + Constants.APP_VERSION);
        stage.setWidth(Constants.MAIN_WINDOW_WIDTH);
        stage.setHeight(Constants.MAIN_WINDOW_HEIGHT);
        stage.setResizable(false);

        // Root Layout
        VBox root = createMainPanel();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        // On close event
        stage.setOnCloseRequest(e -> shutdown());

        // Zeige Fenster
        stage.show();

        // Starte Status-Update Timer
        startStatusUpdater();

        logger.info("MainWindow (JavaFX) initialized");
    }

    /**
     * Erstelle das Hauptpanel mit allen Sections.
     */
    private VBox createMainPanel() {
        VBox mainPanel = new VBox(10);
        mainPanel.setPadding(new Insets(10));

        // === Status Section ===
        VBox statusPanel = createStatusPanel();
        mainPanel.getChildren().add(statusPanel);

        // === Control Section ===
        VBox controlPanel = createControlPanel();
        mainPanel.getChildren().add(controlPanel);

        // === Profile Selection ===
        VBox profilePanel = createProfilePanel();
        mainPanel.getChildren().add(profilePanel);

        return mainPanel;
    }

    /**
     * Erstellt das Status-Panel.
     */
    private VBox createStatusPanel() {
        VBox panel = new VBox(5);
        panel.setStyle("-fx-border-color: #000000; -fx-border-width: 1; -fx-padding: 10;");
        panel.setPadding(new Insets(10));

        // Status Label
        statusLabel = new Label("Status: STOPPED");
        statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: red;");
        panel.getChildren().add(statusLabel);

        // Mausposition Label
        mousePositionLabel = new Label("Position: X=0 Y=0");
        panel.getChildren().add(mousePositionLabel);

        // Frequenz Label
        frequencyLabel = new Label("Frequency: 0 Hz");
        panel.getChildren().add(frequencyLabel);

        return panel;
    }

    /**
     * Erstellt das Kontroll-Panel.
     */
    private VBox createControlPanel() {
        VBox panel = new VBox();
        panel.setStyle("-fx-border-color: #000000; -fx-border-width: 1; -fx-padding: 10;");
        panel.setPadding(new Insets(10));

        Label controlTitle = new Label("Control");
        controlTitle.setStyle("-fx-font-weight: bold;");
        panel.getChildren().add(controlTitle);

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        // Start/Stop Button
        startStopButton = new Button("START");
        startStopButton.setPrefSize(120, 40);
        startStopButton.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        startStopButton.setOnAction(e -> onStartStopClicked());
        buttonBox.getChildren().add(startStopButton);

        // Record Button
        recordButton = new Button("RECORD");
        recordButton.setPrefSize(120, 40);
        recordButton.setOnAction(e -> onRecordClicked());
        buttonBox.getChildren().add(recordButton);

        // Play Button
        playButton = new Button("PLAY");
        playButton.setPrefSize(120, 40);
        playButton.setOnAction(e -> onPlayClicked());
        buttonBox.getChildren().add(playButton);

        // Capture Click Button
        Button captureClickButton = new Button("CAPTURE CLICK");
        captureClickButton.setPrefSize(140, 40);
        captureClickButton.setOnAction(e -> onCaptureClickClicked());
        buttonBox.getChildren().add(captureClickButton);

        // Settings Button
        Button settingsButton = new Button("Settings");
        settingsButton.setPrefSize(120, 40);
        settingsButton.setOnAction(e -> onSettingsClicked());
        buttonBox.getChildren().add(settingsButton);

        // Profile Manager Button
        Button profileManagerButton = new Button("Manage Profiles");
        profileManagerButton.setPrefSize(140, 40);
        profileManagerButton.setOnAction(e -> onProfileManagerClicked());
        buttonBox.getChildren().add(profileManagerButton);

        panel.getChildren().add(buttonBox);
        return panel;
    }

    /**
     * Erstellt das Profil-Auswahl-Panel.
     */
    private VBox createProfilePanel() {
        VBox panel = new VBox(5);
        panel.setStyle("-fx-border-color: #000000; -fx-border-width: 1; -fx-padding: 10;");
        panel.setPadding(new Insets(10));

        Label profileTitle = new Label("Profile");
        profileTitle.setStyle("-fx-font-weight: bold;");
        panel.getChildren().add(profileTitle);

        Label profileLabel = new Label("Select Profile:");
        panel.getChildren().add(profileLabel);

        String[] profileNames = profiles.stream()
                .map(p -> p.getName() + " (" + p.getClickFrequency() + " Hz)")
                .toArray(String[]::new);

        profileSelector = new ComboBox<>();
        for (String name : profileNames) {
            profileSelector.getItems().add(name);
        }
        if (profileNames.length > 0) {
            profileSelector.getSelectionModel().selectFirst();
        }
        profileSelector.setPrefWidth(Double.MAX_VALUE);
        profileSelector.setOnAction(e -> onProfileSelected());
        panel.getChildren().add(profileSelector);

        return panel;
    }

    // ===== Event Handler =====

    private void onStartStopClicked() {
        int selectedIndex = profileSelector.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < profiles.size()) {
            ClickProfile profile = profiles.get(selectedIndex);
            controller.toggleClicking(profile);
        } else {
            showError("Bitte wählen Sie ein Profil aus");
        }
    }

    private void onSettingsClicked() {
        SettingsDialog settingsDialog = new SettingsDialog(configurationManager, keyboardListener);
        settingsDialog.showAndWait();
    }

    private void onCaptureClickClicked() {
        try {
            MousePosition pos = controller.getCurrentMousePosition();
            controller.recordMouseClick(pos, ClickType.LEFT);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Captured");
            alert.setHeaderText("Click Captured");
            alert.setContentText("Captured click at X=" + pos.getX() + " Y=" + pos.getY());
            alert.showAndWait();
        } catch (Exception e) {
            showError("Failed to capture click: " + e.getMessage());
        }
    }

    private void onProfileSelected() {
        int selectedIndex = profileSelector.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < profiles.size()) {
            ClickProfile profile = profiles.get(selectedIndex);
            frequencyLabel.setText("Frequency: " + profile.getClickFrequency() + " Hz");
            mousePositionLabel.setText("Position: X=" + profile.getPosition().getX() +
                    " Y=" + profile.getPosition().getY());
        }
    }

    public void updateMousePosition(MousePosition position) {
        Platform.runLater(() -> {
            mousePositionLabel.setText("Position: X=" + position.getX() + " Y=" + position.getY());
        });
    }

    public void setEngineRunning(boolean isRunning) {
        Platform.runLater(() -> {
            if (isRunning) {
                statusLabel.setText("Status: ACTIVE");
                statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: green;");
                startStopButton.setText("STOP");
                startStopButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
            } else {
                statusLabel.setText("Status: STOPPED");
                statusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: red;");
                startStopButton.setText("START");
                startStopButton.setStyle("");
            }
        });
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void selectNextProfile() {
        int currentIndex = profileSelector.getSelectionModel().getSelectedIndex();
        int nextIndex = (currentIndex + 1) % profiles.size();
        profileSelector.getSelectionModel().select(nextIndex);
    }

    private void startStatusUpdater() {
        statusUpdateTimer = new Timer("StatusUpdater", true);
        statusUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MousePosition currentPos = controller.getCurrentMousePosition();
                updateMousePosition(currentPos);
                if (controller.isRecording()) {
                    controller.recordMouseMove(currentPos);
                }
            }
        }, Constants.MOUSE_POSITION_UPDATE_INTERVAL_MS, Constants.MOUSE_POSITION_UPDATE_INTERVAL_MS);
    }

    public void shutdown() {
        if (statusUpdateTimer != null) {
            statusUpdateTimer.cancel();
        }
    }

    private void onRecordClicked() {
        if (controller == null) return;
        if (!controller.isRecording()) {
            controller.startRecording("auto-" + System.currentTimeMillis(), "Recording");
            recordButton.setText("STOP REC");
            recordButton.setStyle("-fx-background-color: #ffa500;");
        } else {
            controller.stopRecording();

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save Macro");
            dialog.setHeaderText("Enter macro name:");
            dialog.setContentText("Name:");
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String name = result.get().trim();
                if (!name.isEmpty()) {
                    controller.saveCurrentMacro(name);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Saved");
                    alert.setHeaderText("Macro Saved");
                    alert.setContentText("Macro saved: " + name);
                    alert.showAndWait();
                }
            }

            recordButton.setText("RECORD");
            recordButton.setStyle("");
        }
    }

    private void onPlayClicked() {
        if (controller == null) return;
        if (!controller.isPlayingMacro()) {
            controller.playCurrentMacro();
            playButton.setText("STOP PLAY");
            playButton.setStyle("-fx-background-color: #90EE90;");
        } else {
            controller.stopMacroPlayback();
            playButton.setText("PLAY");
            playButton.setStyle("");
        }
    }

    private void onProfileManagerClicked() {
        ProfileManagerDialog profileManagerDialog = new ProfileManagerDialog(configurationManager);
        profileManagerDialog.showAndWait();
        // Neu lade die Profile nach dem Dialog
        if (controller != null) {
            profiles = configurationManager.getAllProfiles();
            // Aktualisiere die Profile-Liste in der UI
            updateProfileList();
        }
    }

    private void updateProfileList() {
        String[] profileNames = profiles.stream()
                .map(p -> p.getName() + " (" + p.getClickFrequency() + " Hz)")
                .toArray(String[]::new);

        profileSelector.getItems().clear();
        for (String name : profileNames) {
            profileSelector.getItems().add(name);
        }
        if (profileNames.length > 0) {
            profileSelector.getSelectionModel().selectFirst();
        }
    }
}
