package org.example.robo.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.robo.config.ConfigurationManager;
import org.example.robo.core.input.HotkeyBinding;
import org.example.robo.core.input.KeyboardListener;
import org.example.robo.core.profile.ClickType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Einstellungs-Dialog mit Tabs für verschiedene Konfigurationsbereiche.
 */
public class SettingsDialog extends Stage {
    private static final Logger logger = LoggerFactory.getLogger(SettingsDialog.class);

    private final ConfigurationManager configurationManager;
    private final KeyboardListener keyboardListener;

    public SettingsDialog(ConfigurationManager configurationManager, KeyboardListener keyboardListener) {
        this.configurationManager = configurationManager;
        this.keyboardListener = keyboardListener;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Settings");
        setWidth(500);
        setHeight(400);
        setResizable(true);
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Titel
        Label titleLabel = new Label("Application Settings");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Tab Pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Hotkeys Tab
        Tab hotkeysTab = createHotkeysTab();
        tabPane.getTabs().add(hotkeysTab);

        // Click Types Tab
        Tab clickTypesTab = createClickTypesTab();
        tabPane.getTabs().add(clickTypesTab);

        // Advanced Tab
        Tab advancedTab = createAdvancedTab();
        tabPane.getTabs().add(advancedTab);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button okButton = new Button("OK");
        okButton.setPrefWidth(100);
        okButton.setOnAction(e -> onOK());

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(cancelButton, okButton);

        root.getChildren().addAll(
                titleLabel,
                new Separator(),
                tabPane,
                new Separator(),
                buttonBox
        );

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private Tab createHotkeysTab() {
        Tab tab = new Tab("Hotkeys");
        tab.setClosable(false);
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // Start/Stop Hotkey
        HBox startStopBox = createHotkeyRow("Start/Stop", "F6");
        content.getChildren().add(startStopBox);

        // Emergency Stop Hotkey (read-only)
        HBox emergencyStopBox = createHotkeyRow("Emergency Stop", "F7 (Fixed)");
        content.getChildren().add(emergencyStopBox);

        // Next Profile Hotkey
        HBox nextProfileBox = createHotkeyRow("Next Profile", "F8");
        content.getChildren().add(nextProfileBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createClickTypesTab() {
        Tab tab = new Tab("Click Types");
        tab.setClosable(false);
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label descLabel = new Label("Default click type for profiles:");
        descLabel.setStyle("-fx-font-size: 12;");

        ComboBox<ClickType> clickTypeCombo = new ComboBox<>();
        clickTypeCombo.getItems().addAll(ClickType.values());
        clickTypeCombo.setValue(ClickType.LEFT);
        clickTypeCombo.setPrefWidth(150);

        Label infoLabel = new Label(
                "• LEFT: Single left-click\n" +
                "• RIGHT: Right-click (context menu)\n" +
                "• SCROLL: Mouse scroll wheel"
        );
        infoLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        content.getChildren().addAll(descLabel, clickTypeCombo, new Separator(), infoLabel);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createAdvancedTab() {
        Tab tab = new Tab("Advanced");
        tab.setClosable(false);
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label freqLabel = new Label("Default Click Frequency (Hz):");
        Spinner<Integer> freqSpinner = new Spinner<>(1, 100, 10);
        freqSpinner.setPrefWidth(150);

        Label delayLabel = new Label("Default Delay Between Clicks (ms):");
        Spinner<Integer> delaySpinner = new Spinner<>(0, 5000, 100, 50);
        delaySpinner.setPrefWidth(150);

        CheckBox startMinimizedCheck = new CheckBox("Start application minimized");
        CheckBox enableNotificationsCheck = new CheckBox("Enable desktop notifications");

        content.getChildren().addAll(
                freqLabel,
                freqSpinner,
                new Separator(),
                delayLabel,
                delaySpinner,
                new Separator(),
                startMinimizedCheck,
                enableNotificationsCheck
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        return tab;
    }

    private HBox createHotkeyRow(String label, String currentValue) {
        HBox box = new HBox(10);
        box.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 3;");

        Label labelControl = new Label(label);
        labelControl.setPrefWidth(120);

        Label valueLabel = new Label(currentValue);
        valueLabel.setPrefWidth(150);
        valueLabel.setStyle("-fx-font-family: monospace;");

        Button recordButton = new Button("Record");
        recordButton.setPrefWidth(100);
        recordButton.setOnAction(e -> onRecordHotkey(label, valueLabel));

        box.getChildren().addAll(labelControl, valueLabel, recordButton);
        return box;
    }

    private void onRecordHotkey(String hotkeyName, Label valueLabel) {
        HotkeyRecorderDialog recorderDialog = new HotkeyRecorderDialog(keyboardListener);
        recorderDialog.showAndWait();

        if (recorderDialog.isAccepted()) {
            HotkeyBinding binding = recorderDialog.getRecordedHotkey();
            if (binding != null) {
                valueLabel.setText(binding.getHotkeyString());
                logger.info("Hotkey recorded for {}: {}", hotkeyName, binding.getHotkeyString());
            }
        }
    }

    private void onOK() {
        logger.info("Settings saved");
        close();
    }
}


