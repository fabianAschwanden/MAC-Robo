package org.example.robo.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import org.example.robo.config.ConfigurationManager;
import org.example.robo.core.input.KeyboardListener;
import org.example.robo.core.profile.ClickProfile;
import org.example.robo.core.profile.ClickType;
import org.example.robo.core.profile.DelayType;
import org.example.robo.core.profile.RobustSelector;
import org.example.robo.core.profile.SpeedMode;
import org.example.robo.core.profile.WebEventType;
import org.example.robo.core.profile.WebRecordingStep;
import org.example.robo.util.Constants;
import org.example.robo.util.MousePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hauptfenster der MACRobo Anwendung.
 * Layout: schmale Icon-Sidebar links + Tabellenansicht rechts.
 */
public class MainWindowFX {
    private static final Logger logger = LoggerFactory.getLogger(MainWindowFX.class);

    // Icon-Farben (passend zum Screenshot)
    private static final Color ICON_DEFAULT  = Color.web("#888888");
    private static final Color ICON_ACTIVE   = Color.web("#e05555");
    private static final Color MOUSE_BODY    = Color.web("#aaaaaa");
    private static final Color MOUSE_FILL    = Color.web("#3a3a3a");

    private UIController controller;
    private ConfigurationManager configurationManager;
    private KeyboardListener keyboardListener;
    private ScheduledExecutorService statusUpdateExecutor;

    private ObservableList<ClickProfile> profiles;
    private TableView<ClickProfile> tableView;
    private Button startStopButton;

    // Web Capture
    private ObservableList<WebRecordingStep> webSteps;
    private TableView<WebRecordingStep> webStepsTable;
    private Button webRecordBtn;
    private Label webStatusLabel;
    private Label serverStatusLabel;
    private StackPane contentStack;
    private Node autoClickPane;
    private Node webCapturePane;
    private Button sidebarAutoClickBtn;
    private Button sidebarWebCaptureBtn;

    // ─── Public API ────────────────────────────────────────────────────────────

    public void show(Stage stage, UIController controller, List<ClickProfile> initialProfiles,
                     ConfigurationManager configurationManager, KeyboardListener keyboardListener) {
        this.controller = controller;
        this.configurationManager = configurationManager;
        this.keyboardListener = keyboardListener;
        this.profiles = FXCollections.observableArrayList(initialProfiles);

        stage.setTitle(Constants.APP_NAME);
        stage.setWidth(Constants.MAIN_WINDOW_WIDTH);
        stage.setHeight(Constants.MAIN_WINDOW_HEIGHT);
        stage.setMinWidth(Constants.MAIN_WINDOW_MIN_WIDTH);
        stage.setMinHeight(Constants.MAIN_WINDOW_MIN_HEIGHT);
        stage.setResizable(true);

        BorderPane root = buildLayout();
        Scene scene = new Scene(root);
        applyCss(scene);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();

        startStatusUpdater();
        logger.info("MainWindowFX (MACRobo) initialized");
    }

    // ─── Layout ────────────────────────────────────────────────────────────────

    private BorderPane buildLayout() {
        webSteps = FXCollections.observableArrayList();

        autoClickPane = buildContent();
        webCapturePane = buildWebCaptureContent();

        contentStack = new StackPane(autoClickPane, webCapturePane);
        webCapturePane.setVisible(false);

        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(contentStack);
        return root;
    }

    private void switchToView(boolean showWebCapture) {
        autoClickPane.setVisible(!showWebCapture);
        webCapturePane.setVisible(showWebCapture);

        sidebarAutoClickBtn.getStyleClass().removeAll("sidebar-btn-active");
        sidebarWebCaptureBtn.getStyleClass().removeAll("sidebar-btn-active");

        if (showWebCapture) {
            sidebarWebCaptureBtn.getStyleClass().add("sidebar-btn-active");
            // Layout-Pass erzwingen: TableView rendert nach initialem setVisible(false) ggf. nicht
            Platform.runLater(() -> {
                if (webCapturePane instanceof javafx.scene.Parent p) p.requestLayout();
                if (webStepsTable != null) webStepsTable.refresh();
            });
        } else {
            sidebarAutoClickBtn.getStyleClass().add("sidebar-btn-active");
        }
    }

