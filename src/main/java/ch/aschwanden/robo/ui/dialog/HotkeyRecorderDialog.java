package ch.aschwanden.robo.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ch.aschwanden.robo.core.input.HotkeyBinding;
import ch.aschwanden.robo.core.input.KeyboardListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog zum Aufzeichnen einer Tastenkombination.
 * Verwendet JavaFX-eigene Key-Events – unabhängig vom MVP-Keyboard-Stub.
 */
public class HotkeyRecorderDialog extends Stage {
    private static final Logger logger = LoggerFactory.getLogger(HotkeyRecorderDialog.class);

    private final KeyboardListener keyboardListener;
    private HotkeyBinding recordedHotkey;
    private boolean accepted = false;

    // UI-Referenzen
    private Label statusLabel;
    private HBox buttonBox;

    public HotkeyRecorderDialog(KeyboardListener keyboardListener) {
        this.keyboardListener = keyboardListener;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Hotkey aufzeichnen");
        setWidth(420);
        setHeight(220);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_CENTER);

        Label instruction = new Label("Drücke eine Tastenkombination …");
        instruction.setStyle("-fx-font-size: 12;");

        statusLabel = new Label("Warte auf Taste");
        statusLabel.getStyleClass().add("hotkey-status-waiting");
        statusLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Abbrechen");
        cancelBtn.setPrefWidth(110);
        cancelBtn.setOnAction(e -> onCancel());
        buttonBox.getChildren().add(cancelBtn);

        root.getChildren().addAll(instruction, statusLabel, new Separator(), buttonBox);

        Scene scene = new Scene(root);
        setScene(scene);

        // Tastaturaufnahme über JavaFX direkt
        scene.setOnKeyPressed(event -> {
            // Escape = Abbrechen
            if (event.getCode() == KeyCode.ESCAPE) {
                onCancel();
                return;
            }
            // Reine Modifier-Tasten (Shift, Ctrl, Alt, Cmd) ignorieren
            if (event.getCode().isModifierKey()) return;

            int keyCode   = event.getCode().getCode();
            int modifiers = buildModifiers(event);

            recordedHotkey = new HotkeyBinding(keyCode, modifiers, null);
            logger.info("Hotkey aufgezeichnet: {}", recordedHotkey.getHotkeyString());

            // Kein weiteres Event mehr aufnehmen
            scene.setOnKeyPressed(null);

            Platform.runLater(() -> showRecordedState());
        });

        setOnCloseRequest(e -> { if (!accepted) onCancel(); });
    }

    // ─── Zustände ─────────────────────────────────────────────────────────────

    private void showRecordedState() {
        statusLabel.setText("Aufgezeichnet:  " + recordedHotkey.getHotkeyString());
        statusLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #4caf50;");

        buttonBox.getChildren().clear();

        Button retryBtn = new Button("Wiederholen");
        retryBtn.setPrefWidth(110);
        retryBtn.setOnAction(e -> onRetry());

        Button acceptBtn = new Button("Übernehmen");
        acceptBtn.setPrefWidth(110);
        acceptBtn.setOnAction(e -> onAccept());
        acceptBtn.setDefaultButton(true);

        buttonBox.getChildren().addAll(retryBtn, acceptBtn);
    }

    private void onRetry() {
        recordedHotkey = null;
        accepted = false;
        statusLabel.setText("Warte auf Taste");
        statusLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        buttonBox.getChildren().clear();
        Button cancelBtn = new Button("Abbrechen");
        cancelBtn.setPrefWidth(110);
        cancelBtn.setOnAction(e -> onCancel());
        buttonBox.getChildren().add(cancelBtn);

        // Aufnahme neu starten
        getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) { onCancel(); return; }
            if (event.getCode().isModifierKey()) return;

            int keyCode   = event.getCode().getCode();
            int modifiers = buildModifiers(event);
            recordedHotkey = new HotkeyBinding(keyCode, modifiers, null);
            getScene().setOnKeyPressed(null);
            Platform.runLater(() -> showRecordedState());
        });
    }

    private void onAccept() {
        accepted = true;
        close();
    }

    private void onCancel() {
        keyboardListener.stopRecordingHotkey();
        recordedHotkey = null;
        accepted = false;
        close();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Baut einen Modifier-Bitmask aus dem JavaFX KeyEvent (AWT-kompatibel). */
    private static int buildModifiers(javafx.scene.input.KeyEvent event) {
        int mod = 0;
        if (event.isShiftDown())   mod |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
        if (event.isControlDown()) mod |= java.awt.event.InputEvent.CTRL_DOWN_MASK;
        if (event.isAltDown())     mod |= java.awt.event.InputEvent.ALT_DOWN_MASK;
        if (event.isMetaDown())    mod |= java.awt.event.InputEvent.META_DOWN_MASK;
        return mod;
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public HotkeyBinding getRecordedHotkey() { return recordedHotkey; }
    public boolean isAccepted()              { return accepted; }
}
