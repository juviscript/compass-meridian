package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.model.KeyMapping;
import dev.juviscript.compassmeridian.model.Profile;
import dev.juviscript.compassmeridian.serial.CompassProtocol;
import dev.juviscript.compassmeridian.utils.UIUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;

public class MappingController {

    // ── Key mapping bindings ──────────────────────────────
    @FXML private ComboBox<String> upCombo;
    @FXML private ComboBox<String> downCombo;
    @FXML private ComboBox<String> leftCombo;
    @FXML private ComboBox<String> rightCombo;
    @FXML private Button saveButton;
    @FXML private Button resetButton;

    // ── Profile bindings ──────────────────────────────────
    @FXML private VBox builtinProfilesList;
    @FXML private VBox customProfilesList;
    @FXML private TextField newProfileNameField;
    @FXML private Button saveProfileButton;
    @FXML private Label saveProfileStatus;
    @FXML private Label customCountLabel;
    @FXML private Label noCustomLabel;
    @FXML private Button saveAsProfileButton;

    // ── State ─────────────────────────────────────────────
    private CompassProtocol protocol;

    private static final List<String> KEY_OPTIONS = List.of(
            "w", "a", "s", "d", "i", "j", "k", "l",
            "UP", "DOWN", "LEFT", "RIGHT",
            "SPACE", "ENTER", "TAB", "ESC",
            "SHIFT", "CTRL", "ALT",
            "F1", "F2", "F3", "F4", "F5",
            "F6", "F7", "F8", "F9", "F10", "F11", "F12"
    );

