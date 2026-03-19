package org.example.robo.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import org.example.robo.config.ConfigurationManager;
import org.example.robo.core.profile.ClickProfile;
import org.example.robo.core.profile.ClickType;
import org.example.robo.core.profile.SpeedMode;
import org.example.robo.util.Constants;
import org.example.robo.util.MousePosition;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Builds and manages the Auto-Click panel (profile table + bottom bar). */
class AutoClickPane {

    private final UIController controller;
    private final ConfigurationManager configManager;
    private final ObservableList<ClickProfile> profiles;

    private TableView<ClickProfile> tableView;
    private Button startStopButton;

    AutoClickPane(UIController controller, ConfigurationManager configManager,
                  ObservableList<ClickProfile> profiles) {
        this.controller   = controller;
        this.configManager = configManager;
        this.profiles     = profiles;
    }

    VBox build() {
        VBox content = new VBox();
        content.getStyleClass().add("content-area");
        content.getChildren().add(buildTitleBar());

        tableView = buildTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);
        content.getChildren().add(tableView);
        content.getChildren().add(buildBottomBar());
        return content;
    }

    void triggerStartStop() {
        controller.toggleClicking(profiles);
    }

    void setEngineRunning(boolean running) {
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

    void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fehler");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ─── Title bar ─────────────────────────────────────────────────────────────

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

    // ─── Profile table ─────────────────────────────────────────────────────────

    private TableView<ClickProfile> buildTable() {
        TableView<ClickProfile> table = new TableView<>(profiles);
        table.getStyleClass().add("click-table");
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Keine Einträge. Klicke \"+ New Click\"."));

        // ── Checkbox ──────────────────────────────────────────────────────────
        TableColumn<ClickProfile, Boolean> enabledCol = new TableColumn<>("");
        enabledCol.setPrefWidth(46); enabledCol.setMaxWidth(46); enabledCol.setMinWidth(46);
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
                    grid.setHgap(8); grid.setVgap(8);
                    grid.setPadding(new Insets(12));
                    grid.add(new Label("X:"), 0, 0); grid.add(xField, 1, 0);
                    grid.add(new Label("Y:"), 0, 1); grid.add(yField, 1, 1);
                    dlg.getDialogPane().setContent(grid);

                    ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

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
                                return new MousePosition(
                                        Integer.parseInt(xField.getText().trim()),
                                        Integer.parseInt(yField.getText().trim()));
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
                if (empty || item == null) { cancelActiveCountdown(); setGraphic(null); return; }
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
            private final Label lbl     = new Label();
            private final Label clockIc = new Label("◎");
            private final HBox  pill    = new HBox(lbl, new Region(), clockIc);
            {
                pill.getStyleClass().add("speed-pill");
                pill.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(pill.getChildren().get(1), Priority.ALWAYS);
                lbl.getStyleClass().add("speed-label");
                clockIc.getStyleClass().add("speed-clock-icon");
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

        // ── Mouse (click type) ────────────────────────────────────────────────
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
        optCol.setPrefWidth(44); optCol.setMaxWidth(44); optCol.setMinWidth(44);
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

        table.getColumns().addAll(List.of(enabledCol, nameCol, posCol, speedCol, mouseCol, optCol));
        return table;
    }

    // ─── Bottom bar ────────────────────────────────────────────────────────────

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
        startStopButton.setOnAction(e -> controller.toggleClicking(profiles));

        bar.getChildren().addAll(spacer, startStopButton);
        return bar;
    }

    // ─── Event handlers ────────────────────────────────────────────────────────

    private void onNewClick() {
        int num = profiles.size() + 1;
        ClickProfile p = ClickProfile.createNew("MACRobo " + num);
        configManager.saveProfile(p);
        profiles.add(p);
        tableView.scrollTo(p);
        tableView.getSelectionModel().select(p);
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
                    configManager.saveProfile(profile);
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
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    private void showSpeedEditor(ClickProfile profile) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Speed");
        dlg.setHeaderText(profile.getName());
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ToggleGroup group  = new ToggleGroup();
        RadioButton freqRb = new RadioButton("Klicks pro Sekunde  (N / sec)");
        RadioButton intRb  = new RadioButton("Einmal alle N Sekunden  (N sec click once)");
        freqRb.setToggleGroup(group);
        intRb.setToggleGroup(group);

        Spinner<Integer> freqSpinner = new Spinner<>(1, 100, profile.getClickFrequency());
        Spinner<Integer> intSpinner  = new Spinner<>(1, 3600, profile.getIntervalSeconds());
        freqSpinner.setEditable(true); freqSpinner.setPrefWidth(90);
        intSpinner.setEditable(true);  intSpinner.setPrefWidth(90);

        (profile.getSpeedMode() == SpeedMode.INTERVAL ? intRb : freqRb).setSelected(true);
        freqSpinner.disableProperty().bind(freqRb.selectedProperty().not());
        intSpinner.disableProperty().bind(intRb.selectedProperty().not());

        VBox content = new VBox(10,
                freqRb, new HBox(8, new Label("Hz:"), freqSpinner),
                new Separator(),
                intRb,  new HBox(8, new Label("Sekunden:"), intSpinner));
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
            configManager.saveProfile(profile);
            tableView.refresh();
        });
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private static String clickTypeLabel(ClickType type) {
        return switch (type) {
            case RIGHT       -> "Right";
            case SCROLL_UP   -> "Middle";
            case SCROLL_DOWN -> "Middle";
            default          -> "Left";
        };
    }
}
