package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.utils.UIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import dev.juviscript.compassmeridian.MeridianApp;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DeviceController {

    @FXML private ImageView deviceImage;
    @FXML private Label connDot;
    @FXML private Label connLabel;
    @FXML private HBox connBadge;
    @FXML private Label firmwareLabel;
    @FXML private Label hwRevLabel;
    @FXML private Label portLabel;
    @FXML private Label buildDateLabel;
    @FXML private Label installedVersion;
    @FXML private Button checkUpdatesButton;

    @FXML
    public void initialize() {
        // Load placeholder image
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

        // Set initial disconnected state
        setConnectedState(false);
    }

    @FXML
    private void onCheckUpdates() {
        // V2
        System.out.println("[device] Update check coming in V2");
    }

    public void updateDeviceInfo(String version, String hwRev,
                                 String port, String buildDate) {
        firmwareLabel.setText("v" + version);
        hwRevLabel.setText(hwRev);
        portLabel.setText(port);
        buildDateLabel.setText(buildDate);
        installedVersion.setText("v" + version);
        connDot.getStyleClass().setAll("status-dot-connected");
        connLabel.setText("Connected");
    }

    @FXML private Button restartButton;

    @FXML
    private void onRestartClicked() {
        if (restartButton.isDisabled()) return;

        new Thread(() -> {
            // Send restart command over serial
            // For now we disconnect and reconnect which forces a reboot
            System.out.println("[device] Restarting Compass...");

            // Visual feedback
            Platform.runLater(() -> {
                restartButton.setDisable(true);
                restartButton.setText("Restarting...");
                connDot.getStyleClass().setAll("status-dot-disconnected");
                connLabel.setText("Restarting...");
            });

            try { Thread.sleep(2000); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            Platform.runLater(() -> {
                restartButton.setText("Restart Compass");
                restartButton.setDisable(false);
            });
        }).start();
    }

    public void setConnectedState(boolean isConnected) {
        connDot.getStyleClass().setAll(
                isConnected ? "status-dot-connected" : "status-dot-disconnected"
        );
        connLabel.setText(isConnected ? "CONNECTED" : "DISCONNECTED");
        connBadge.getStyleClass().setAll(
                "badge", isConnected ? "badge-green" : "badge-red"
        );
        restartButton.setDisable(!isConnected);

        VBox parent = (VBox) restartButton.getParent();
        Tooltip.install(parent, new Tooltip(
                isConnected
                        ? "Restart the Compass device"
                        : "Compass not connected. Please connect your device."
        ));
    }

}