    // ─── Sidebar ───────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(Constants.SIDEBAR_WIDTH);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(12, 0, 12, 0));
        sidebar.setSpacing(2);

        sidebarAutoClickBtn = sidebarBtn(iconMouseFront(28, true), "MACRobo", true);
        sidebarAutoClickBtn.setOnAction(e -> switchToView(false));

        sidebarWebCaptureBtn = sidebarBtn(iconBrowser(24, ICON_DEFAULT), "Web Capture", false);
        sidebarWebCaptureBtn.setOnAction(e -> switchToView(true));

        sidebar.getChildren().addAll(
                sidebarAutoClickBtn,
                sidebarWebCaptureBtn
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button settingsBtn = sidebarBtn(iconGear(24, ICON_DEFAULT), "Settings", false);
        settingsBtn.setOnAction(e -> openSettings());
        sidebar.getChildren().add(settingsBtn);

        return sidebar;
    }

    private Button sidebarBtn(Node icon, String tooltip, boolean active) {
        Button btn = new Button();
        btn.setGraphic(icon);
        btn.getStyleClass().add("sidebar-btn");
        if (active) btn.getStyleClass().add("sidebar-btn-active");
        btn.setPrefSize(Constants.SIDEBAR_WIDTH, Constants.SIDEBAR_WIDTH);
        btn.setTooltip(new Tooltip(tooltip));
        return btn;
    }

    // ─── Icons (Canvas-drawn) ─────────────────────────────────────────────────

    /**
     * Vorderansicht-Maus.
     *  active = true  → linker Button ist rot ausgefüllt (aktive Sektion)
     *  active = false → beide Buttons helles Grau (passiv)
     */
    private static Canvas iconMouseFront(double sz, boolean active) {
        Canvas c = new Canvas(sz, sz);
        GraphicsContext gc = c.getGraphicsContext2D();

        double bodyW   = sz * 0.60;
        double bodyH   = sz * 0.78;
        double bx      = (sz - bodyW) / 2;
        double by      = (sz - bodyH) / 2;
        double arcR    = bodyW * 0.48;
        double splitY  = by + bodyH * 0.40;   // Trennlinie Buttons / Körper
        double midX    = bx + bodyW / 2;

        // Hintergrund-Body
        gc.setFill(MOUSE_FILL);
        gc.fillRoundRect(bx, by, bodyW, bodyH, arcR, arcR);

        // Linker Button-Bereich
        Color leftColor = active ? ICON_ACTIVE : Color.web("#555555");
        gc.setFill(leftColor);
        gc.save();
        // Clipping auf linke obere Hälfte des Körpers
        gc.beginPath();
        gc.moveTo(midX, by);
        gc.lineTo(bx + arcR / 2, by);
        gc.quadraticCurveTo(bx, by, bx, by + arcR / 2);
        gc.lineTo(bx, splitY);
        gc.lineTo(midX, splitY);
        gc.closePath();
        gc.fill();
        gc.restore();

        // Rechter Button-Bereich
        gc.setFill(Color.web("#4a4a4a"));
        gc.save();
        gc.beginPath();
        gc.moveTo(midX, by);
        gc.lineTo(bx + bodyW - arcR / 2, by);
        gc.quadraticCurveTo(bx + bodyW, by, bx + bodyW, by + arcR / 2);
        gc.lineTo(bx + bodyW, splitY);
        gc.lineTo(midX, splitY);
        gc.closePath();
        gc.fill();
        gc.restore();

        // Outline
        Color strokeColor = active ? ICON_ACTIVE : MOUSE_BODY;
        gc.setStroke(strokeColor);
        gc.setLineWidth(1.4);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.strokeRoundRect(bx, by, bodyW, bodyH, arcR, arcR);

        // Vertikale Mittellinie (Button-Trenner, nur oben)
        gc.strokeLine(midX, by, midX, splitY);

        // Horizontale Trennlinie Buttons/Körper
        gc.strokeLine(bx, splitY, bx + bodyW, splitY);

        // Scroll-Wheel (kleiner Kreis in der Mitte, zwischen Buttons)
        double wR = bodyW * 0.11;
        double wCy = by + (splitY - by) / 2;
        gc.setFill(strokeColor);
        gc.fillOval(midX - wR, wCy - wR, wR * 2, wR * 2);

        return c;
    }

