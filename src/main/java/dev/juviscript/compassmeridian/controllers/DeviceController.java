package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.serial.CompassProtocol;
import dev.juviscript.compassmeridian.serial.CompassSerial;
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

    private CompassProtocol protocol;

    public void setProtocol(CompassProtocol protocol) {
        this.protocol = protocol;
    }

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
        if (restartButton.isDisabled() || protocol == null) {
            System.err.println("[device] Cannot restart - protocol is null");
            return;
        }

        new Thread(() -> {
            Platform.runLater(() -> {
                restartButton.setDisable(true);
                restartButton.setText("Restarting...");
            });

            try {
                System.out.println("[device] Sending restart command to Compass");
                protocol.restart();
                Thread.sleep(3000);
            } // wait for board to reboot
            catch (InterruptedException e) {
                System.out.println("[device] Restart interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> restartButton.setText("Restart Compass"));
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