package dev.juviscript.compassmeridian.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import dev.juviscript.compassmeridian.MeridianApp;

public class DeviceController {

    @FXML private ImageView deviceImage;
    @FXML private Label connDot;
    @FXML private Label connLabel;
    @FXML private Label firmwareLabel;
    @FXML private Label hwRevLabel;
    @FXML private Label portLabel;
    @FXML private Label buildDateLabel;
    @FXML private Label installedVersion;

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
}