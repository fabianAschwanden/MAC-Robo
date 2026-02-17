package org.example.robo.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.robo.core.input.HotkeyBinding;
import org.example.robo.core.input.HotkeyRecordingCallback;
import org.example.robo.core.input.KeyboardListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog zum Aufzeichnen von Custom Hotkeys.
 * Benutzer drückt eine Tasten-Kombination und der Dialog erfasst diese.
 */
public class HotkeyRecorderDialog extends Stage {
    private static final Logger logger = LoggerFactory.getLogger(HotkeyRecorderDialog.class);
    private static final long RECORDING_TIMEOUT = 5000; // 5 Sekunden

    private final KeyboardListener keyboardListener;
    private HotkeyBinding recordedHotkey;
    private boolean accepted = false;

    public HotkeyRecorderDialog(KeyboardListener keyboardListener) {
        this.keyboardListener = keyboardListener;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Record Hotkey");
        setWidth(400);
        setHeight(200);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Instruktion
        Label instructionLabel = new Label("Press any key combination (within 5 seconds)");
        instructionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        // Display für aufgezeichnete Taste
        Label recordedLabel = new Label("Waiting for key...");
        recordedLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #0066cc;");

        // Buttons
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> onCancel());

        // Button-Container
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");
        buttonBox.getChildren().add(cancelButton);

        root.getChildren().addAll(
                instructionLabel,
                recordedLabel,
                new Separator(),
                buttonBox
        );

        Scene scene = new Scene(root);
        setScene(scene);

        // Starte Recording
        startRecording(recordedLabel);

        // Handle Close
        setOnCloseRequest(e -> {
            if (!accepted) {
                onCancel();
            }
        });
    }

    private void startRecording(Label recordedLabel) {
        keyboardListener.startRecordingHotkey(RECORDING_TIMEOUT, binding -> {
            javafx.application.Platform.runLater(() -> {
                if (binding != null) {
                    recordedHotkey = binding;
                    recordedLabel.setText("Recorded: " + binding.getHotkeyString());
                    recordedLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #00aa00;");

                    // Zeige Accept/Retry Buttons
                    showAcceptButtons();
                } else {
                    recordedLabel.setText("Timeout - No key detected");
                    recordedLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #cc0000;");
                    showRetryButton();
                }
            });
        });
    }

    private void showAcceptButtons() {
        // Ersetze Cancel-Button mit Accept/Retry
        javafx.scene.layout.HBox buttonBox = (javafx.scene.layout.HBox) getScene().getRoot()
                .getChildrenUnmodifiable().stream()
                .filter(n -> n instanceof javafx.scene.layout.HBox)
                .findFirst()
                .orElse(null);

        if (buttonBox != null) {
            buttonBox.getChildren().clear();

            Button acceptButton = new Button("Accept");
            acceptButton.setPrefWidth(100);
            acceptButton.setStyle("-fx-font-size: 12;");
            acceptButton.setOnAction(e -> onAccept());

            Button retryButton = new Button("Retry");
            retryButton.setPrefWidth(100);
            retryButton.setOnAction(e -> onRetry());

            buttonBox.getChildren().addAll(retryButton, acceptButton);
        }
    }

    private void showRetryButton() {
        javafx.scene.layout.HBox buttonBox = (javafx.scene.layout.HBox) getScene().getRoot()
                .getChildrenUnmodifiable().stream()
                .filter(n -> n instanceof javafx.scene.layout.HBox)
                .findFirst()
                .orElse(null);

        if (buttonBox != null) {
            buttonBox.getChildren().clear();

            Button retryButton = new Button("Retry");
            retryButton.setPrefWidth(100);
            retryButton.setOnAction(e -> onRetry());

            Button cancelButton = new Button("Cancel");
            cancelButton.setPrefWidth(100);
            cancelButton.setOnAction(e -> onCancel());

            buttonBox.getChildren().addAll(cancelButton, retryButton);
        }
    }

    private void onAccept() {
        accepted = true;
        logger.info("Hotkey recorded: {}", recordedHotkey);
        close();
    }

    private void onRetry() {
        recordedHotkey = null;
        accepted = false;
        Label recordedLabel = (Label) getScene().getRoot().getChildrenUnmodifiable().get(1);
        recordedLabel.setText("Waiting for key...");
        recordedLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #0066cc;");
        startRecording(recordedLabel);
    }

    private void onCancel() {
        keyboardListener.stopRecordingHotkey();
        recordedHotkey = null;
        accepted = false;
        close();
    }

    /**
     * Gibt die aufgezeichnete Hotkey-Bindung zurück, oder null wenn abgebrochen.
     */
    public HotkeyBinding getRecordedHotkey() {
        return recordedHotkey;
    }

    /**
     * Gibt an, ob der Dialog erfolgreich akzeptiert wurde.
     */
    public boolean isAccepted() {
        return accepted;
    }
}

