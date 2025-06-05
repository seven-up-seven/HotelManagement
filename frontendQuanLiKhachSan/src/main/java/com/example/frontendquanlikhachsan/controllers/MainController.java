package com.example.frontendquanlikhachsan.controllers;

import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private TabPane tabPane;
    @FXML private AnchorPane sidebarContainer;

    private final Map<String, Integer> tabCounters = new HashMap<>();
    private boolean isPinned = true;
    private boolean sidebarVisible = true;

    public void initialize() {
        tabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupAutoHideBehavior();
            }
        });

        // Set initial state
        if (!isPinned) {
            sidebarContainer.setTranslateX(-sidebarContainer.getPrefWidth());
            sidebarContainer.setMouseTransparent(true);
            sidebarContainer.setManaged(false);
            sidebarContainer.setVisible(true);
        } else {
            sidebarContainer.setTranslateX(0);
            sidebarContainer.setMouseTransparent(false);
            sidebarContainer.setManaged(true);
            sidebarContainer.setVisible(true);
        }
    }

    private void openTab(String title, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            int count = tabCounters.getOrDefault(title, 0) + 1;
            tabCounters.put(title, count);

            String tabTitle = title + " #" + count;
            Tab tab = new Tab(tabTitle, content);
            tab.setClosable(true);

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openHomeTab() {
        openTab("Trang chủ", "/com/example/frontendquanlikhachsan/views/Home.fxml");
    }

    public void openBookingTab() {
        openTab("Đặt phòng", "/com/example/frontendquanlikhachsan/views/Booking.fxml");
    }

    public void openStaffTab() {
        openTab("qlnv", "/com/example/frontendquanlikhachsan/views/manager/Staff.fxml");
    }

    @FXML
    public void toggleSidebar() {
        isPinned = !isPinned;
        if (!isPinned && sidebarVisible) {
            hideSidebar();
        } else if (isPinned && !sidebarVisible) {
            showSidebar();
        }
    }

    private void showSidebar() {
        // 1. First shrink the content area (make room for sidebar)
        // We'll use constraints to properly adjust layout
        double sidebarWidth = sidebarContainer.getPrefWidth();
        tabPane.setPrefWidth(tabPane.getWidth() - sidebarWidth);

        // 2. After delay, show sidebar
        PauseTransition delay = new PauseTransition(Duration.millis(50));
        delay.setOnFinished(e -> {
            // Make sidebar part of the layout
            sidebarContainer.setManaged(true);
            sidebarContainer.setMouseTransparent(false);

            // Animate it coming in
            TranslateTransition sidebarShow = new TranslateTransition(Duration.millis(220), sidebarContainer);
            sidebarShow.setFromX(-sidebarWidth);
            sidebarShow.setToX(0);
            sidebarShow.setInterpolator(Interpolator.EASE_OUT);
            sidebarShow.play();
        });
        delay.play();

        sidebarVisible = true;
    }

    private void hideSidebar() {
        // 1. First hide the sidebar
        double sidebarWidth = sidebarContainer.getPrefWidth();
        TranslateTransition sidebarHide = new TranslateTransition(Duration.millis(300), sidebarContainer);
        sidebarHide.setToX(-sidebarWidth);
        sidebarHide.setInterpolator(Interpolator.EASE_IN);
        sidebarHide.play();

        // 2. After delay, expand content
        PauseTransition delay = new PauseTransition(Duration.millis(310));
        delay.setOnFinished(e -> {
            // Remove sidebar from layout calculations
            sidebarContainer.setManaged(false);
            sidebarContainer.setMouseTransparent(true);
            sidebarContainer.setVisible(true);

            // Clear preferred width to let the tabPane fill available space
            tabPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        });
        delay.play();

        sidebarVisible = false;
    }

    private void setupAutoHideBehavior() {
        tabPane.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (!isPinned) {
                if (event.getScreenX() < 5 && !sidebarVisible) {
                    showSidebar();
                } else if (event.getScreenX() > sidebarContainer.getPrefWidth() + 10 && sidebarVisible) {
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
                    delay.setOnFinished(e -> {
                        if (!isPinned) hideSidebar();
                    });
                    delay.play();
                }
            }
        });
    }
}
