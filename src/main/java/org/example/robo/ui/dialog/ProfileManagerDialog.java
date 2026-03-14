package org.example.robo.ui.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.robo.config.ConfigurationManager;
import org.example.robo.core.profile.ClickProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Dialog zur Verwaltung von Click-Profilen (Save/Load/Delete).
 */
public class ProfileManagerDialog extends Stage {
    private static final Logger logger = LoggerFactory.getLogger(ProfileManagerDialog.class);

    private final ConfigurationManager configurationManager;
    private final TableView<ClickProfile> profileTable;
    private ClickProfile selectedProfile;

    public ProfileManagerDialog(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        this.profileTable = new TableView<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Profile Manager");
        setWidth(600);
        setHeight(400);
        setResizable(true);
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Titel
        Label titleLabel = new Label("Manage Click Profiles");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Profile-Tabelle
        setupProfileTable();
        ScrollPane tableScroll = new ScrollPane(profileTable);
        tableScroll.setFitToWidth(true);

        // Buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(
                titleLabel,
                new Separator(),
                tableScroll,
                buttonBox
        );

        Scene scene = new Scene(root);
        setScene(scene);

        // Lade Profile beim Öffnen
        loadProfiles();
    }

    private void setupProfileTable() {
        profileTable.setPrefHeight(300);

        // Name Column
        TableColumn<ClickProfile, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);

        // Description Column
        TableColumn<ClickProfile, String> descColumn = new TableColumn<>("Description");
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descColumn.setPrefWidth(250);

        // Frequency Column
        TableColumn<ClickProfile, Integer> freqColumn = new TableColumn<>("Frequency (Hz)");
        freqColumn.setCellValueFactory(new PropertyValueFactory<>("clickFrequency"));
        freqColumn.setPrefWidth(80);

        profileTable.getColumns().addAll(nameColumn, descColumn, freqColumn);
        profileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Selection Listener
        profileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedProfile = newVal;
        });
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");

        Button newButton = new Button("New");
        newButton.setPrefWidth(100);
        newButton.setOnAction(e -> onNewProfile());

        Button editButton = new Button("Edit");
        editButton.setPrefWidth(100);
        editButton.setOnAction(e -> onEditProfile());
        editButton.setDisable(true);
        profileTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> editButton.setDisable(newVal == null));

        Button deleteButton = new Button("Delete");
        deleteButton.setPrefWidth(100);
        deleteButton.setStyle("-fx-text-fill: #cc0000;");
        deleteButton.setOnAction(e -> onDeleteProfile());
        deleteButton.setDisable(true);
        profileTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> deleteButton.setDisable(newVal == null));

        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(100);
        closeButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(newButton, editButton, deleteButton, closeButton);
        return buttonBox;
    }

    private void loadProfiles() {
        try {
            List<ClickProfile> profiles = configurationManager.getAllProfiles();
            ObservableList<ClickProfile> observableProfiles = FXCollections.observableArrayList(profiles);
            profileTable.setItems(observableProfiles);
            logger.info("Loaded {} profiles", profiles.size());
        } catch (Exception e) {
            logger.error("Error loading profiles", e);
            showError("Error loading profiles: " + e.getMessage());
        }
    }

    private void onNewProfile() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Profile");
        dialog.setHeaderText("Create new click profile");
        dialog.setContentText("Profile name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get().trim();
            if (!name.isEmpty()) {
                ClickProfile newProfile = new ClickProfile();
                newProfile.setId("profile-" + System.currentTimeMillis());
                newProfile.setName(name);
                newProfile.setDescription("New profile");
                newProfile.setClickFrequency(10);
                try {
                    configurationManager.saveProfile(newProfile);
                    loadProfiles();
                    logger.info("New profile created: {}", name);
                } catch (Exception e) {
                    logger.error("Error creating profile", e);
                    showError("Error creating profile: " + e.getMessage());
                }
            }
        }
    }

    private void onEditProfile() {
        if (selectedProfile == null) {
            showError("Please select a profile to edit");
            return;
        }

        // Einfacher Dialog zum Ändern des Namens
        TextInputDialog dialog = new TextInputDialog(selectedProfile.getName());
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Edit profile name");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();
            if (!newName.isEmpty() && !newName.equals(selectedProfile.getName())) {
                selectedProfile.setName(newName);
                try {
                    configurationManager.saveProfile(selectedProfile);
                    loadProfiles();
                    logger.info("Profile updated: {}", newName);
                } catch (Exception e) {
                    logger.error("Error updating profile", e);
                    showError("Error updating profile: " + e.getMessage());
                }
            }
        }
    }

    private void onDeleteProfile() {
        if (selectedProfile == null) {
            showError("Please select a profile to delete");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Profile");
        confirmAlert.setHeaderText("Delete profile?");
        confirmAlert.setContentText("Are you sure you want to delete \"" + selectedProfile.getName() + "\"?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                configurationManager.deleteProfile(selectedProfile.getId());
                loadProfiles();
                logger.info("Profile deleted: {}", selectedProfile.getName());
            } catch (Exception e) {
                logger.error("Error deleting profile", e);
                showError("Error deleting profile: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gibt das aktuell ausgewählte Profil zurück.
     */
    public ClickProfile getSelectedProfile() {
        return selectedProfile;
    }
}

