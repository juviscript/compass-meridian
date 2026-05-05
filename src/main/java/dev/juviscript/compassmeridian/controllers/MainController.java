package dev.juviscript.compassmeridian.controllers;

import com.fazecast.jSerialComm.SerialPort;
import dev.juviscript.compassmeridian.MeridianApp;
import dev.juviscript.compassmeridian.serial.CompassProtocol;
import dev.juviscript.compassmeridian.serial.CompassSerial;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
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
    @FXML private Label statusDot;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;
    @FXML private ImageView appIcon;
    @FXML private ImageView brandIcon;

    // ── State ─────────────────────────────────────────────
    private CompassSerial serial;
    private CompassProtocol protocol;
    private boolean connected = false;

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
            brandIcon.setImage(icon);
        } catch (Exception e) {
            System.err.println("[ui] Could not load icon: " + e.getMessage());
        }

        setActiveNav(navDevice);
        loadPage("device-page.fxml");
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
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("[ui] Failed to load page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ── Connection ────────────────────────────────────────
    @FXML
    private void onConnectClicked() {
        if (connected) {
            disconnect();
            return;
        }

        serial = new CompassSerial();
        SerialPort port = CompassSerial.findCompass();

        if (port == null) {
            statusLabel.setText("Not found");
            return;
        }

        if (serial.connect(port)) {
            protocol = new CompassProtocol(serial);
            setConnectedState(true);
        } else {
            statusLabel.setText("Failed");
        }
    }

    private void disconnect() {
        if (serial != null) serial.disconnect();
        setConnectedState(false);
    }

    private void setConnectedState(boolean isConnected) {
        this.connected = isConnected;
        connectButton.setText(isConnected ? "Disconnect" : "Connect");
        statusDot.getStyleClass().setAll(
                isConnected ? "status-dot-connected" : "status-dot-disconnected"
        );
        statusLabel.setText(isConnected ? "Connected" : "Disconnected");
    }

    // ── Helper ────────────────────────────────────────────
    private Stage getStage() {
        return MeridianApp.getPrimaryStage();
    }

    public CompassSerial getSerial()     { return serial; }
    public CompassProtocol getProtocol() { return protocol; }
    public boolean isConnected()         { return connected; }
}