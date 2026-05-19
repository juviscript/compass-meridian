package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.MeridianApp;
import dev.juviscript.compassmeridian.serial.CompassProtocol;
import dev.juviscript.compassmeridian.utils.UIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DeviceController {

    @FXML private StackPane deviceImageContainer;
    @FXML private ImageView deviceImage;
    @FXML private Label connDot;
    @FXML private Label connLabel;
    @FXML private javafx.scene.layout.HBox connBadge;
    @FXML private Label deviceNameLabel;
    @FXML private TextField deviceNameField;
    @FXML private Label hwRevLabel;
    @FXML private Label portLabel;
    @FXML private Label buildDateLabel;
    @FXML private Label installedVersion;
    @FXML private Button checkUpdatesButton;
    @FXML private Button restartButton;
    @FXML private Label editNameIcon;
    @FXML private Label activeProfileLabel;
    @FXML private Label totalProfilesLabel;

    private boolean editMode = false;
    private CompassProtocol protocol;

    // ── Init ──────────────────────────────────────────────
    @FXML
    public void initialize() {
        try {
            Image placeholder = new Image(
                    MeridianApp.class.getResourceAsStream(
                            "/dev/juviscript/compassmeridian/assets/placeholder.png"
                    )
            );
            deviceImage.setImage(placeholder);
        } catch (Exception e) {
            System.err.println("[device] Could not load placeholder: " + e.getMessage());
        }

        UIUtils.addHoverFade(checkUpdatesButton);
        UIUtils.addHoverFade(restartButton);
        UIUtils.addClickPulse(checkUpdatesButton);
        UIUtils.addClickPulse(restartButton);

        setConnectedState(false);
    }

    // ── Device name editing ───────────────────────────────
    @FXML
    private void onDeviceNameKeyPressed(javafx.scene.input.KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            confirmNameChange();
        } else if (e.getCode() == KeyCode.ESCAPE) {
            exitEditMode();
        }
    }

    private void enterEditMode() {
        editMode = true;
        deviceNameField.setText(deviceNameLabel.getText());
        deviceNameLabel.setVisible(false);
        deviceNameLabel.setManaged(false);
        deviceNameField.setVisible(true);
        deviceNameField.setManaged(true);
        deviceNameField.requestFocus();
        deviceNameField.selectAll();

        editNameIcon.setText("✔");
        editNameIcon.getStyleClass().setAll("edit-icon", "edit-icon-confirm");
    }

    private void exitEditMode() {
        editMode = false;
        deviceNameLabel.setVisible(true);
        deviceNameLabel.setManaged(true);
        deviceNameField.setVisible(false);
        deviceNameField.setManaged(false);

        editNameIcon.setText("✎");
        editNameIcon.getStyleClass().setAll("edit-icon");
    }

    private void confirmNameChange() {
        String newName = deviceNameField.getText().trim();

        if (newName.isEmpty()) {
            exitEditMode();
            return;
        }

        if (newName.length() > 32) {
            deviceNameField.setStyle("-fx-border-color: #ff0000;");
            return;
        }

        if (protocol == null) {
            exitEditMode();
            return;
        }

        deviceNameLabel.setText(newName);
        exitEditMode();

        new Thread(() -> {
            boolean ok = protocol.setName(newName);
            if (ok) {
                protocol.save();
                System.out.println("[device] Name saved: " + newName);
            } else {
                Platform.runLater(() ->
                        System.err.println("[device] Failed to save name")
                );
            }
        }).start();
    }

    // ── Connection state ──────────────────────────────────
    public void setConnectedState(boolean isConnected) {
        connDot.getStyleClass().setAll(
                isConnected ? "status-dot-connected" : "status-dot-disconnected"
        );
        connLabel.setText(isConnected ? "CONNECTED" : "DISCONNECTED");
        connBadge.getStyleClass().setAll(
                "badge", isConnected ? "badge-green" : "badge-red"
        );
        restartButton.setDisable(!isConnected);

        // Show/hide placeholder image
        deviceImageContainer.setVisible(isConnected);
        deviceImageContainer.setManaged(isConnected);

        // Show/hide edit icon
        editNameIcon.setVisible(isConnected);
        editNameIcon.setManaged(isConnected);

        // If disconnected mid-edit, exit edit mode
        if (!isConnected && editMode) exitEditMode();

        // Reset labels when disconnected
        if (!isConnected) {
            deviceNameLabel.setText("Compass Not Detected");
            activeProfileLabel.setText("--");
            totalProfilesLabel.setText("--");
        }

        VBox parent = (VBox) restartButton.getParent();
        Tooltip.install(parent, new Tooltip(
                isConnected
                        ? "Restart the Compass device"
                        : "Compass not connected. Please connect your device."
        ));
    }

    // ── Device info ───────────────────────────────────────
    public void updateDeviceInfo(String name, String version,
                                 String hwRev, String port,
                                 String buildDate, String activeProfile,
                                 int totalProfiles) {
        deviceNameLabel.setText(name);
        hwRevLabel.setText(hwRev);
        portLabel.setText(port);
        buildDateLabel.setText(buildDate);
        installedVersion.setText("v" + version);
        activeProfileLabel.setText(activeProfile.isEmpty() ? "--" : activeProfile);
        totalProfilesLabel.setText(String.valueOf(totalProfiles));
    }

    // ── Actions ───────────────────────────────────────────
    @FXML
    private void onCheckUpdates() {
        System.out.println("[device] Update check coming in V2");
    }

    @FXML
    private void onRestartClicked() {
        if (restartButton.isDisabled() || protocol == null) return;

        new Thread(() -> {
            Platform.runLater(() -> {
                restartButton.setDisable(true);
                restartButton.setText("Restarting...");
            });

            System.out.println("[device] Sending restart command to Compass");
            protocol.restart();

            try { Thread.sleep(3000); }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> restartButton.setText("Restart Compass"));
        }).start();
    }

    @FXML
    private void onEditIconClicked() {
        if (editMode) {
            confirmNameChange();
        } else {
            enterEditMode();
        }
    }

    // ── Setters ───────────────────────────────────────────
    public void setProtocol(CompassProtocol protocol) {
        this.protocol = protocol;
    }
}