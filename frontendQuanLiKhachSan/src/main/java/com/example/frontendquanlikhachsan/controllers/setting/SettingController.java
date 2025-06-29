package com.example.frontendquanlikhachsan.controllers.setting;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SettingController {
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnOption1;
    @FXML
    private Button btnOption2;
    @FXML
    private Button btnOption3;

    @FXML
    public void initialize() {
        btnOption1.setOnAction(e -> loadUI("/com/example/frontendquanlikhachsan/views/setting/UserInfo.fxml"));
        btnOption2.setOnAction(e -> loadUI("/com/example/frontendquanlikhachsan/views/setting/Account.fxml"));
        btnOption3.setOnAction(e -> loadUI("option3"));
        loadUI("/com/example/frontendquanlikhachsan/views/setting/UserInfo.fxml");
    }

    private void loadUI(String ui) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(ui));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