    // ── Init ──────────────────────────────────────────────
    @FXML
    public void initialize() {
        upCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        downCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        leftCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        rightCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));

        upCombo.setValue("w");
        downCombo.setValue("s");
        leftCombo.setValue("a");
        rightCombo.setValue("d");

        if (saveButton != null)          { UIUtils.addHoverFade(saveButton);          UIUtils.addClickPulse(saveButton); }
        if (resetButton != null)         { UIUtils.addHoverFade(resetButton);         UIUtils.addClickPulse(resetButton); }
        if (saveProfileButton != null)   { UIUtils.addHoverFade(saveProfileButton);   UIUtils.addClickPulse(saveProfileButton); }
        if (saveAsProfileButton != null) { UIUtils.addHoverFade(saveAsProfileButton); UIUtils.addClickPulse(saveAsProfileButton); }
    }

    // ── Key mapping ───────────────────────────────────────
    @FXML private void onMappingChanged() {}

    @FXML
    private void onSaveClicked() {
        if (protocol == null) return;

        String up    = upCombo.getValue();
        String down  = downCombo.getValue();
        String left  = leftCombo.getValue();
        String right = rightCombo.getValue();

        new Thread(() -> {
            boolean ok = true;
            ok &= protocol.setKey("up",    up);
            ok &= protocol.setKey("down",  down);
            ok &= protocol.setKey("left",  left);
            ok &= protocol.setKey("right", right);
            boolean saved = protocol.save();

            boolean success = ok && saved;
            Platform.runLater(() ->
                    showStatus(success ? "Saved successfully" : "Save failed", !success)
            );
        }).start();
    }

    private void showStatus(String message, boolean isError) {
        if (saveProfileStatus == null) return;
        saveProfileStatus.setText(message);
        saveProfileStatus.setStyle(isError
                ? "-fx-text-fill: #cc0000; -fx-font-size: 10px;"
                : "-fx-text-fill: #00e676; -fx-font-size: 10px;");
    }

    @FXML
    private void onSaveProfileClicked() {
        if (newProfileNameField == null || protocol == null) return;
        String name = newProfileNameField.getText().trim();
        if (name.isEmpty()) { showStatus("Please enter a profile name", true); return; }
        if (name.length() > 32) { showStatus("Name too long (max 32 characters)", true); return; }

        saveProfileButton.setDisable(true);
        showStatus("Saving...", false);

        new Thread(() -> {
            boolean ok = protocol.saveProfile(name);
            Platform.runLater(() -> {
                if (saveProfileButton != null) saveProfileButton.setDisable(false);
                if (ok) {
                    newProfileNameField.clear();
                    showStatus("Profile saved!", false);
                    loadProfiles();
                } else {
                    showStatus("Failed to save profile", true);
                }
            });
        }).start();
    }

    @FXML
    private void onSaveAsProfileClicked() {
        if (protocol == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/dev/juviscript/compassmeridian/save-profile-modal.fxml"
                    )
            );

            Parent root = loader.load();
            SaveProfileModalController controller = loader.getController();

            Stage modal = new Stage();
            modal.initStyle(StageStyle.UNDECORATED);
            modal.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource(
                            "/dev/juviscript/compassmeridian/styles.css"
                    ).toExternalForm()
            );

            modal.setScene(scene);
            controller.setup(modal, protocol, getCurrentMapping(), () -> {
                Platform.runLater(this::loadProfiles);
            });

            modal.show();

        } catch (IOException e) {
            System.err.println("[mapping] Failed to open modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onResetClicked() {
        if (protocol == null) return;
        new Thread(() -> {
            boolean ok = protocol.reset();
            Platform.runLater(() -> {
                if (ok) applyMapping(new KeyMapping());
                showStatus(ok ? "Reset to WASD" : "Reset failed", !ok);
            });
        }).start();
    }

    public void applyMapping(KeyMapping mapping) {
        upCombo.setValue(mapping.getUp());
        downCombo.setValue(mapping.getDown());
        leftCombo.setValue(mapping.getLeft());
        rightCombo.setValue(mapping.getRight());
    }

    public KeyMapping getCurrentMapping() {
        return new KeyMapping(
                upCombo.getValue(),
                downCombo.getValue(),
                leftCombo.getValue(),
                rightCombo.getValue()
        );
    }

    // ── Profiles ──────────────────────────────────────────
    public void loadProfiles() {
        if (protocol == null) return;

        new Thread(() -> {
            List<Profile> profiles = protocol.getProfileList();
            Platform.runLater(() -> renderProfiles(profiles));
        }).start();
    }

    private void renderProfiles(List<Profile> profiles) {
        builtinProfilesList.getChildren().clear();
        customProfilesList.getChildren().clear();

        int customCount = 0;

        for (Profile p : profiles) {
            HBox row = buildProfileRow(p);
            if (p.isBuiltin()) {
                builtinProfilesList.getChildren().add(row);
            } else {
                customProfilesList.getChildren().add(row);
                customCount++;
            }
        }

        if (customCountLabel != null) customCountLabel.setText(customCount + " / 10");
        if (noCustomLabel != null) {
            noCustomLabel.setVisible(customCount == 0);
            noCustomLabel.setManaged(customCount == 0);
        }
    }

    private HBox buildProfileRow(Profile profile) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("profile-row");
        if (profile.isActive()) row.getStyleClass().add("profile-row-active");

        // Name + type badge on same line
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameRow, Priority.ALWAYS);

        Label nameLabel = new Label(profile.getDisplayName());
        nameLabel.getStyleClass().add("profile-name");

        // Type badge — to the right of name
        Label typeBadge = new Label(profile.isBuiltin() ? "BUILT-IN" : "CUSTOM");
        typeBadge.getStyleClass().addAll("badge",
                profile.isBuiltin() ? "badge-builtin" : "badge-custom");

        nameRow.getChildren().addAll(nameLabel, typeBadge);
        row.getChildren().add(nameRow);

        // Active badge
        if (profile.isActive()) {
            Label activeBadge = new Label("ACTIVE");
            activeBadge.getStyleClass().addAll("badge", "badge-green");
            row.getChildren().add(activeBadge);
        }

        // Load icon — only when not active
        if (!profile.isActive()) {
            Button loadBtn = new Button("▶");
            loadBtn.getStyleClass().addAll("icon-btn", "icon-btn-load");
            loadBtn.setTooltip(new Tooltip("Load profile"));
            loadBtn.setOnAction(e -> onLoadProfile(profile));
            UIUtils.addHoverFade(loadBtn);
            row.getChildren().add(loadBtn);
        }

        // Delete icon — custom only
        if (!profile.isBuiltin()) {
            Button deleteBtn = new Button("✕");
            deleteBtn.getStyleClass().addAll("icon-btn", "icon-btn-delete");
            deleteBtn.setTooltip(new Tooltip("Delete profile"));
            deleteBtn.setOnAction(e -> onDeleteProfile(profile));
            UIUtils.addHoverFade(deleteBtn);
            row.getChildren().add(deleteBtn);
        }

        return row;
    }

    @FXML
    private void onNewProfileKeyPressed(javafx.scene.input.KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) onSaveProfileClicked();
    }

    private void onLoadProfile(Profile profile) {
        new Thread(() -> {
            boolean ok = protocol.loadProfile(profile.getFilename());
            if (!ok) {
                Platform.runLater(() -> showStatus("Failed to load profile", true));
                return;
            }

            try { Thread.sleep(300); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

            List<Profile> profiles = protocol.getProfileList();
            KeyMapping mapping     = protocol.getConfig();

            Platform.runLater(() -> {
                renderProfiles(profiles);
                applyMapping(mapping);
                showStatus("Loaded: " + profile.getDisplayName(), false);
            });
        }).start();
    }

    private void onDeleteProfile(Profile profile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Profile");
        alert.setHeaderText("Delete \"" + profile.getDisplayName() + "\"?");
        alert.setContentText("This cannot be undone.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = protocol.deleteProfile(profile.getFilename());
                    Platform.runLater(() -> {
                        if (ok) {
                            showStatus("Profile deleted", false);
                            loadProfiles();
                        } else {
                            showStatus("Failed to delete profile", true);
                        }
                    });
                }).start();
            }
        });
    }

    // ── Setters ───────────────────────────────────────────
    public void setProtocol(CompassProtocol protocol) {
        this.protocol = protocol;
        loadProfiles();

        new Thread(() -> {
            var mapping = protocol.getConfig();
            Platform.runLater(() -> applyMapping(mapping));
        }).start();
    }
}