    /** Zahnrad-Icon (8 Zähne) */
    private static Canvas iconGear(double sz, Color color) {
        Canvas c = new Canvas(sz, sz);
        GraphicsContext gc = c.getGraphicsContext2D();
        gc.setFill(color);

        double cx   = sz / 2;
        double cy   = sz / 2;
        double outerR = sz * 0.40;
        double innerR = sz * 0.24;
        double holeR  = sz * 0.13;
        int teeth     = 8;

        double[] xs = new double[teeth * 4];
        double[] ys = new double[teeth * 4];
        double toothW = Math.PI / teeth * 0.55;

        for (int i = 0; i < teeth; i++) {
            double baseAngle = 2 * Math.PI * i / teeth;
            double a0 = baseAngle - toothW;
            double a1 = baseAngle;
            double a2 = baseAngle + toothW;
            double a3 = baseAngle + 2 * Math.PI / teeth - toothW;

            xs[i * 4]     = cx + innerR * Math.cos(a0);
            ys[i * 4]     = cy + innerR * Math.sin(a0);
            xs[i * 4 + 1] = cx + outerR * Math.cos(a1);
            ys[i * 4 + 1] = cy + outerR * Math.sin(a1);
            xs[i * 4 + 2] = cx + outerR * Math.cos(a2);
            ys[i * 4 + 2] = cy + outerR * Math.sin(a2);
            xs[i * 4 + 3] = cx + innerR * Math.cos(a3);
            ys[i * 4 + 3] = cy + innerR * Math.sin(a3);
        }
        gc.fillPolygon(xs, ys, teeth * 4);

        // Loch
        gc.setFill(Color.web("#2a2a2a"));
        gc.fillOval(cx - holeR, cy - holeR, holeR * 2, holeR * 2);

        return c;
    }

