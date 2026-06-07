package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.utils.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public class HelpController {

    @FXML private Label logPathLabel;
    @FXML private Button openLogButton;

    // ── URLs ──────────────────────────────────────────────
    private static final String DOCS_URL    = "https://klutch-gaming.com/docs";
    private static final String SUPPORT_URL = "mailto:support@klutch-gaming.com";
    private static final String GITHUB_URL  = "https://github.com/juviscript/compass-meridian";

    // ── Init ──────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Show log folder path
        File logDir = getLogDir();
        logPathLabel.setText(logDir.getAbsolutePath());
    }

    // ── Actions ───────────────────────────────────────────

    @FXML
    private void onOpenLogFolder() {
        try {
            File logDir = getLogDir();
            if (!logDir.exists()) logDir.mkdirs();
            Desktop.getDesktop().open(logDir);
        } catch (Exception e) {
            Logger.error("help", "Failed to open log folder: " + e.getMessage(), e);
        }
    }

    @FXML
    private void onOpenDocs() {
        openUrl(DOCS_URL);
    }

    @FXML
    private void onContactSupport() {
        openUrl(SUPPORT_URL);
    }

    @FXML
    private void onOpenGitHub() {
        openUrl(GITHUB_URL);
    }

    // ── Helpers ───────────────────────────────────────────

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            Logger.error("help", "Failed to open URL: " + url, e);
        }
    }

    private File getLogDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isEmpty()) {
            return new File(appData, "Meridian");
        }
        return new File(System.getProperty("user.home"), "Meridian");
    }
}
