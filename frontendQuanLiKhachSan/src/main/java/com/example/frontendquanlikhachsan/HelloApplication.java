package com.example.frontendquanlikhachsan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Font.loadFont(
                getClass().getResourceAsStream("/com/example/frontendquanlikhachsan/assets/fonts/RozhaOne-Regular.ttf"),
                24
        );
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/frontendquanlikhachsan/views/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        URL cssUrl = getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/login.css");
        if (cssUrl == null) {
            throw new IllegalStateException("Không tìm thấy login.css");
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setTitle("Roomify");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/frontendquanlikhachsan/assets/images/logo-image.png")));
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}