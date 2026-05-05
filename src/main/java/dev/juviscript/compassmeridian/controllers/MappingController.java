package dev.juviscript.compassmeridian.controllers;

import dev.juviscript.compassmeridian.model.KeyMapping;
import dev.juviscript.compassmeridian.model.Preset;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.util.List;

public class MappingController {

    @FXML private ComboBox<String> upCombo;
    @FXML private ComboBox<String> downCombo;
    @FXML private ComboBox<String> leftCombo;
    @FXML private ComboBox<String> rightCombo;
    @FXML private Button saveButton;
    @FXML private Button resetButton;

    private static final List<String> KEY_OPTIONS = List.of(
            "w", "a", "s", "d", "i", "j", "k", "l",
            "UP", "DOWN", "LEFT", "RIGHT",
            "SPACE", "ENTER", "TAB", "ESC",
            "SHIFT", "CTRL", "ALT",
            "F1", "F2", "F3", "F4", "F5",
            "F6", "F7", "F8", "F9", "F10", "F11", "F12"
    );

    // Callback to MainController for save/reset
    private Runnable onSave;
    private Runnable onReset;

    @FXML
    public void initialize() {
        upCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        downCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        leftCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));
        rightCombo.setItems(FXCollections.observableArrayList(KEY_OPTIONS));

        // Default WASD
        upCombo.setValue("w");
        downCombo.setValue("s");
        leftCombo.setValue("a");
        rightCombo.setValue("d");
    }

    @FXML private void onPresetWASD()   { applyMapping(Preset.wasd().getMapping()); }
    @FXML private void onPresetArrows() { applyMapping(Preset.arrows().getMapping()); }
    @FXML private void onPresetIJKL()   { applyMapping(Preset.ijkl().getMapping()); }
    @FXML private void onMappingChanged() {}

    @FXML
    private void onSaveClicked() {
        if (onSave != null) onSave.run();
    }

    @FXML
    private void onResetClicked() {
        if (onReset != null) onReset.run();
    }

    public void applyMapping(KeyMapping mapping) {
        upCombo.setValue(mapping.getUp());
        downCombo.setValue(mapping.getDown());
        leftCombo.setValue(mapping.getLeft());
        rightCombo.setValue(mapping.getRight());
    }

    public KeyMapping getCurrentMapping() {
        return new KeyMapping(
                upCombo.getValue(),
                downCombo.getValue(),
                leftCombo.getValue(),
                rightCombo.getValue()
        );
    }

    public void setOnSave(Runnable r)  { this.onSave  = r; }
    public void setOnReset(Runnable r) { this.onReset = r; }
}