    /** Einfaches Browser-Fenster-Icon (Titelleiste + zwei Tabs) */
    private static Canvas iconBrowser(double sz, Color color) {
        Canvas c = new Canvas(sz, sz);
        GraphicsContext gc = c.getGraphicsContext2D();
        gc.setStroke(color);
        gc.setLineWidth(1.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double pad  = sz * 0.10;
        double w    = sz - 2 * pad;
        double h    = sz - 2 * pad;
        double arc  = 3.0;
        double barH = h * 0.28;

        // Äußerer Rahmen
        gc.strokeRoundRect(pad, pad, w, h, arc, arc);
        // Trennlinie Titelleiste
        gc.strokeLine(pad, pad + barH, pad + w, pad + barH);
        // Zwei kleine Tab-Rechtecke in der Titelleiste
        double tabW = w * 0.28;
        double tabH = barH * 0.60;
        double tabY = pad + barH - tabH;
        gc.setFill(color);
        gc.fillRoundRect(pad + w * 0.06, tabY, tabW, tabH, 2, 2);
        gc.strokeRoundRect(pad + w * 0.38, tabY, tabW, tabH, 2, 2);
        // Adressleiste im Body
        double addrY = pad + barH + h * 0.12;
        gc.strokeRoundRect(pad + w * 0.08, addrY, w * 0.84, h * 0.15, 2, 2);
        return c;
    }

    // ─── Web Capture Panel ─────────────────────────────────────────────────────

    private VBox buildWebCaptureContent() {
        VBox content = new VBox();
        content.getStyleClass().add("content-area");
        content.getChildren().add(buildWebCaptureTitleBar());

        Node configBar = buildWebCaptureConfigBar();
        content.getChildren().add(configBar);

        webStepsTable = buildWebStepsTable();
        VBox.setVgrow(webStepsTable, Priority.ALWAYS);
        content.getChildren().add(webStepsTable);

        content.getChildren().add(buildWebCaptureBottomBar());
        return content;
    }

    private HBox buildWebCaptureTitleBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("title-bar");
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Web Capture");
        title.getStyleClass().add("title-label");

        webStatusLabel = new Label("Bereit");
        webStatusLabel.getStyleClass().add("web-status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        webRecordBtn = new Button("● Aufzeichnung starten");
        webRecordBtn.getStyleClass().add("web-record-btn");
        webRecordBtn.setOnAction(e -> onToggleWebRecording());

        serverStatusLabel = new Label("○ Server inaktiv");
        serverStatusLabel.getStyleClass().add("web-server-status-inactive");

        bar.getChildren().addAll(title, new Label("  "), webStatusLabel, new Label("   "), serverStatusLabel, spacer, webRecordBtn);
        return bar;
    }

    private HBox buildWebCaptureConfigBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("web-config-bar");
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        // ── Idle-Threshold ────────────────────────────────────────────────────
        Label idleLabel = new Label("Idle-Threshold:");
        idleLabel.getStyleClass().add("web-config-label");

        Spinner<Integer> idleSpinner = new Spinner<>(100, 30000, (int) Constants.IDLE_THRESHOLD_MS, 100);
        idleSpinner.setEditable(true);
        idleSpinner.setPrefWidth(90);
        idleSpinner.getStyleClass().add("repeat-spinner");
        idleSpinner.valueProperty().addListener((obs, o, n) -> controller.setWebIdleThreshold(n));

        Label msLabel = new Label("ms");
        msLabel.getStyleClass().add("web-config-label");

        // ── Trenner ───────────────────────────────────────────────────────────
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setPadding(new Insets(0, 4, 0, 4));

        // ── Hotkey-Konfiguration ──────────────────────────────────────────────
        Label hotkeyLabel = new Label("Aufzeichnungs-Hotkey:");
        hotkeyLabel.getStyleClass().add("web-config-label");

        Label hotkeyValueLabel = new Label("–  nicht konfiguriert");
        hotkeyValueLabel.getStyleClass().add("web-hotkey-value");

        Button configHotkeyBtn = new Button("Konfigurieren");
        configHotkeyBtn.getStyleClass().add("new-click-btn");
        configHotkeyBtn.setOnAction(e -> openWebCaptureHotkeyDialog(hotkeyValueLabel));

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

    private void openWebCaptureHotkeyDialog(Label hotkeyValueLabel) {
        org.example.robo.ui.dialog.HotkeyRecorderDialog dlg =
                new org.example.robo.ui.dialog.HotkeyRecorderDialog(keyboardListener);
        applyCss(dlg.getScene());
        dlg.showAndWait();

        if (dlg.isAccepted() && dlg.getRecordedHotkey() != null) {
            org.example.robo.core.input.HotkeyBinding binding = dlg.getRecordedHotkey();
            controller.setWebCaptureHotkey(binding);
            hotkeyValueLabel.setText(binding.getHotkeyString());
            hotkeyValueLabel.setStyle("-fx-text-fill: #60c060; -fx-font-weight: bold;");
        }
    }

    private TableView<WebRecordingStep> buildWebStepsTable() {
        TableView<WebRecordingStep> table = new TableView<>(webSteps);
        table.getStyleClass().add("click-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Noch keine Schritte aufgezeichnet. Starte die Aufzeichnung."));

        TableColumn<WebRecordingStep, String> numCol = new TableColumn<>("#");
        numCol.setPrefWidth(40);
        numCol.setMaxWidth(40);
        // CellValueFactory muss gesetzt sein – sonst liefert JavaFX item=null + empty=true für alle Zeilen
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
            String primary = sel != null ? sel.getPrimary() : "–";
            int strategies = sel != null ? sel.strategyCount() : 0;
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

    private HBox buildWebCaptureBottomBar() {
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
        demoBtn.setOnAction(e -> onInjectDemoStep());
        demoBtn.setTooltip(new Tooltip("Simuliert einen aufgezeichneten Browser-Schritt"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hintLabel = new Label("Schritte kommen automatisch vom Browser-Extension-Kontext.");
        hintLabel.getStyleClass().add("web-config-hint");

        bar.getChildren().addAll(clearBtn, demoBtn, spacer, hintLabel);
        return bar;
    }

    private void onToggleWebRecording() {
        if (controller.isWebRecording()) {
            controller.stopWebRecording();
            logger.info("[WebCapture] Aufzeichnung gestoppt. Recorder-Steps: {}, UI-Steps: {}",
                    controller.getWebRecordedSteps().size(), webSteps.size());
            // Tabelle mit dem finalen Stand des Recorders synchronisieren
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

    /**
     * Fügt einen Demo-Schritt hinzu.
     * Funktioniert auch ohne aktive Aufzeichnung, um die Tabellendarstellung zu testen.
     */
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

        // Step direkt in die UI-Liste schreiben (kein Umweg über Recorder nötig)
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

        // Auch in den Recorder schreiben wenn Aufzeichnung aktiv
        if (controller.isWebRecording()) {
            controller.recordWebStep(types[idx], selector, payload);
        }

        logger.info("[WebCapture] Demo-Schritt #{} hinzugefügt ({})", webSteps.size(), types[idx]);
        boolean rec = controller.isWebRecording();
        webStatusLabel.setText((rec ? "● Aufzeichnung läuft…  " : "") + "(" + webSteps.size() + " Schritte)");
    }

    // ─── Content ───────────────────────────────────────────────────────────────

    private VBox buildContent() {
        VBox content = new VBox();
        content.getStyleClass().add("content-area");

        content.getChildren().add(buildTitleBar());

        TableView<ClickProfile> table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);

        content.getChildren().add(buildBottomBar());
        return content;
    }

    private HBox buildTitleBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("title-bar");
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(Constants.APP_NAME);
        title.getStyleClass().add("title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newClick = new Button("+ New Click");
        newClick.getStyleClass().add("new-click-btn");
        newClick.setOnAction(e -> onNewClick());

        bar.getChildren().addAll(title, spacer, newClick);
        return bar;
    }

    // ─── Table ─────────────────────────────────────────────────────────────────

    private TableView<ClickProfile> buildTable() {
        tableView = new TableView<>(profiles);
        tableView.getStyleClass().add("click-table");
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setPlaceholder(new Label("Keine Einträge. Klicke \"+ New Click\"."));

        // ── Checkbox ─────────────────────────────────────────────────────────
        TableColumn<ClickProfile, Boolean> enabledCol = new TableColumn<>("");
        enabledCol.setPrefWidth(46);
        enabledCol.setMaxWidth(46);
        enabledCol.setMinWidth(46);
        enabledCol.setCellValueFactory(cd -> {
            ClickProfile p = cd.getValue();
            SimpleBooleanProperty prop = new SimpleBooleanProperty(p.isEnabled());
            prop.addListener((obs, o, n) -> controller.setEntryEnabled(p, n));
            return prop;
        });
        enabledCol.setCellFactory(CheckBoxTableCell.forTableColumn(enabledCol));
        enabledCol.setEditable(true);

        // ── Name ──────────────────────────────────────────────────────────────
        TableColumn<ClickProfile, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));

        // ── Position ──────────────────────────────────────────────────────────
        TableColumn<ClickProfile, String> posCol = new TableColumn<>("Position");
        posCol.setPrefWidth(220);
        posCol.setCellValueFactory(cd -> {
            MousePosition pos = cd.getValue().getPosition();
            return new javafx.beans.property.SimpleStringProperty(
                    "(" + pos.getX() + ", " + pos.getY() + ")");
        });
        posCol.setCellFactory(col -> new TableCell<>() {
            private final Label  lbl      = new Label();
            private final Button btn      = new Button("Set pos");
            private final Button enterBtn = new Button("Enter");
            private final HBox   box      = new HBox(6, lbl, btn, enterBtn);
            private ScheduledExecutorService activeCountdown = null;
            {
                box.setAlignment(Pos.CENTER_LEFT);
                lbl.getStyleClass().add("pos-label");
                btn.getStyleClass().add("set-pos-btn");
                enterBtn.getStyleClass().add("set-pos-btn");

                // ── Maus-Capture mit 3s-Countdown ──────────────────────────
                btn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    ClickProfile p = getTableView().getItems().get(idx);
                    btn.setDisable(true);
                    enterBtn.setDisable(true);
                    int[] remaining = {3};
                    btn.setText(remaining[0] + "s...");
                    cancelActiveCountdown();
                    activeCountdown = Executors.newSingleThreadScheduledExecutor(r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });
                    activeCountdown.scheduleAtFixedRate(() -> {
                        remaining[0]--;
                        if (remaining[0] > 0) {
                            Platform.runLater(() -> btn.setText(remaining[0] + "s..."));
                        } else {
                            activeCountdown.shutdown();
                            activeCountdown = null;
                            controller.captureAndSetPosition(p);
                            Platform.runLater(() -> {
                                btn.setText("Set pos");
                                btn.setDisable(false);
                                enterBtn.setDisable(false);
                                int listIdx = tableView.getItems().indexOf(p);
                                if (listIdx >= 0) tableView.getItems().set(listIdx, p);
                            });
                        }
                    }, 1000, 1000, TimeUnit.MILLISECONDS);
                });

                // ── Manuelle Eingabe ────────────────────────────────────────
                enterBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    ClickProfile p = getTableView().getItems().get(idx);
                    MousePosition cur = p.getPosition();

                    Dialog<MousePosition> dlg = new Dialog<>();
                    dlg.setTitle("Position eingeben");
                    dlg.setHeaderText(null);

                    TextField xField = new TextField(String.valueOf(cur.getX()));
                    TextField yField = new TextField(String.valueOf(cur.getY()));
                    xField.setPrefWidth(80);
                    yField.setPrefWidth(80);

                    GridPane grid = new GridPane();
                    grid.setHgap(8);
                    grid.setVgap(8);
                    grid.setPadding(new Insets(12));
                    grid.add(new Label("X:"), 0, 0);
                    grid.add(xField, 1, 0);
                    grid.add(new Label("Y:"), 0, 1);
                    grid.add(yField, 1, 1);
                    dlg.getDialogPane().setContent(grid);

                    ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

                    // OK nur aktivieren wenn beide Felder gültige Zahlen enthalten
                    Node okNode = dlg.getDialogPane().lookupButton(okType);
                    Runnable validate = () -> {
                        try {
                            Integer.parseInt(xField.getText().trim());
                            Integer.parseInt(yField.getText().trim());
                            okNode.setDisable(false);
                        } catch (NumberFormatException ex) {
                            okNode.setDisable(true);
                        }
                    };
                    xField.textProperty().addListener((obs, o, n) -> validate.run());
                    yField.textProperty().addListener((obs, o, n) -> validate.run());

                    dlg.setResultConverter(bt -> {
                        if (bt == okType) {
                            try {
                                int nx = Integer.parseInt(xField.getText().trim());
                                int ny = Integer.parseInt(yField.getText().trim());
                                return new MousePosition(nx, ny);
                            } catch (NumberFormatException ignored) {}
                        }
                        return null;
                    });

                    dlg.showAndWait().ifPresent(pos -> {
                        p.setPosition(pos);
                        controller.getConfigurationManager().saveProfile(p);
                        int listIdx = tableView.getItems().indexOf(p);
                        if (listIdx >= 0) tableView.getItems().set(listIdx, p);
                    });
                });
            }
            private void cancelActiveCountdown() {
                if (activeCountdown != null) {
                    activeCountdown.shutdownNow();
                    activeCountdown = null;
                    btn.setText("Set pos");
                    btn.setDisable(false);
                    enterBtn.setDisable(false);
                }
            }

            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    cancelActiveCountdown();
                    setGraphic(null);
                    return;
                }
                lbl.setText(item);
                setGraphic(box);
            }
        });

        // ── Speed ─────────────────────────────────────────────────────────────
        TableColumn<ClickProfile, String> speedCol = new TableColumn<>("Speed");
        speedCol.setPrefWidth(200);
        speedCol.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getSpeedDisplay()));
        speedCol.setCellFactory(col -> new TableCell<>() {
            private final Label   lbl     = new Label();
            private final Label   clockIc = new Label("◎");   // Zieluhr-Symbol
            private final HBox    pill    = new HBox(lbl, new Region(), clockIc);
            {
                pill.getStyleClass().add("speed-pill");
                pill.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(pill.getChildren().get(1), Priority.ALWAYS); // spacer
                lbl.getStyleClass().add("speed-label");
                clockIc.getStyleClass().add("speed-clock-icon");
                // Klick auf die ganze Zelle / den Pill öffnet den Speed-Editor
                pill.setOnMouseClicked(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    showSpeedEditor(getTableView().getItems().get(idx));
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                lbl.setText(item);
                setGraphic(pill);
            }
        });

        // ── Mouse (Klick-Typ) ─────────────────────────────────────────────────
        TableColumn<ClickProfile, String> mouseCol = new TableColumn<>("Mouse");
        mouseCol.setPrefWidth(130);
        mouseCol.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        clickTypeLabel(cd.getValue().getClickType())));
        mouseCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("Left", "Right", "Middle"));
            {
                combo.getStyleClass().add("mouse-combo");
                combo.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    ClickProfile p = getTableView().getItems().get(idx);
                    ClickType type = switch (combo.getValue()) {
                        case "Right"  -> ClickType.RIGHT;
                        case "Middle" -> ClickType.SCROLL_UP;
                        default       -> ClickType.LEFT;
                    };
                    controller.setClickType(p, type);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                combo.setValue(item);
                setGraphic(combo);
            }
        });

        // ── Options ("⋯") ─────────────────────────────────────────────────────
        TableColumn<ClickProfile, Void> optCol = new TableColumn<>("");
        optCol.setPrefWidth(44);
        optCol.setMaxWidth(44);
        optCol.setMinWidth(44);
        optCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⋯");
            {
                btn.getStyleClass().add("options-btn");
                btn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    showOptionsMenu(getTableView().getItems().get(idx), btn);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tableView.getColumns().addAll(List.of(enabledCol, nameCol, posCol, speedCol, mouseCol, optCol));
        return tableView;
    }

    // ─── Bottom Bar ────────────────────────────────────────────────────────────

    private HBox buildBottomBar() {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("bottom-bar");
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        startStopButton = new Button("Start Click");
        startStopButton.getStyleClass().add("start-btn");
        startStopButton.setPrefWidth(140);
        startStopButton.setPrefHeight(38);
        startStopButton.setOnAction(e -> onStartStop());

        bar.getChildren().addAll(spacer, startStopButton);
        return bar;
    }

    // ─── Event Handler ─────────────────────────────────────────────────────────

    private void openSettings() {
        org.example.robo.ui.dialog.SettingsDialog dlg =
                new org.example.robo.ui.dialog.SettingsDialog(configurationManager, keyboardListener);
        dlg.showAndWait();
    }

    private void onNewClick() {
        int num = profiles.size() + 1;
        ClickProfile p = ClickProfile.createNew("MACRobo " + num);
        configurationManager.saveProfile(p);
        profiles.add(p);
        tableView.scrollTo(p);
        tableView.getSelectionModel().select(p);
    }

    private void onStartStop() {
        controller.toggleClicking(profiles);
    }

    public void triggerStartStop() {
        Platform.runLater(this::onStartStop);
    }

    public void triggerWebCaptureToggle() {
        Platform.runLater(this::onToggleWebRecording);
    }

    /** Called from UIController via Platform.runLater() when a browser event arrives. */
    public void onWebStepReceived() {
        webSteps.clear();
        webSteps.addAll(controller.getWebRecordedSteps());
        webStepsTable.refresh();
        webStatusLabel.setText("● Aufzeichnung läuft…  (" + webSteps.size() + " Schritte)");
    }

    private void showOptionsMenu(ClickProfile profile, Button anchor) {
        ContextMenu menu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Umbenennen");
        renameItem.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog(profile.getName());
            dlg.setTitle("Umbenennen");
            dlg.setHeaderText(null);
            dlg.setContentText("Neuer Name:");
            dlg.showAndWait().ifPresent(name -> {
                if (!name.isBlank()) {
                    profile.setName(name);
                    configurationManager.saveProfile(profile);
                    tableView.refresh();
                }
            });
        });

        MenuItem deleteItem = new MenuItem("Löschen");
        deleteItem.setOnAction(e -> {
            Alert dlg = new Alert(Alert.AlertType.CONFIRMATION,
                    "\"" + profile.getName() + "\" löschen?", ButtonType.OK, ButtonType.CANCEL);
            dlg.setHeaderText(null);
            dlg.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    controller.deleteProfile(profile.getId());
                    profiles.remove(profile);
                }
            });
        });

        menu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showSpeedEditor(ClickProfile profile) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Speed");
        dlg.setHeaderText(profile.getName());
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ToggleGroup group = new ToggleGroup();
        RadioButton freqRb = new RadioButton("Klicks pro Sekunde  (N / sec)");
        RadioButton intRb  = new RadioButton("Einmal alle N Sekunden  (N sec click once)");
        freqRb.setToggleGroup(group);
        intRb.setToggleGroup(group);

        Spinner<Integer> freqSpinner = new Spinner<>(1, 100, profile.getClickFrequency());
        Spinner<Integer> intSpinner  = new Spinner<>(1, 3600, profile.getIntervalSeconds());
        freqSpinner.setEditable(true);  freqSpinner.setPrefWidth(90);
        intSpinner.setEditable(true);   intSpinner.setPrefWidth(90);

        (profile.getSpeedMode() == SpeedMode.INTERVAL ? intRb : freqRb).setSelected(true);
        freqSpinner.disableProperty().bind(freqRb.selectedProperty().not());
        intSpinner.disableProperty().bind(intRb.selectedProperty().not());

        VBox content = new VBox(10,
                freqRb, new HBox(8, new Label("Hz:"), freqSpinner),
                new Separator(),
                intRb,  new HBox(8, new Label("Sekunden:"), intSpinner)
        );
        content.setPadding(new Insets(16));
        dlg.getDialogPane().setContent(content);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            if (freqRb.isSelected()) {
                profile.setSpeedMode(SpeedMode.FREQUENCY);
                profile.setClickFrequency(freqSpinner.getValue());
            } else {
                profile.setSpeedMode(SpeedMode.INTERVAL);
                profile.setIntervalSeconds(intSpinner.getValue());
            }
            configurationManager.saveProfile(profile);
            tableView.refresh();
        });
    }

    // ─── Engine Callbacks ──────────────────────────────────────────────────────

    public void setEngineRunning(boolean running) {
        Platform.runLater(() -> {
            startStopButton.getStyleClass().removeAll("start-btn", "stop-btn");
            if (running) {
                startStopButton.setText("Stop Click");
                startStopButton.getStyleClass().add("stop-btn");
            } else {
                startStopButton.setText("Start Click");
                startStopButton.getStyleClass().add("start-btn");
            }
        });
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ─── Status Updater ────────────────────────────────────────────────────────

    private void startStatusUpdater() {
        statusUpdateExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "StatusUpdater");
            t.setDaemon(true);
            return t;
        });
        statusUpdateExecutor.scheduleAtFixedRate(() -> {
            if (controller.isRecording()) {
                controller.recordMouseMove(controller.getCurrentMousePosition());
            }
            // Snapshot im Executor-Thread holen (thread-safe), dann auf FX-Thread wechseln
            if (controller.isWebRecording()) {
                List<WebRecordingStep> snapshot = new java.util.ArrayList<>(controller.getWebRecordedSteps());
                Platform.runLater(() -> {
                    // Nur aktualisieren wenn sich die Anzahl geändert hat
                    if (snapshot.size() != webSteps.size()) {
                        webSteps.setAll(snapshot);
                        webStepsTable.refresh();
                        webStatusLabel.setText("● Aufzeichnung läuft…  (" + webSteps.size() + " Schritte)");
                    }
                });
            }
        }, Constants.MOUSE_POSITION_UPDATE_INTERVAL_MS, Constants.MOUSE_POSITION_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (statusUpdateExecutor != null) {
            statusUpdateExecutor.shutdown();
            try {
                if (!statusUpdateExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    statusUpdateExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                statusUpdateExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void applyCss(Scene scene) {
        var res = getClass().getResource("/dark-theme.css");
        if (res != null) {
            scene.getStylesheets().add(res.toExternalForm());
        } else {
            logger.warn("dark-theme.css not found on classpath");
        }
    }

    private static String clickTypeLabel(ClickType type) {
        return switch (type) {
            case RIGHT       -> "Right";
            case SCROLL_UP   -> "Middle";
            case SCROLL_DOWN -> "Middle";
            default          -> "Left";
        };
    }
}
