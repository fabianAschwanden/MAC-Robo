package ch.aschwanden.robo.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ch.aschwanden.robo.core.input.HotkeyBinding;
import ch.aschwanden.robo.core.input.KeyboardListener;
import ch.aschwanden.robo.core.profile.DelayType;
import ch.aschwanden.robo.core.profile.RobustSelector;
import ch.aschwanden.robo.core.profile.WebEventType;
import ch.aschwanden.robo.core.profile.WebRecordingStep;
import ch.aschwanden.robo.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/** Builds and manages the Web Capture panel (recording controls + steps table). */
class WebCapturePane {
    private static final Logger logger = LoggerFactory.getLogger(WebCapturePane.class);

    private final UIController controller;
    private final KeyboardListener keyboardListener;
    private final Consumer<Scene> cssApplier;

    private ObservableList<WebRecordingStep> webSteps;
    private TableView<WebRecordingStep> webStepsTable;
    private Button webRecordBtn;
    private Label webStatusLabel;
    private Label serverStatusLabel;
    private VBox view;

    WebCapturePane(UIController controller, KeyboardListener keyboardListener,
                   Consumer<Scene> cssApplier) {
        this.controller       = controller;
        this.keyboardListener = keyboardListener;
        this.cssApplier       = cssApplier;
    }

    VBox build() {
        webSteps = FXCollections.observableArrayList();
        view = new VBox();
        view.getStyleClass().add("content-area");
        view.getChildren().add(buildTitleBar());
        view.getChildren().add(buildConfigBar());

        webStepsTable = buildStepsTable();
        VBox.setVgrow(webStepsTable, Priority.ALWAYS);
        view.getChildren().add(webStepsTable);
        view.getChildren().add(buildBottomBar());
        return view;
    }

    // ─── Public API (called from MainWindowFX) ─────────────────────────────────

    void toggleRecording() {
        onToggleWebRecording();
    }

    /** Sync UI with recorder state; called from background status-updater thread. */
    void syncWithController() {
        if (!controller.isWebRecording()) return;
        List<WebRecordingStep> snapshot = new java.util.ArrayList<>(controller.getWebRecordedSteps());
        Platform.runLater(() -> {
            if (snapshot.size() != webSteps.size()) {
                webSteps.setAll(snapshot);
                webStepsTable.refresh();
                webStatusLabel.setText("● Aufzeichnung läuft…  (" + webSteps.size() + " Schritte)");
            }
        });
    }

    /** Called when a browser event arrives (via UIController → Platform.runLater). */
    void onWebStepReceived() {
        webSteps.clear();
        webSteps.addAll(controller.getWebRecordedSteps());
        webStepsTable.refresh();
        webStatusLabel.setText("● Aufzeichnung läuft…  (" + webSteps.size() + " Schritte)");
    }

    /** Force a table layout pass after becoming visible. */
    void requestTableLayout() {
        Platform.runLater(() -> {
            if (view != null) view.requestLayout();
            if (webStepsTable != null) webStepsTable.refresh();
        });
    }

    // ─── Title bar ─────────────────────────────────────────────────────────────

    private HBox buildTitleBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("title-bar");
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Web Capture");
        title.getStyleClass().add("title-label");

        webStatusLabel = new Label("Bereit");
        webStatusLabel.getStyleClass().add("web-status-label");

        serverStatusLabel = new Label("○ Server inaktiv");
        serverStatusLabel.getStyleClass().add("web-server-status-inactive");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button helpBtn = new Button("?");
        helpBtn.getStyleClass().add("new-click-btn");
        helpBtn.setTooltip(new Tooltip("Anleitung: Web Capture einrichten"));
        helpBtn.setOnAction(e -> showHelp());

        webRecordBtn = new Button("● Aufzeichnung starten");
        webRecordBtn.getStyleClass().add("web-record-btn");
        webRecordBtn.setOnAction(e -> onToggleWebRecording());

