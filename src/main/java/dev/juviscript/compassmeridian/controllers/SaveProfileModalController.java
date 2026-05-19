package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.model.KeyMapping;
import dev.juviscript.compassmeridian.serial.CompassProtocol;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class SaveProfileModalController {

    @FXML private TextField profileNameField;
    @FXML private Label nameError;
    @FXML private Label previewUp;
    @FXML private Label previewDown;
    @FXML private Label previewLeft;
    @FXML private Label previewRight;
    @FXML private Button saveButton;

    private Stage stage;
    private CompassProtocol protocol;
    private KeyMapping mapping;
    private Runnable onSaved;

    private double dragOffsetX;
    private double dragOffsetY;

    // ── Init ──────────────────────────────────────────────
    public void setup(Stage stage, CompassProtocol protocol,
                      KeyMapping mapping, Runnable onSaved) {
        this.stage    = stage;
        this.protocol = protocol;
        this.mapping  = mapping;
        this.onSaved  = onSaved;

        // Populate preview
        previewUp.setText(mapping.getUp());
        previewDown.setText(mapping.getDown());
        previewLeft.setText(mapping.getLeft());
        previewRight.setText(mapping.getRight());

        profileNameField.requestFocus();
    }

    // ── Titlebar drag ─────────────────────────────────────
    @FXML
    private void onTitlebarPressed(javafx.scene.input.MouseEvent e) {
        dragOffsetX = e.getScreenX() - stage.getX();
        dragOffsetY = e.getScreenY() - stage.getY();
    }

    @FXML
    private void onTitlebarDragged(javafx.scene.input.MouseEvent e) {
        stage.setX(e.getScreenX() - dragOffsetX);
        stage.setY(e.getScreenY() - dragOffsetY);
    }

    // ── Actions ───────────────────────────────────────────
    @FXML
    private void onKeyPressed(javafx.scene.input.KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER)  onSaveClicked();
        if (e.getCode() == KeyCode.ESCAPE) onCancelClicked();
    }

    @FXML
    private void onCancelClicked() {
        stage.close();
    }

    @FXML
    private void onSaveClicked() {
        String name = profileNameField.getText().trim();

        if (name.isEmpty()) {
            nameError.setText("Please enter a profile name");
            return;
        }
        if (name.length() > 32) {
            nameError.setText("Name too long (max 32 characters)");
            return;
        }

        nameError.setText("");
        saveButton.setDisable(true);
        saveButton.setText("Saving...");

        new Thread(() -> {
            // First apply current mapping to device
            boolean ok = true;
            ok &= protocol.setKey("up",    mapping.getUp());
            ok &= protocol.setKey("down",  mapping.getDown());
            ok &= protocol.setKey("left",  mapping.getLeft());
            ok &= protocol.setKey("right", mapping.getRight());

            // Then save as named profile
            if (ok) ok = protocol.saveProfile(name);
            if (ok) ok = protocol.save();

            boolean success = ok;
            Platform.runLater(() -> {
                if (success) {
                    stage.close();
                    if (onSaved != null) onSaved.run();
                } else {
                    saveButton.setDisable(false);
                    saveButton.setText("Save Profile");
                    nameError.setText("Failed to save. Please try again.");
                }
            });
        }).start();
    }
}