package dev.juviscript.compassmeridian.controllers;

import com.fazecast.jSerialComm.SerialPort;
import dev.juviscript.compassmeridian.MeridianApp;
import dev.juviscript.compassmeridian.serial.CompassProtocol;
import dev.juviscript.compassmeridian.serial.CompassSerial;
import dev.juviscript.compassmeridian.utils.UIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // ── FXML bindings ─────────────────────────────────────
    @FXML private StackPane contentArea;
    @FXML private VBox navDevice;
    @FXML private VBox navMapping;
    @FXML private VBox navProfiles;
    @FXML private ImageView appIcon;
    @FXML private ImageView brandIcon;

    // ── State ─────────────────────────────────────────────
    private CompassSerial serial;
    private CompassProtocol protocol;
    private boolean connected = false;
    private DeviceController deviceController;

    private double dragOffsetX;
    private double dragOffsetY;

    private VBox currentNav;

    // ── Init ──────────────────────────────────────────────
    @FXML
    public void initialize() {
        try {
            Image icon = new Image(
                    MeridianApp.class.getResourceAsStream(
                            "/dev/juviscript/compassmeridian/assets/icon.png"
                    )
            );
            appIcon.setImage(icon);

            Image logo = new Image(
                    MeridianApp.class.getResourceAsStream(
                            "/dev/juviscript/compassmeridian/assets/logo.png"
                    )
            );
            brandIcon.setImage(logo);
        } catch (Exception e) {
            System.err.println("[ui] Could not load icon: " + e.getMessage());
        }

        // Nav hover effects
        UIUtils.addHoverFadeChildren(navDevice);
        UIUtils.addHoverFadeChildren(navMapping);
        UIUtils.addHoverFadeChildren(navProfiles);

        setActiveNav(navDevice);
        loadPage("device-page.fxml");
        autoConnect();
    }

    // ── Auto connect ──────────────────────────────────────
    private void autoConnect() {
        new Thread(() -> {
            for (int attempt = 1; attempt <= 3; attempt++) {
                System.out.println("[serial] Auto-connect attempt " + attempt);

                SerialPort port = CompassSerial.findCompass();
                if (port != null) {
                    serial = new CompassSerial();
                    if (serial.connect(port)) {
                        protocol = new CompassProtocol(serial);
                        connected = true;
                        Platform.runLater(() -> {
                            if (deviceController != null) {
                                deviceController.setConnectedState(true);
                            }
                        });
                        System.out.println("[serial] Auto-connected on attempt " + attempt);
                        return;
                    }
                }

                try { Thread.sleep(1000); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println("[serial] Auto-connect failed after 3 attempts");
        }).start();
    }

    // ── Titlebar drag ─────────────────────────────────────
    @FXML
    private void onTitlebarPressed(javafx.scene.input.MouseEvent e) {
        dragOffsetX = e.getScreenX() - getStage().getX();
        dragOffsetY = e.getScreenY() - getStage().getY();
    }

    @FXML
    private void onTitlebarDragged(javafx.scene.input.MouseEvent e) {
        getStage().setX(e.getScreenX() - dragOffsetX);
        getStage().setY(e.getScreenY() - dragOffsetY);
    }

    // ── Window controls ───────────────────────────────────
    @FXML private void onMinimize() { getStage().setIconified(true); }
    @FXML private void onMaximize() { getStage().setMaximized(!getStage().isMaximized()); }
    @FXML private void onClose()    { getStage().close(); }

    // ── Navigation ────────────────────────────────────────
    @FXML private void onNavDevice()   { setActiveNav(navDevice);   loadPage("device-page.fxml"); }
    @FXML private void onNavMapping()  { setActiveNav(navMapping);  loadPage("mapping-page.fxml"); }
    @FXML private void onNavProfiles() { setActiveNav(navProfiles); loadPage("profiles-page.fxml"); }

    private void setActiveNav(VBox selected) {
        if (currentNav != null) {
            currentNav.getStyleClass().remove("nav-item-active");
        }
        selected.getStyleClass().add("nav-item-active");
        currentNav = selected;
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/dev/juviscript/compassmeridian/" + fxmlPath
                    )
            );
            Node page = loader.load();

            // Store DeviceController reference when device page loads
            if (fxmlPath.equals("device-page.fxml")) {
                deviceController = loader.getController();
            }

            // Fade in page transition
            UIUtils.fadeIn(page, 200);
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("[ui] Failed to load page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ── Disconnect ────────────────────────────────────────
    public void disconnect() {
        if (serial != null) serial.disconnect();
        connected = false;
        if (deviceController != null) {
            deviceController.setConnectedState(false);
        }
    }

    // ── Helpers ───────────────────────────────────────────
    private Stage getStage() {
        return MeridianApp.getPrimaryStage();
    }

    public CompassSerial getSerial()         { return serial; }
    public CompassProtocol getProtocol()     { return protocol; }
    public boolean isConnected()             { return connected; }
    public DeviceController getDeviceController() { return deviceController; }
}