package com.example.frontendquanlikhachsan.controllers.setting;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

@Component
public class KeyMapController {
    @FXML private ComboBox<String> searchKeyComboBox;
    @FXML private Label statusLabel;

    private final Preferences preferences = Preferences.userNodeForPackage(KeyMapController.class);
    private static final String PREF_SEARCH_KEY = "search_shortcut_key";

    @FXML
    public void initialize() {
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
        String selectedKey = searchKeyComboBox.getValue();
        if (selectedKey == null || selectedKey.isBlank()) {
            statusLabel.setText("Vui lòng chọn phím.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        preferences.put(PREF_SEARCH_KEY, selectedKey);
        statusLabel.setText("Đã lưu: Ctrl + " + selectedKey);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    // Có thể thêm hàm để dùng lại từ MainController
    public static String getSearchKey() {
        Preferences prefs = Preferences.userNodeForPackage(KeyMapController.class);
        return prefs.get(PREF_SEARCH_KEY, "T");
    }
}
