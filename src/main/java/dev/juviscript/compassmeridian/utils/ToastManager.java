package dev.juviscript.compassmeridian.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Toast notification manager.
 * Shows green (success) or red (error) toasts in the top-right
 * corner of the provided root StackPane.
 */
public class ToastManager {

    private static final int    DISPLAY_MS = 3000;
    private static final int    FADE_MS    = 300;
    private static final double MAX_WIDTH  = 320;

    private final StackPane root;

    public ToastManager(StackPane root) {
        this.root = root;
    }

    // ── Public API ────────────────────────────────────────

    public void success(String message) {
        show(message, true);
    }

    public void error(String message) {
        show(message, false);
    }

    // ── Internal ──────────────────────────────────────────

    private void show(String message, boolean isSuccess) {
        Platform.runLater(() -> {
            // Icon + message
            String icon = isSuccess ? "✓  " : "✕  ";

            Label label = new Label(icon + message);
            label.getStyleClass().add(isSuccess ? "toast-label-success" : "toast-label-error");
            label.setWrapText(true);
            label.setMaxWidth(MAX_WIDTH - 40);

            HBox toast = new HBox(label);
            toast.getStyleClass().addAll("toast", isSuccess ? "toast-success" : "toast-error");
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setMaxWidth(MAX_WIDTH);
            toast.setMaxHeight(Control.USE_PREF_SIZE); // ← add this
            toast.setOpacity(0);

            // Position top right
            StackPane.setAlignment(toast, Pos.TOP_RIGHT);
            StackPane.setMargin(toast, new Insets(16, 16, 0, 0));

            root.getChildren().add(toast);

            // Animate: fade in → pause → fade out → remove
            FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_MS), toast);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            PauseTransition pause = new PauseTransition(Duration.millis(DISPLAY_MS));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_MS), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
            sequence.setOnFinished(e -> root.getChildren().remove(toast));
            sequence.play();
        });
    }
}
