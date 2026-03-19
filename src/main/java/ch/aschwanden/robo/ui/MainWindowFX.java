package ch.aschwanden.robo.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ch.aschwanden.robo.config.ConfigurationManager;
import ch.aschwanden.robo.core.input.KeyboardListener;
import ch.aschwanden.robo.core.profile.ClickProfile;
import ch.aschwanden.robo.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hauptfenster der MACRobo Anwendung.
 * Orchestriert Sidebar, AutoClickPane und WebCapturePane.
 */
public class MainWindowFX {
    private static final Logger logger = LoggerFactory.getLogger(MainWindowFX.class);

    private UIController controller;
    private ConfigurationManager configManager;
    private KeyboardListener keyboardListener;
    private ScheduledExecutorService statusUpdateExecutor;

    private AutoClickPane autoClickPane;
    private WebCapturePane webCapturePane;
    private Node autoClickView;
    private Node webCaptureView;
    private Button sidebarAutoClickBtn;
    private Button sidebarWebCaptureBtn;

    // ─── Public API ────────────────────────────────────────────────────────────

    public void show(Stage stage, UIController controller, List<ClickProfile> initialProfiles,
                     ConfigurationManager configurationManager, KeyboardListener keyboardListener) {
        this.controller     = controller;
        this.configManager  = configurationManager;
        this.keyboardListener = keyboardListener;

        ObservableList<ClickProfile> profiles = FXCollections.observableArrayList(initialProfiles);

        autoClickPane  = new AutoClickPane(controller, configurationManager, profiles);
        webCapturePane = new WebCapturePane(controller, keyboardListener, this::applyCss);

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

    public void triggerStartStop()         { Platform.runLater(autoClickPane::triggerStartStop); }
    public void triggerWebCaptureToggle()  { Platform.runLater(webCapturePane::toggleRecording); }
    public void onWebStepReceived()        { Platform.runLater(webCapturePane::onWebStepReceived); }
    public void setEngineRunning(boolean r){ autoClickPane.setEngineRunning(r); }
    public void showError(String message)  { autoClickPane.showError(message); }

    // ─── Layout ────────────────────────────────────────────────────────────────

    private BorderPane buildLayout() {
        autoClickView  = autoClickPane.build();
        webCaptureView = webCapturePane.build();

        StackPane contentStack = new StackPane(autoClickView, webCaptureView);
        webCaptureView.setVisible(false);

        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(contentStack);
        return root;
    }

    private void switchToView(boolean showWebCapture) {
        autoClickView.setVisible(!showWebCapture);
        webCaptureView.setVisible(showWebCapture);

        sidebarAutoClickBtn.getStyleClass().removeAll("sidebar-btn-active");
        sidebarWebCaptureBtn.getStyleClass().removeAll("sidebar-btn-active");

        if (showWebCapture) {
            sidebarWebCaptureBtn.getStyleClass().add("sidebar-btn-active");
            sidebarAutoClickBtn.setGraphic(SidebarIcons.mouseIcon(28, false));
            sidebarWebCaptureBtn.setGraphic(SidebarIcons.browserIcon(24, SidebarIcons.ICON_ACTIVE));
            webCapturePane.requestTableLayout();
        } else {
            sidebarAutoClickBtn.getStyleClass().add("sidebar-btn-active");
            sidebarAutoClickBtn.setGraphic(SidebarIcons.mouseIcon(28, true));
            sidebarWebCaptureBtn.setGraphic(SidebarIcons.browserIcon(24, SidebarIcons.ICON_DEFAULT));
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

        sidebarAutoClickBtn = sidebarBtn(SidebarIcons.mouseIcon(28, true), "MACRobo", true);
        sidebarAutoClickBtn.setOnAction(e -> switchToView(false));

        sidebarWebCaptureBtn = sidebarBtn(SidebarIcons.browserIcon(24, SidebarIcons.ICON_DEFAULT), "Web Capture", false);
        sidebarWebCaptureBtn.setOnAction(e -> switchToView(true));

        sidebar.getChildren().addAll(sidebarAutoClickBtn, sidebarWebCaptureBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button settingsBtn = sidebarBtn(SidebarIcons.gearIcon(24, SidebarIcons.ICON_DEFAULT), "Settings", false);
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

    private void openSettings() {
        ch.aschwanden.robo.ui.dialog.SettingsDialog dlg =
                new ch.aschwanden.robo.ui.dialog.SettingsDialog(configManager, keyboardListener);
        dlg.showAndWait();
    }

    // ─── Status updater ────────────────────────────────────────────────────────

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
            webCapturePane.syncWithController();
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
}
