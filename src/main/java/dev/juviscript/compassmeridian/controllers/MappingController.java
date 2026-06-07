package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.model.KeyMapping;
import dev.juviscript.compassmeridian.model.Profile;
import dev.juviscript.compassmeridian.serial.CompassProtocol;
import dev.juviscript.compassmeridian.utils.ToastManager;
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
    @FXML private ComboBox<String> clickCombo;
    @FXML private Button saveButton;
    @FXML private Button resetButton;

    // ── Deadzone bindings ─────────────────────────────────
    @FXML private Slider thresholdSlider;
    @FXML private Slider diagonalSlider;
    @FXML private Label thresholdLabel;
    @FXML private Label diagonalLabel;

    // ── Profile bindings ──────────────────────────────────
    @FXML private VBox builtinProfilesList;
    @FXML private VBox customProfilesList;
    @FXML private Label customCountLabel;
    @FXML private Label noCustomLabel;
    @FXML private Button saveAsProfileButton;

    // ── Deadzone slider config ────────────────────────────
    private static final int DZ_MIN      = 50;
    private static final int DZ_MAX      = 150;
    private static final int DZ_DIAG_MIN = 30;
    private static final int DZ_DIAG_MAX = 100;

    // ── State ─────────────────────────────────────────────
    private CompassProtocol protocol;
    private ToastManager toast;

    private static final List<String> KEY_OPTIONS = List.of(
            "w", "a", "s", "d", "i", "j", "k", "l",
            "UP", "DOWN", "LEFT", "RIGHT",
            "SPACE", "ENTER", "TAB", "ESC",
            "SHIFT", "CTRL", "ALT", "GUI", "CAPSLOCK",
            "F1", "F2", "F3", "F4", "F5", "F6",
            "F7", "F8", "F9", "F10", "F11", "F12",
            "F13", "F14", "F15", "F16", "F17", "F18",
            "F19", "F20", "F21", "F22", "F23", "F24",
            "DELETE", "INSERT", "HOME", "END", "PAGEUP", "PAGEDOWN",
            "COMMA", "PERIOD", "SLASH", "SEMICOLON", "QUOTE",
            "BACKSLASH", "MINUS", "EQUALS", "LBRACKET", "RBRACKET", "GRAVE"
    );

    // ── Init ──────────────────────────────────────────────
    @FXML
    public void initialize() {
        upCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        downCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        leftCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        rightCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        clickCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));

        upCombo.setValue("w");
        downCombo.setValue("s");
        leftCombo.setValue("a");
        rightCombo.setValue("d");
        clickCombo.setValue("SPACE");

        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int deadzone = sliderToDeadzone(newVal.intValue(), DZ_MIN, DZ_MAX);
            thresholdLabel.setText(String.valueOf(deadzone));
        });

        diagonalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int deadzone = sliderToDeadzone(newVal.intValue(), DZ_DIAG_MIN, DZ_DIAG_MAX);
            diagonalLabel.setText(String.valueOf(deadzone));
        });

        if (saveButton != null)          { UIUtils.addHoverFade(saveButton);          UIUtils.addClickPulse(saveButton); }
        if (resetButton != null)         { UIUtils.addHoverFade(resetButton);         UIUtils.addClickPulse(resetButton); }
        if (saveAsProfileButton != null) { UIUtils.addHoverFade(saveAsProfileButton); UIUtils.addClickPulse(saveAsProfileButton); }
    }

    // ── Slider inversion helpers ──────────────────────────
    private int sliderToDeadzone(int sliderValue, int dzMin, int dzMax) {
        return dzMax - sliderValue + dzMin;
    }

    private int deadzoneToSlider(int deadzone, int dzMin, int dzMax) {
        return dzMax - deadzone + dzMin;
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
        String click = clickCombo.getValue();
        int deadzone         = sliderToDeadzone((int) thresholdSlider.getValue(), DZ_MIN, DZ_MAX);
        int diagonalDeadzone = sliderToDeadzone((int) diagonalSlider.getValue(), DZ_DIAG_MIN, DZ_DIAG_MAX);

        new Thread(() -> {
            boolean ok = true;
            ok &= protocol.setKey("up",    up);
            ok &= protocol.setKey("down",  down);
            ok &= protocol.setKey("left",  left);
            ok &= protocol.setKey("right", right);
            ok &= protocol.setKey("click", click);
            ok &= protocol.setDeadzone(deadzone);
            ok &= protocol.setDiagonalDeadzone(diagonalDeadzone);
            boolean saved = protocol.save();

            boolean success = ok && saved;
            Platform.runLater(() -> {
                if (toast != null) {
                    if (success) toast.success("Mapping saved successfully");
                    else toast.error("Failed to save mapping");
                }
            });
        }).start();
    }

    @FXML
    private void onResetClicked() {
        if (protocol == null) return;
        new Thread(() -> {
            boolean ok = protocol.reset();
            Platform.runLater(() -> {
                if (ok) {
                    applyMapping(new KeyMapping());
                    if (toast != null) toast.success("Reset to defaults");
                } else {
                    if (toast != null) toast.error("Reset failed");
                }
            });
        }).start();
    }

    @FXML
    private void onSaveAsProfileClicked() {
        if (protocol == null) return;
        openSaveProfileModal();
    }

    private void openSaveProfileModal() {
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
                Platform.runLater(() -> {
                    loadProfiles();
                    if (toast != null) toast.success("Profile saved");
                });
            });
            modal.show();

        } catch (IOException e) {
            System.err.println("[mapping] Failed to open modal: " + e.getMessage());
        }
    }

    // ── Mapping helpers ───────────────────────────────────
    public void applyMapping(KeyMapping mapping) {
        upCombo.setValue(mapping.getUp());
        downCombo.setValue(mapping.getDown());
        leftCombo.setValue(mapping.getLeft());
        rightCombo.setValue(mapping.getRight());
        clickCombo.setValue(mapping.getClick());

        int dzSlider     = deadzoneToSlider(mapping.getDeadzone(), DZ_MIN, DZ_MAX);
        int diagDzSlider = deadzoneToSlider(mapping.getDiagonalDeadzone(), DZ_DIAG_MIN, DZ_DIAG_MAX);

        thresholdSlider.setValue(dzSlider);
        diagonalSlider.setValue(diagDzSlider);
        thresholdLabel.setText(String.valueOf(mapping.getDeadzone()));
        diagonalLabel.setText(String.valueOf(mapping.getDiagonalDeadzone()));
    }

    public KeyMapping getCurrentMapping() {
        return new KeyMapping(
                upCombo.getValue(),
                downCombo.getValue(),
                leftCombo.getValue(),
                rightCombo.getValue(),
                clickCombo.getValue(),
                sliderToDeadzone((int) thresholdSlider.getValue(), DZ_MIN, DZ_MAX),
                sliderToDeadzone((int) diagonalSlider.getValue(), DZ_DIAG_MIN, DZ_DIAG_MAX)
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

        // Name + type badge
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameRow, Priority.ALWAYS);

        Label nameLabel = new Label(profile.getDisplayName());
        nameLabel.getStyleClass().add("profile-name");

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

        // Set Active button — only when not active
        if (!profile.isActive()) {
            Button setActiveBtn = new Button("Set Active");
            setActiveBtn.getStyleClass().add("btn-set-active");
            setActiveBtn.setOnAction(e -> onLoadProfile(profile));
            UIUtils.addHoverFade(setActiveBtn);
            row.getChildren().add(setActiveBtn);
        }

        // Delete button — custom only
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

    private void onLoadProfile(Profile profile) {
        new Thread(() -> {
            boolean ok = protocol.loadProfile(profile.getFilename());
            if (!ok) {
                Platform.runLater(() -> {
                    if (toast != null) toast.error("Failed to load profile");
                });
                return;
            }
            try { Thread.sleep(300); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

            List<Profile> profiles = protocol.getProfileList();
            KeyMapping mapping     = protocol.getConfig();

            Platform.runLater(() -> {
                renderProfiles(profiles);
                applyMapping(mapping);
                if (toast != null) toast.success("Loaded: " + profile.getDisplayName());
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
                            if (toast != null) toast.success("Profile deleted");
                            loadProfiles();
                        } else {
                            if (toast != null) toast.error("Failed to delete profile");
                        }
                    });
                }).start();
            }
        });
    }

    // ── Setters ───────────────────────────────────────────
    public void setToast(ToastManager toast) {
        this.toast = toast;
    }

    public void setProtocol(CompassProtocol protocol) {
        this.protocol = protocol;
        loadProfiles();
        new Thread(() -> {
            var mapping = protocol.getConfig();
            Platform.runLater(() -> applyMapping(mapping));
        }).start();
    }
}