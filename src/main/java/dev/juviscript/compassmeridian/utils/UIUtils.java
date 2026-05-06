package dev.juviscript.compassmeridian.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class UIUtils {

    // ── Hover fade ────────────────────────────────────────
    /**
     * Adds a smooth fade effect on mouse enter/exit.
     * Works on any JavaFX Node — buttons, VBoxes, labels, etc.
     */
    public static void addHoverFade(Node node) {
        node.setOnMouseEntered(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), node);
            ft.setFromValue(node.getOpacity());
            ft.setToValue(1.0);
            ft.play();
        });

        node.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), node);
            ft.setFromValue(node.getOpacity());
            ft.setToValue(0.75);
            ft.play();
        });
    }

    // ── Hover scale ───────────────────────────────────────
    /**
     * Adds a subtle scale up effect on hover.
     * Good for cards and buttons.
     */
    public static void addHoverScale(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });

        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // ── Hover fade + scale ────────────────────────────────
    /**
     * Combines fade and scale for a more pronounced hover effect.
     */
    public static void addHoverFadeScale(Node node) {
        node.setOnMouseEntered(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), node);
            ft.setFromValue(node.getOpacity());
            ft.setToValue(1.0);

            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.03);
            st.setToY(1.03);

            new ParallelTransition(ft, st).play();
        });

        node.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), node);
            ft.setFromValue(node.getOpacity());
            ft.setToValue(0.75);

            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.0);
            st.setToY(1.0);

            new ParallelTransition(ft, st).play();
        });
    }

    // ── Click pulse ───────────────────────────────────────
    /**
     * Adds a quick scale down/up on click — tactile press feel.
     */
    public static void addClickPulse(Node node) {
        node.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), node);
            st.setToX(0.95);
            st.setToY(0.95);
            st.play();
        });

        node.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // ── Fade in ───────────────────────────────────────────
    /**
     * Fades a node in from invisible to fully visible.
     * Good for page transitions.
     */
    public static void fadeIn(Node node, int durationMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    // ── Fade out ──────────────────────────────────────────
    /**
     * Fades a node out then runs a callback when done.
     * Good for removing elements gracefully.
     */
    public static void fadeOut(Node node, int durationMs, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(node.getOpacity());
        ft.setToValue(0.0);
        if (onFinished != null) ft.setOnFinished(e -> onFinished.run());
        ft.play();
    }

    /**
     * Fades only the children of a node, not the node itself.
     * Useful for nav items where you want the background to stay
     * solid while the icon and label fade.
     */
    public static void addHoverFadeChildren(javafx.scene.layout.Pane node) {
        node.setOnMouseEntered(e -> {
            for (Node child : node.getChildrenUnmodifiable()) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), child);
                ft.setFromValue(child.getOpacity());
                ft.setToValue(1.0);
                ft.play();
            }
        });

        node.setOnMouseExited(e -> {
            for (Node child : node.getChildrenUnmodifiable()) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), child);
                ft.setFromValue(child.getOpacity());
                ft.setToValue(0.75);
                ft.play();
            }
        });
    }
}