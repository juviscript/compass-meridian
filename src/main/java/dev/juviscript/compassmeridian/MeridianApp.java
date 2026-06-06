package dev.juviscript.compassmeridian;

import dev.juviscript.compassmeridian.utils.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MeridianApp extends Application {

    private static Stage primaryStage;

    private void setupSystemTray(Stage stage) {
        if (!java.awt.SystemTray.isSupported()) {
            Logger.warn("tray", "System tray not supported");
            return;
        }

        try {
            java.awt.image.BufferedImage trayIcon = javax.imageio.ImageIO.read(
                    MeridianApp.class.getResourceAsStream(
                            "/dev/juviscript/compassmeridian/assets/icon.png"
                    )
            );

            java.awt.PopupMenu popup = new java.awt.PopupMenu();

            java.awt.MenuItem showItem = new java.awt.MenuItem("Show Meridian");
            showItem.addActionListener(e -> Platform.runLater(stage::show));

            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(e -> {
                Platform.runLater(stage::close);
                java.awt.SystemTray.getSystemTray().remove(
                        java.awt.SystemTray.getSystemTray().getTrayIcons()[0]
                );
            });

            popup.add(showItem);
            popup.addSeparator();
            popup.add(exitItem);

            java.awt.TrayIcon icon = new java.awt.TrayIcon(trayIcon, "Meridian", popup);
            icon.setImageAutoSize(true);
            icon.addActionListener(e -> Platform.runLater(stage::show));

            java.awt.SystemTray.getSystemTray().add(icon);

            stage.setOnCloseRequest(e -> {
                e.consume();
                stage.hide();
            });

        } catch (Exception e) {
            Logger.error("tray", "Failed to setup tray: " + e.getMessage(), e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            primaryStage = stage;

            FXMLLoader fxmlLoader = new FXMLLoader(
                    MeridianApp.class.getResource("main-view.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(
                    MeridianApp.class.getResource("styles.css").toExternalForm()
            );

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Meridian");
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(620);

            try {
                stage.getIcons().add(
                        new Image(MeridianApp.class.getResourceAsStream(
                                "/dev/juviscript/compassmeridian/assets/icon.png"
                        ))
                );
            } catch (Exception e) {
                Logger.error("app", "Could not load icon: " + e.getMessage(), e);
            }

            stage.show();
            setupSystemTray(stage);

        } catch (Exception e) {
            Logger.error("app", "Fatal error during startup: " + e.getMessage(), e);
            throw e;
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        Logger.info("app", "Meridian shutting down");
    }

    public static void main(String[] args) {
        // Initialize logger before anything else
        Logger.init();

        // Catch uncaught exceptions on any thread
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                Logger.error("uncaught", "Uncaught exception on thread ["
                        + thread.getName() + "]: " + throwable.getMessage(), throwable)
        );

        launch();
    }
}