        bar.getChildren().addAll(
                title, new Label("  "), webStatusLabel, new Label("   "), serverStatusLabel,
                spacer, helpBtn, new Label(" "), webRecordBtn);
        return bar;
    }

    // ─── Config bar ────────────────────────────────────────────────────────────

    private HBox buildConfigBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("web-config-bar");
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label idleLabel = new Label("Idle-Threshold:");
        idleLabel.getStyleClass().add("web-config-label");

        Spinner<Integer> idleSpinner = new Spinner<>(100, 30000, (int) Constants.IDLE_THRESHOLD_MS, 100);
        idleSpinner.setEditable(true);
        idleSpinner.setPrefWidth(90);
        idleSpinner.getStyleClass().add("repeat-spinner");
        idleSpinner.valueProperty().addListener((obs, o, n) -> controller.setWebIdleThreshold(n));

        Label msLabel = new Label("ms");
        msLabel.getStyleClass().add("web-config-label");

        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setPadding(new Insets(0, 4, 0, 4));

        Label hotkeyLabel = new Label("Aufzeichnungs-Hotkey:");
        hotkeyLabel.getStyleClass().add("web-config-label");

        Label hotkeyValueLabel = new Label("–  nicht konfiguriert");
        hotkeyValueLabel.getStyleClass().add("web-hotkey-value");

        Button configHotkeyBtn = new Button("Konfigurieren");
        configHotkeyBtn.getStyleClass().add("new-click-btn");
        configHotkeyBtn.setOnAction(e -> openHotkeyDialog(hotkeyValueLabel));

        Button clearHotkeyBtn = new Button("✕");
        clearHotkeyBtn.getStyleClass().add("new-click-btn");
        clearHotkeyBtn.setTooltip(new Tooltip("Hotkey entfernen"));
        clearHotkeyBtn.setOnAction(e -> {
            controller.clearWebCaptureHotkey();
            hotkeyValueLabel.setText("–  nicht konfiguriert");
            hotkeyValueLabel.setStyle("");
        });

        bar.getChildren().addAll(
                idleLabel, idleSpinner, msLabel,
                sep,
                hotkeyLabel, hotkeyValueLabel, configHotkeyBtn, clearHotkeyBtn);
        return bar;
    }

    private void openHotkeyDialog(Label hotkeyValueLabel) {
        ch.aschwanden.robo.ui.dialog.HotkeyRecorderDialog dlg =
                new ch.aschwanden.robo.ui.dialog.HotkeyRecorderDialog(keyboardListener);
        cssApplier.accept(dlg.getScene());
        dlg.showAndWait();

        if (dlg.isAccepted() && dlg.getRecordedHotkey() != null) {
            HotkeyBinding binding = dlg.getRecordedHotkey();
            controller.setWebCaptureHotkey(binding);
            hotkeyValueLabel.setText(binding.getHotkeyString());
            hotkeyValueLabel.setStyle("-fx-text-fill: #60c060; -fx-font-weight: bold;");
        }
    }

    // ─── Steps table ───────────────────────────────────────────────────────────

    private TableView<WebRecordingStep> buildStepsTable() {
        TableView<WebRecordingStep> table = new TableView<>(webSteps);
        table.getStyleClass().add("click-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Noch keine Schritte aufgezeichnet. Starte die Aufzeichnung."));

        TableColumn<WebRecordingStep, String> numCol = new TableColumn<>("#");
        numCol.setPrefWidth(40); numCol.setMaxWidth(40);
        numCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(""));
        numCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        TableColumn<WebRecordingStep, String> typeCol = new TableColumn<>("Typ");
        typeCol.setPrefWidth(90);
        typeCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getWebEventType() != null ? cd.getValue().getWebEventType().name() : ""));

        TableColumn<WebRecordingStep, String> selectorCol = new TableColumn<>("Selektor");
        selectorCol.setPrefWidth(240);
        selectorCol.setCellValueFactory(cd -> {
            RobustSelector sel = cd.getValue().getSelector();
            String primary    = sel != null ? sel.getPrimary() : "–";
            int strategies    = sel != null ? sel.strategyCount() : 0;
            return new javafx.beans.property.SimpleStringProperty(
                    primary + (strategies > 1 ? "  [+" + (strategies - 1) + "]" : ""));
        });

        TableColumn<WebRecordingStep, String> payloadCol = new TableColumn<>("Payload");
        payloadCol.setPrefWidth(130);
        payloadCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getPayload() != null ? cd.getValue().getPayload() : "–"));

        TableColumn<WebRecordingStep, String> timingCol = new TableColumn<>("Timing");
        timingCol.setPrefWidth(90);
        timingCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getTimingMs() + " ms"));

        TableColumn<WebRecordingStep, String> delayCol = new TableColumn<>("Delay");
        delayCol.setPrefWidth(80);
        delayCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getDelayType() != null ? cd.getValue().getDelayType().name() : ""));
        delayCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(DelayType.SMART.name().equals(item)
                        ? "-fx-text-fill: #e0a020;"
                        : "-fx-text-fill: #60c060;");
            }
        });

        table.getColumns().addAll(List.of(numCol, typeCol, selectorCol, payloadCol, timingCol, delayCol));
        return table;
    }

    // ─── Bottom bar ────────────────────────────────────────────────────────────

    private HBox buildBottomBar() {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("bottom-bar");
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        Button clearBtn = new Button("Löschen");
        clearBtn.getStyleClass().add("new-click-btn");
        clearBtn.setOnAction(e -> {
            webSteps.clear();
            controller.clearWebRecording();
        });

        Button demoBtn = new Button("Demo-Schritt");
        demoBtn.getStyleClass().add("new-click-btn");
        demoBtn.setTooltip(new Tooltip("Simuliert einen aufgezeichneten Browser-Schritt"));
        demoBtn.setOnAction(e -> onInjectDemoStep());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hintLabel = new Label("Schritte kommen automatisch vom Browser-Extension-Kontext.");
        hintLabel.getStyleClass().add("web-config-hint");

        bar.getChildren().addAll(clearBtn, demoBtn, spacer, hintLabel);
        return bar;
    }

    // ─── Recording toggle ──────────────────────────────────────────────────────

    private void onToggleWebRecording() {
        if (controller.isWebRecording()) {
            controller.stopWebRecording();
            logger.info("[WebCapture] Aufzeichnung gestoppt. Recorder-Steps: {}, UI-Steps: {}",
                    controller.getWebRecordedSteps().size(), webSteps.size());
            webSteps.clear();
            webSteps.addAll(controller.getWebRecordedSteps());
            webStepsTable.refresh();
            webRecordBtn.setText("● Aufzeichnung starten");
            webRecordBtn.getStyleClass().removeAll("web-record-btn-active");
            webRecordBtn.getStyleClass().add("web-record-btn");
            webStatusLabel.setText("Gestoppt – " + webSteps.size() + " Schritte");
            webStatusLabel.setStyle("-fx-text-fill: #888888;");
            serverStatusLabel.setText("○ Server inaktiv");
            serverStatusLabel.getStyleClass().removeAll("web-server-status-active");
            serverStatusLabel.getStyleClass().add("web-server-status-inactive");
        } else {
            controller.startWebRecording();
            logger.info("[WebCapture] Aufzeichnung gestartet. isWebRecording={}", controller.isWebRecording());
            webSteps.clear();
            webStepsTable.refresh();
            webRecordBtn.setText("■ Aufzeichnung stoppen");
            webRecordBtn.getStyleClass().removeAll("web-record-btn");
            webRecordBtn.getStyleClass().add("web-record-btn-active");
            webStatusLabel.setText("● Aufzeichnung läuft…");
            webStatusLabel.setStyle("-fx-text-fill: #e05555;");
            serverStatusLabel.setText("● Server aktiv – localhost:" + controller.getCaptureServerPort());
            serverStatusLabel.getStyleClass().removeAll("web-server-status-inactive");
            serverStatusLabel.getStyleClass().add("web-server-status-active");
        }
    }

    private void onInjectDemoStep() {
        WebEventType[] types = { WebEventType.CLICK, WebEventType.TYPE, WebEventType.HOVER };
        String[][] selectors = {
            { "button[data-id='submit']", "//button[@data-id='submit']", "Anmelden" },
            { "input#password",           "//input[@id='password']",      null },
            { "nav a.active",             "//nav/a[@class='active']",     "Home" }
        };
        int idx = webSteps.size() % 3;
        String payload = idx == 1 ? "MeinPasswort123" : null;
        RobustSelector selector = RobustSelector.of(selectors[idx][0], selectors[idx][1], selectors[idx][2]);

        WebRecordingStep step = WebRecordingStep.builder()
                .timestamp(System.currentTimeMillis())
                .webEventType(types[idx])
                .selector(selector)
                .payload(payload)
                .timing(webSteps.isEmpty() ? 0 : 500)
                .delayType(DelayType.HARD)
                .build();
        webSteps.add(step);
        webStepsTable.refresh();

        if (controller.isWebRecording()) {
            controller.recordWebStep(types[idx], selector, payload);
        }

        logger.info("[WebCapture] Demo-Schritt #{} hinzugefügt ({})", webSteps.size(), types[idx]);
        boolean rec = controller.isWebRecording();
        webStatusLabel.setText((rec ? "● Aufzeichnung läuft…  " : "") + "(" + webSteps.size() + " Schritte)");
    }

    // ─── Help dialog ───────────────────────────────────────────────────────────

    private void showHelp() {
        Stage dialog = new Stage();
        dialog.setTitle("Web Capture – Anleitung");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24, 28, 20, 28));
        root.setPrefWidth(540);

        Label heading = new Label("Web Capture einrichten");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e05555;");
        root.getChildren().add(heading);

        root.getChildren().add(helpSection("Schritt 1 – Chrome Extension laden",
                "1. Chrome öffnen und chrome://extensions aufrufen\n" +
                "2. Oben rechts Entwicklermodus aktivieren\n" +
                "3. \"Entpackte Erweiterung laden\" klicken\n" +
                "4. Den Ordner  browser-extension/  im Projektverzeichnis auswählen\n" +
                "5. Die Extension \"MACRobo Web Capture\" erscheint in der Liste"));

        root.getChildren().add(helpSection("Schritt 2 – Aufzeichnung starten",
                "1. In der App auf \"● Aufzeichnung starten\" klicken\n" +
                "   → Der HTTP-Server startet auf  localhost:7890\n" +
                "   → Status wechselt zu: ● Server aktiv\n" +
                "2. Im Chrome-Browser zur Ziel-Webseite navigieren\n" +
                "3. Das Extension-Icon zeigt einen grünen Punkt, wenn die Verbindung steht"));

        root.getChildren().add(helpSection("Schritt 3 – Aktionen aufzeichnen",
                "Folgende Aktionen auf der Webseite werden automatisch erfasst:\n\n" +
                "  CLICK    – Klick auf Link, Button, Input\n" +
                "  TYPE     – Text in Eingabefeld (Wert wird mitgespeichert)\n\n" +
                "Jede Aktion erscheint sofort als neue Zeile in der Tabelle."));

        root.getChildren().add(helpSection("Schritt 4 – Aufzeichnung stoppen",
                "\"■ Aufzeichnung stoppen\" klicken.\n" +
                "Alle aufgezeichneten Schritte bleiben in der Tabelle sichtbar.\n" +
                "Der HTTP-Server wird automatisch beendet."));

        root.getChildren().add(helpSection("Fehlerbehebung",
                "Extension zeigt roten Punkt (nicht verbunden):\n" +
                "  → Sicherstellen, dass die Aufzeichnung in der App gestartet ist\n" +
                "  → Popup schließen und wieder öffnen\n\n" +
                "Keine Schritte erscheinen in der Tabelle:\n" +
                "  → Prüfen ob Status ● Server aktiv zeigt\n" +
                "  → Extension auf chrome://-Tabs ist nicht aktiv (normale Seite verwenden)\n\n" +
                "Hotkeys funktionieren nicht:\n" +
                "  → Accessibility-Berechtigung prüfen:\n" +
                "     Systemeinstellungen → Datenschutz & Sicherheit → Bedienungshilfen"));

        Button closeBtn = new Button("Schließen");
        closeBtn.getStyleClass().add("new-click-btn");
        closeBtn.setOnAction(e -> dialog.close());
        HBox btnBar = new HBox(closeBtn);
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().add(btnBar);

        Scene scene = new Scene(new ScrollPane(root));
        cssApplier.accept(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static VBox helpSection(String title, String body) {
        VBox section = new VBox(4);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #cccccc; -fx-font-size: 12px;");
        Label bodyLbl = new Label(body);
        bodyLbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
        bodyLbl.setWrapText(true);
        section.getChildren().addAll(titleLbl, bodyLbl);
        return section;
    }
}
