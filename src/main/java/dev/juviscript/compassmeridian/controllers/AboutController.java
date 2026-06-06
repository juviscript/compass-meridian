package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.MeridianApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AboutController {

    @FXML private ImageView aboutLogo;
    @FXML private Label versionLabel;

    private Stage stage;
    private double dragOffsetX;
    private double dragOffsetY;

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(
                    MeridianApp.class.getResourceAsStream(
                            "/dev/juviscript/compassmeridian/assets/logo.png"
                    )
            );
            aboutLogo.setImage(logo);
        } catch (Exception e) {
            System.err.println("[about] Could not load logo: " + e.getMessage());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
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

    @FXML
    private void onCloseClicked() {
        stage.close();
    }
}
