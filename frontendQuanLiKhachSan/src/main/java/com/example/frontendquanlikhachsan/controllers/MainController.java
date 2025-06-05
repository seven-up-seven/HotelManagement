package com.example.frontendquanlikhachsan.controllers;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class MainController {
    private final Map<String, Integer> tabCounter = new HashMap<>();

    @FXML private TabPane tabPane;
    @FXML private AnchorPane sidebarContainer;
    @FXML private VBox taskbar;

    private boolean sidebarVisible = true;

    public void openHomeTab() {
        openTab("Home", "/com/example/frontendquanlikhachsan/views/Home.fxml");
    }

    public void openBookingTab() {
        openTab("Booking", "/com/example/frontendquanlikhachsan/views/Booking.fxml");
    }

    private void openTab(String baseTitle, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            int counter = tabCounter.getOrDefault(baseTitle, 0) + 1;
            tabCounter.put(baseTitle, counter);
            String tabTitle = baseTitle + " #" + counter++;
            Tab tab = new Tab(tabTitle, content);
            tab.setClosable(true);

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sidebarContainer);

        if (sidebarVisible) {
            slide.setToX(-sidebarContainer.getWidth());
        } else {
            slide.setToX(0);
        }

        slide.setInterpolator(Interpolator.EASE_BOTH);
        slide.play();

        sidebarVisible = !sidebarVisible;
    }
}