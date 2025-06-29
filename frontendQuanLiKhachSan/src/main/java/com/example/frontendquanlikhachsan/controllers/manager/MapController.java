package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.block.BlockDto;
import com.example.frontendquanlikhachsan.entity.block.ResponseBlockDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapController {
    @FXML private AnchorPane mapPane;
    @FXML private Button editMapButton, saveMapButton;
    @FXML private ScrollPane mapScrollPane;
    @FXML private VBox unplacedBlockContainer;


    private boolean isEditMode = false;
    private final Map<Integer, Node> blockNodes = new HashMap<>();
    private List<ResponseBlockDto> allBlocks;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        loadMapFromApi();
    }

    private void loadMapFromApi() {
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("block", ApiHttpClientCaller.Method.GET, null, TokenHolder.getInstance().getAccessToken());
                allBlocks = mapper.readValue(json, new TypeReference<>() {});
                Platform.runLater(this::drawMap);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    e.printStackTrace();
                    showError("Lỗi khi tải sơ đồ");
                });
            }
        }).start();
    }

    private void drawMap() {
        mapPane.getChildren().removeIf(node -> node != unplacedBlockContainer);
        unplacedBlockContainer.getChildren().clear();

        for (ResponseBlockDto block : allBlocks) {
            Rectangle rect = new Rectangle(100, 60);
            rect.setArcWidth(10); // bo góc nhẹ
            rect.setArcHeight(10);
            rect.setUserData(block.getId());
            rect.setStyle("-fx-fill: #90caf9; -fx-stroke: #1565c0; -fx-stroke-width: 1;");

            Label label = new Label(block.getName());
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d47a1; -fx-font-size: 12;");
            label.setMouseTransparent(true); // không chặn kéo

            StackPane blockContainer = new StackPane(rect, label);
            blockContainer.setPrefSize(100, 60);
            blockContainer.setUserData(block.getId());
            blockContainer.setOnScroll(event -> {
                if (event.isControlDown()) {
                    double currentRotation = blockContainer.getRotate();
                    double delta = event.getDeltaY() > 0 ? 15 : -15;
                    blockContainer.setRotate((currentRotation + delta) % 360);
                }
            });

            if (block.getPosX() == null || block.getPosY() == null || block.getPosX() < 0 || block.getPosY() < 0) {
                AnchorPane wrapper = new AnchorPane(blockContainer);
                AnchorPane.setTopAnchor(blockContainer, 0.0);
                AnchorPane.setLeftAnchor(blockContainer, 0.0);
                unplacedBlockContainer.getChildren().add(wrapper);

                setupClickToTransfer(blockContainer);
            } else {
                blockContainer.setLayoutX(block.getPosX());
                blockContainer.setLayoutY(block.getPosY());
                mapPane.getChildren().add(blockContainer);

                enableDrag(blockContainer);
            }

            blockNodes.put(block.getId(), blockContainer);
        }
    }

    @FXML
    private void enableMapEditing() {
        isEditMode = true;
        saveMapButton.setDisable(false);
        for (Node node : blockNodes.values()) {
            enableDrag(node);
        }
    }

    private void enableDrag(Node node) {
        final double[] offset = new double[2];

        node.setOnMousePressed(e -> {
            offset[0] = e.getX();
            offset[1] = e.getY();
        });

        node.setOnMouseDragged(e -> {
            double newX = e.getSceneX() - offset[0];
            double newY = e.getSceneY() - offset[1];

            node.setLayoutX(mapPane.sceneToLocal(newX, newY).getX());
            node.setLayoutY(mapPane.sceneToLocal(newX, newY).getY());
        });

        node.setOnMouseReleased(e -> {
            double rightEdge = node.getLayoutX() + node.getBoundsInParent().getWidth();
            double bottomEdge = node.getLayoutY() + node.getBoundsInParent().getHeight();

            boolean isOutsideVisibleMap =
                    node.getLayoutX() < 0 ||
                            node.getLayoutY() < 0 ||
                            rightEdge > mapScrollPane.getViewportBounds().getWidth() ||
                            bottomEdge > mapScrollPane.getViewportBounds().getHeight();

            if (isOutsideVisibleMap) {
                mapPane.getChildren().remove(node);

                // Reset layout constraints
                node.setLayoutX(0);
                node.setLayoutY(0);

                AnchorPane wrapper = new AnchorPane(node);
                AnchorPane.setTopAnchor(node, 0.0);
                AnchorPane.setLeftAnchor(node, 0.0);
                unplacedBlockContainer.getChildren().add(wrapper);

                // Gỡ drag để tránh bug sau
                node.setOnMousePressed(null);
                node.setOnMouseDragged(null);
                node.setOnMouseReleased(null);

                // Gắn lại click để chuyển về mapPane
                setupClickToTransfer(node);
            }
        });
    }

    private void setupClickToTransfer(Node node) {
        node.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) { // chỉ 1 click thôi
                // Gỡ khỏi wrapper AnchorPane
                AnchorPane wrapper = (AnchorPane) node.getParent();
                wrapper.getChildren().clear();
                unplacedBlockContainer.getChildren().remove(wrapper);

                // Reset layout constraints từ AnchorPane
                AnchorPane.setTopAnchor(node, null);
                AnchorPane.setLeftAnchor(node, null);
                AnchorPane.setBottomAnchor(node, null);
                AnchorPane.setRightAnchor(node, null);

                // Đặt node vào mapPane tại vị trí của chuột (hoặc vị trí mặc định)
                double newX = e.getSceneX() - mapPane.getBoundsInParent().getMinX();
                double newY = e.getSceneY() - mapPane.getBoundsInParent().getMinY();

                // Đảm bảo block không bị đặt ngoài map
                newX = Math.max(0, Math.min(newX, mapScrollPane.getViewportBounds().getWidth() - 100));
                newY = Math.max(0, Math.min(newY, mapScrollPane.getViewportBounds().getHeight() - 60));

                node.setLayoutX(newX);
                node.setLayoutY(newY);
                mapPane.getChildren().add(node);

                // Gỡ bỏ event handler click cũ
                node.setOnMouseClicked(null);

                enableDrag(node); // kích hoạt kéo thả lại
            }
        });
    }

    @FXML
    private void saveMapPositions() {
        isEditMode = false;
        saveMapButton.setDisable(true);

        // Chuyển đổi từ ResponseBlockDto → BlockDto để gửi lên server
        List<BlockDto> updates = allBlocks.stream().map(responseBlock -> {
            Node node = blockNodes.get(responseBlock.getId());

            BlockDto dto = new BlockDto();
            dto.setName(responseBlock.getName());
            dto.setId(responseBlock.getId());

            boolean inUnplacedContainer = node.getParent() instanceof AnchorPane &&
                    ((AnchorPane) node.getParent()).getParent() == unplacedBlockContainer;

            if (!inUnplacedContainer) {
                dto.setPosX(node.getLayoutX());
                dto.setPosY(node.getLayoutY());
            } else {
                dto.setPosX(null);
                dto.setPosY(null);
            }

            return dto;
        }).toList();

        new Thread(() -> {
            try {
                for (BlockDto dto : updates) {
                    String url = "block/"+dto.getId()+"/";
                    ApiHttpClientCaller.call(url, ApiHttpClientCaller.Method.PUT, dto);
                }
                Platform.runLater(() -> System.out.println("Vị trí block đã được cập nhật."));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Không thể cập nhật vị trí của tòa"));
            }
        }).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Lỗi");
        alert.setContentText(message);

        // Thêm stylesheet cho DialogPane
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        alert.showAndWait();
    }
}
