package com.example.frontendquanlikhachsan.controllers.setting;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

@Component
public class KeyMapController {
    @FXML
    private ComboBox<String> searchKeyComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> modifierComboBox;

    private final Preferences preferences = Preferences.userNodeForPackage(KeyMapController.class);
    private static final String PREF_SEARCH_KEY = "search_shortcut_key";
    private static final String PREF_SEARCH_MODIFIER = "search_shortcut_modifier";

    @FXML
    public void initialize() {
        modifierComboBox.setItems(FXCollections.observableArrayList(
                "CTRL", "ALT", "SHIFT", "META"
        ));
        // Load saved modifier, default CTRL
        String savedMod = preferences.get(PREF_SEARCH_MODIFIER, "CTRL");
        modifierComboBox.setValue(savedMod);

        // Populate A-Z
        searchKeyComboBox.setItems(FXCollections.observableArrayList(
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"
        ));

        // Load from preferences
        String savedKey = preferences.get(PREF_SEARCH_KEY, "T");
        searchKeyComboBox.setValue(savedKey);
    }

    @FXML
    public void handleSave() {
        String mod = modifierComboBox.getValue();
        String key = searchKeyComboBox.getValue();
        if (mod == null || key == null || key.isBlank()) {
            statusLabel.setText("Vui lòng chọn đầy đủ phím tắt.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        preferences.put(PREF_SEARCH_MODIFIER, mod);
        preferences.put(PREF_SEARCH_KEY, key);

        statusLabel.setText("Đã lưu: " + mod + " + " + key);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    public static String getSearchModifier() {
        Preferences p = Preferences.userNodeForPackage(KeyMapController.class);
        return p.get(PREF_SEARCH_MODIFIER, "CTRL");
    }

    // Có thể thêm hàm để dùng lại từ MainController
    public static String getSearchKey() {
        Preferences prefs = Preferences.userNodeForPackage(KeyMapController.class);
        return prefs.get(PREF_SEARCH_KEY, "T");
    }
}
