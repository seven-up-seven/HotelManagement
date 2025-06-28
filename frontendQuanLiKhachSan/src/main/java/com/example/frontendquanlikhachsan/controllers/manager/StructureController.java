package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.block.BlockDto;
import com.example.frontendquanlikhachsan.entity.block.ResponseBlockDto;
import com.example.frontendquanlikhachsan.entity.floor.FloorDto;
import com.example.frontendquanlikhachsan.entity.floor.ResponseFloorDto;
import com.example.frontendquanlikhachsan.entity.room.RoomDto;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.example.frontendquanlikhachsan.entity.enums.RoomState;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.*;

public class StructureController {
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField searchId;
    @FXML private TextField searchName;
    @FXML private ComboBox<String> searchState;
    @FXML private TextField searchType;
    @FXML private TreeView<String> structureTree;
    @FXML private VBox detailPane;

    private final Map<Integer, List<Integer>> floorRoomFilter = new HashMap<>();

    private List<ResponseBlockDto> allBlocks = new ArrayList<>();
    private ContextMenu menuBackground, menuBuilding, menuFloor, menuRoom;

    private final Map<TreeItem<String>, Integer> blockMap = new HashMap<>();
    private final Map<TreeItem<String>, Integer> floorMap = new HashMap<>();
    private final Map<TreeItem<String>, Integer> roomMap  = new HashMap<>();

    private final Map<Integer, TreeItem<String>> reverseFloorMap = new HashMap<>();
    private final Map<Integer, TreeItem<String>> reverseRoomMap = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String token = "";

    @FXML
    public void initialize() {
        // Category dropdown
        categoryCombo.setItems(FXCollections.observableArrayList("Tòa", "Tầng", "Phòng"));
        categoryCombo.getSelectionModel().selectFirst();
        categoryCombo.valueProperty().addListener((o,ov,nv)-> updateSearchFields(nv));

        initContextMenus();

        // State dropdown now string options
        List<String> states = new ArrayList<>();
        states.add("Tất cả");
        for(RoomState st : RoomState.values()) {
            states.add(st.toString());
        }
        searchState.setItems(FXCollections.observableArrayList(states));
        searchState.getSelectionModel().selectFirst();

        // Search listeners
        Runnable filterListener = this::applyFilters;
        searchId.textProperty().addListener((o,ov,nv)->filterListener.run());
        searchName.textProperty().addListener((o,ov,nv)->filterListener.run());
        searchState.valueProperty().addListener((o,ov,nv)->filterListener.run());
        categoryCombo.valueProperty().addListener((o,ov,nv)->filterListener.run());

        TreeItem<String> root = new TreeItem<>("ROOT");
        root.setExpanded(true);
        structureTree.setRoot(root);
        structureTree.setShowRoot(false);

        structureTree.setCellFactory(new Callback<>() {
            @Override public TreeCell<String> call(TreeView<String> tv) {
                return new TreeCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setContextMenu(menuBackground);
                            getStyleClass().removeAll("building","floor","room");
                        } else {
                            TreeItem<String> ti = getTreeItem();
                            setText(item);
                            setGraphic(ti.getGraphic());
                            getStyleClass().removeAll("building","floor","room");
                            switch (getDepth(ti)) {
                                case 1 -> { getStyleClass().add("building"); setContextMenu(menuBuilding); }
                                case 2 -> { getStyleClass().add("floor");    setContextMenu(menuFloor);    }
                                case 3 -> { getStyleClass().add("room");     setContextMenu(menuRoom);     }
                                default -> setContextMenu(menuBackground);
                            }
                        }
                    }
                };
            }
        });

        structureTree.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            detailPane.getChildren().clear();
            if (selected != null && getDepth(selected) > 0) {
                showDetail(selected);
            }
        });

        // background right-click
        structureTree.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, ev->{
            if(structureTree.getSelectionModel().getSelectedItem()==null) {
                menuBackground.show(structureTree,ev.getScreenX(),ev.getScreenY()); ev.consume();
            }
        });

        // left-click show detail
        structureTree.getSelectionModel().selectedItemProperty()
                .addListener((obs,old,sel)->{
                    detailPane.getChildren().clear();
                    if(sel!=null && getDepth(sel)>0) showDetail(sel);
                });

        loadStructureFromApi();
    }

    private Integer getSelectedBlockId() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        return (sel != null && getDepth(sel) == 1) ? blockMap.get(sel) : null;
    }
    private Integer getSelectedFloorId() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        return (sel != null && getDepth(sel) == 2) ? floorMap.get(sel) : null;
    }
    private Integer getSelectedRoomId() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        return (sel != null && getDepth(sel) == 3) ? roomMap.get(sel) : null;
    }

    private void initContextMenus() {
        MenuItem miNewB = new MenuItem("➕ Tạo tòa"); miNewB.setOnAction(e->showBlockForm(null));
        menuBackground = new ContextMenu(miNewB);

        MenuItem miEditB = new MenuItem("✏️ Sửa tòa"); miEditB.setOnAction(e->showBlockForm(getSelectedBlockId()));
        MenuItem miNewF = new MenuItem("➕ Tạo tầng"); miNewF.setOnAction(e->showFloorForm(null));
        MenuItem miDelB = new MenuItem("❌ Xóa tòa"); miDelB.setOnAction(e->deleteBuilding());
        menuBuilding = new ContextMenu(miEditB,new SeparatorMenuItem(),miNewF,miDelB);

        MenuItem miEditF = new MenuItem("✏️ Sửa tầng"); miEditF.setOnAction(e->showFloorForm(getSelectedFloorId()));
        MenuItem miNewR = new MenuItem("➕ Tạo phòng"); miNewR.setOnAction(e->showRoomForm(null));
        MenuItem miDelF = new MenuItem("❌ Xóa tầng"); miDelF.setOnAction(e->deleteFloor());
        menuFloor = new ContextMenu(miEditF,new SeparatorMenuItem(),miNewR,miDelF);

        MenuItem miEditR = new MenuItem("✏️ Sửa phòng"); miEditR.setOnAction(e->showRoomForm(getSelectedRoomId()));
        MenuItem miDelR = new MenuItem("❌ Xóa phòng"); miDelR.setOnAction(e->deleteRoom());
        menuRoom = new ContextMenu(miEditR,miDelR);
    }

    private void updateSearchFields(String category) {
        boolean isRoom = "Phòng".equals(category);
        searchState.setDisable(!isRoom);
        searchState.setVisible(isRoom);
        // id/name always visible
    }

    private void applyFilters() {
        String cat      = categoryCombo.getValue();
        String idText   = searchId.getText().trim();
        String nameText = searchName.getText().trim().toLowerCase();
        String stateText= searchState.getValue();

        // reset bộ lọc phòng
        floorRoomFilter.clear();

        List<ResponseBlockDto> filteredBlocks = new ArrayList<>();
        for (ResponseBlockDto b : allBlocks) {
            // ----- 1) Lọc TÒA -----
            if ("Tòa".equals(cat)) {
                if ((idText.isEmpty() || String.valueOf(b.getId()).equals(idText))
                        && (nameText.isEmpty() || b.getName().toLowerCase().contains(nameText))) {
                    filteredBlocks.add(b);
                }
                continue;  // next block
            }


        List<Integer> keepFloor = new ArrayList<>();
            for (int fid : b.getFloorIds()) {
                try {
                    ResponseFloorDto f = mapper.readValue(
                            ApiHttpClientCaller.call("floor/" + fid, GET, null, token),
                            ResponseFloorDto.class
                    );

                    boolean floorOK = "Tầng".equals(cat)
                            && (idText.isEmpty()   || String.valueOf(f.getId()).equals(idText))
                            && (nameText.isEmpty() || f.getName().toLowerCase().contains(nameText));

                    List<Integer> keepRoom = new ArrayList<>();
                    if ("Phòng".equals(cat) || floorOK) {
                        for (int rid : f.getRoomIds()) {
                            ResponseRoomDto r = mapper.readValue(
                                    ApiHttpClientCaller.call("room/" + rid, GET, null, token),
                                    ResponseRoomDto.class
                            );
                            boolean roomOK = (idText.isEmpty()   || String.valueOf(r.getId()).equals(idText))
                                    && (nameText.isEmpty() || r.getName().toLowerCase().contains(nameText));
                            if (stateText != null && !"Tất cả".equals(stateText)) {
                                roomOK &= r.getRoomState().toString().equals(stateText);
                            }
                            if (roomOK) keepRoom.add(rid);
                        }
                    }

                    // nếu có ít nhất 1 phòng thoả mãn, ghi vào floorRoomFilter
                    if (!keepRoom.isEmpty()) {
                        floorRoomFilter.put(fid, keepRoom);
                    }

                    if (floorOK || !keepRoom.isEmpty()) {
                        keepFloor.add(fid);
                    }
                } catch (Exception ex) { /* ignore */ }
            }

            if (!keepFloor.isEmpty()) {
                // tạo 1 block mới chỉ với những floor thoả mãn
                ResponseBlockDto nb = new ResponseBlockDto();
                nb.setId(b.getId());
                nb.setName(b.getName());
                nb.setFloorIds(keepFloor);
                nb.setFloorNames(
                        b.getFloorNames().stream()
                                .filter(nm -> keepFloor.contains(
                                        b.getFloorNames().indexOf(nm) >= 0
                                                ? b.getFloorIds().get(b.getFloorNames().indexOf(nm))
                                                : -1
                                ))
                                .collect(Collectors.toList())
                );
                filteredBlocks.add(nb);
            }
        }

        Platform.runLater(() -> buildTree(filteredBlocks));
    }


    private void loadStructureFromApi() {
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("block",GET,null,token);
                allBlocks = mapper.readValue(json, new TypeReference<List<ResponseBlockDto>>(){});
                Platform.runLater(this::applyFilters);
            } catch(Exception e) {
                Platform.runLater(()->showError("Lỗi tải cấu trúc",e.getMessage()));
            }
        }).start();
    }

    private void showDetail(TreeItem<String> ti) {
        detailPane.getChildren().clear();
        int depth = getDepth(ti);
        try {
            if (depth == 1) {
                ResponseBlockDto b = mapper.readValue(
                        ApiHttpClientCaller.call("block/" + blockMap.get(ti), GET, null, token),
                        ResponseBlockDto.class
                );
                addDetailRow("ID", String.valueOf(b.getId()));
                addDetailRow("Tên", b.getName());

                // --- Danh sách tầng ---
                ListView<String> listView = new ListView<>();
                for (int i = 0; i < b.getFloorIds().size(); i++) {
                    Integer fid = b.getFloorIds().get(i);
                    String fname = b.getFloorNames().get(i);
                    listView.getItems().add("#" + fid + " – " + fname);
                }
                listView.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        String sel = listView.getSelectionModel().getSelectedItem();
                        int fid = Integer.parseInt(sel.split(" – ")[0].substring(1));
                        if (reverseFloorMap.containsKey(fid))
                            structureTree.getSelectionModel().select(reverseFloorMap.get(fid));
                    }
                });

                // adjust height: 24px mỗi dòng + 2px padding
                listView.setFixedCellSize(24);
                listView.setPrefHeight(listView.getItems().size() * 24 + 2);

                TitledPane floorPane = new TitledPane("Danh sách tầng (double click)", listView);

                detailPane.getChildren().add(floorPane);

            } else if (depth == 2) {
                ResponseFloorDto f = mapper.readValue(
                        ApiHttpClientCaller.call("floor/" + floorMap.get(ti), GET, null, token),
                        ResponseFloorDto.class
                );
                addDetailRow("ID", String.valueOf(f.getId()));
                addDetailRow("Tên", f.getName());
                addDetailRow("Block ID", String.valueOf(f.getBlockId()));
                addDetailRow("Block", f.getBlockName());

                // --- Danh sách phòng ---
                List<ResponseRoomDto> rooms = new ArrayList<>();
                for (int rid : f.getRoomIds()) {
                    rooms.add(mapper.readValue(
                            ApiHttpClientCaller.call("room/" + rid, GET, null, token),
                            ResponseRoomDto.class
                    ));
                }
                rooms.sort(Comparator.comparingInt(r -> roomStateOrder(r.getRoomState())));

                ObservableList<String> roomItems = FXCollections.observableArrayList();
                for (ResponseRoomDto r : rooms) {
                    roomItems.add("#" + r.getId() + " – " + r.getName() + " (" + r.getRoomState() + ")");
                }
                ListView<String> roomList = new ListView<>(roomItems);
                roomList.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        String sel = roomList.getSelectionModel().getSelectedItem();
                        int rid = Integer.parseInt(sel.split(" – ")[0].substring(1));
                        if (reverseRoomMap.containsKey(rid))
                            structureTree.getSelectionModel().select(reverseRoomMap.get(rid));
                    }
                });

                // adjust height similarly
                roomList.setFixedCellSize(24);
                roomList.setPrefHeight(roomList.getItems().size() * 24 + 2);

                TitledPane roomPane = new TitledPane("Danh sách phòng (double click)", roomList);

                detailPane.getChildren().add(roomPane);

            } else if (depth == 3) {
                ResponseRoomDto r = mapper.readValue(
                        ApiHttpClientCaller.call("room/" + roomMap.get(ti), GET, null, token),
                        ResponseRoomDto.class
                );
                addDetailRow("ID", String.valueOf(r.getId()));
                addDetailRow("Tên", r.getName());
                addDetailRow("Ghi chú", r.getNote() != null ? r.getNote() : "–");
                addDetailRow("Trạng thái", r.getRoomState().toString());
                addDetailRow("Loại phòng ID", String.valueOf(r.getRoomTypeId()));
                addDetailRow("Loại phòng", r.getRoomTypeName());
                addDetailRow("Floor ID", String.valueOf(r.getFloorId()));
                addDetailRow("Floor", r.getFloorName());
                addDetailRow("BookingConfirm IDs", String.join(", ",
                        r.getBookingConfirmationFormIds().stream().map(String::valueOf).collect(Collectors.toList())
                ));
                addDetailRow("RentalForm IDs", String.join(", ",
                        r.getRentalFormIds().stream().map(String::valueOf).collect(Collectors.toList())
                ));
            }
        } catch (Exception e) {
            showError("Lỗi tải chi tiết", e.getMessage());
        }
    }


    private void decorateRoomCircle(Circle dot) {
        dot.setStroke(Color.DODGERBLUE);
        dot.setStrokeWidth(2);
    }

    private void addDetailRow(String key, String val) {
        Label k = new Label(key + ":");
        k.setStyle("-fx-font-weight:bold;");
        Label v = new Label(val);
        HBox row = new HBox(5, k, v);
        row.setPadding(new Insets(5, 0, 5, 0));
        detailPane.getChildren().add(row);
    }


    private Label createBoldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold");
        return lbl;
    }

    private void buildTree(List<ResponseBlockDto> blocks) {
        String cat = categoryCombo.getValue();
        TreeItem<String> root = structureTree.getRoot();
        root.getChildren().clear();
        blockMap.clear(); floorMap.clear(); roomMap.clear();
        reverseFloorMap.clear(); reverseRoomMap.clear();

        for (ResponseBlockDto b : blocks) {
            TreeItem<String> blockItem = new TreeItem<>(b.getName(), new Label("🏢"));
            blockMap.put(blockItem, b.getId());

            // nếu lọc tầng hoặc phòng, auto mở toà
            if ("Tầng".equals(cat) || "Phòng".equals(cat)) {
                blockItem.setExpanded(true);
            }

            for (int i = 0; i < b.getFloorIds().size(); i++) {
                int fid = b.getFloorIds().get(i);
                try {
                    ResponseFloorDto f = mapper.readValue(
                            ApiHttpClientCaller.call("floor/" + fid, GET, null, token),
                            ResponseFloorDto.class
                    );

                    TreeItem<String> floorItem = new TreeItem<>(f.getName(), new Label("🏬"));
                    floorMap.put(floorItem, fid);
                    reverseFloorMap.put(fid, floorItem);

                    // nếu lọc phòng, auto mở tầng
                    if ("Phòng".equals(cat)) {
                        floorItem.setExpanded(true);
                    }

                    // chỉ duyệt những room đã lọc (nếu có), ngược lại lấy tất cả
                    List<Integer> roomsToShow = floorRoomFilter.containsKey(fid)
                            ? floorRoomFilter.get(fid)
                            : f.getRoomIds();

                    // load và show mỗi room
                    for (int rid : roomsToShow) {
                        ResponseRoomDto r = mapper.readValue(
                                ApiHttpClientCaller.call("room/" + rid, GET, null, token),
                                ResponseRoomDto.class
                        );
                        Color c = switch (r.getRoomState()) {
                            case BEING_RENTED -> Color.LIMEGREEN;
                            case READY_TO_SERVE -> Color.web("#fafafa");
                            case BEING_CLEANED -> Color.LIGHTGRAY;
                            case UNDER_RENOVATION -> Color.CRIMSON;
                            case BOOKED -> Color.ORANGE;
                        };
                        Circle dot = new Circle(6, c);
                        if (r.getRoomState() == RoomState.READY_TO_SERVE) {
                            dot.setStroke(Color.DODGERBLUE);
                            dot.setStrokeWidth(2);
                        }
                        TreeItem<String> roomItem = new TreeItem<>(
                                r.getName() + " – " + r.getRoomState(), dot
                        );
                        roomMap.put(roomItem, rid);
                        reverseRoomMap.put(rid, roomItem);
                        floorItem.getChildren().add(roomItem);
                    }

                    blockItem.getChildren().add(floorItem);
                } catch (Exception ignored) {}
            }

            root.getChildren().add(blockItem);
        }
    }

    private int roomStateOrder(RoomState state) {
        return switch (state) {
            case READY_TO_SERVE -> 1;
            case BOOKED -> 2;
            case BEING_RENTED -> 3;
            case UNDER_RENOVATION -> 4;
            case BEING_CLEANED -> 5;
        };
    }

    private int getDepth(TreeItem<?> ti) {
        int d = 0;
        while (ti.getParent() != null) { d++; ti = ti.getParent(); }
        return d;
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(content);

        // Thêm stylesheet cho DialogPane
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }

    //───────────────────────────────────────────────────
    // CRUD BLOCK
    //───────────────────────────────────────────────────

    private void createBuilding() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Tạo tòa mới");
        dlg.setHeaderText(null);
        dlg.setContentText("Tên tòa:");
        dlg.showAndWait().ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) {
                showError("Lỗi nhập", "Tên tòa không được để trống.");
                return;
            }
            try {
                // 1) fetch all để check trùng
                String allJson = ApiHttpClientCaller.call("block", GET, null, token);
                List<ResponseBlockDto> all = mapper.readValue(
                        allJson, new TypeReference<List<ResponseBlockDto>>() {}
                );
                @org.jetbrains.annotations.NotNull String finalName = name;
                if (all.stream().anyMatch(b -> b.getName().equalsIgnoreCase(finalName))) {
                    showError("Trùng tên", "Đã tồn tại tòa cùng tên.");
                    return;
                }
                // 2) tạo mới
                BlockDto dto = BlockDto.builder().name(name).build();
                ApiHttpClientCaller.call("block", POST, dto, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi tạo tòa", ex.getMessage());
            }
        });
    }

    private void editBuilding() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 1) return;
        int id = blockMap.get(sel);
        String oldName = sel.getValue();

        TextInputDialog dlg = new TextInputDialog(oldName);
        dlg.setTitle("Sửa tòa");
        dlg.setContentText("Tên tòa:");
        dlg.showAndWait().ifPresent(name -> {
            name = name.trim();
            if (name.isEmpty()) {
                showError("Lỗi nhập", "Tên tòa không được để trống.");
                return;
            }
            try {
                // fetch all để check trùng (ngoại trừ chính nó)
                String allJson = ApiHttpClientCaller.call("block", GET, null, token);
                List<ResponseBlockDto> all = mapper.readValue(
                        allJson, new TypeReference<List<ResponseBlockDto>>() {}
                );
                @org.jetbrains.annotations.NotNull String finalName = name;
                if (all.stream()
                        .filter(b->b.getId()!=id)
                        .anyMatch(b->b.getName().equalsIgnoreCase(finalName))) {
                    showError("Trùng tên", "Đã tồn tại tòa cùng tên.");
                    return;
                }
                // PUT
                BlockDto dto = BlockDto.builder().name(name).build();
                ApiHttpClientCaller.call("block/" + id, PUT, dto, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi sửa tòa", ex.getMessage());
            }
        });
    }

    private void deleteBuilding() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 1) return;
        int id = blockMap.get(sel);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa tòa này?", ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait().filter(b->b==ButtonType.OK).ifPresent(x->{
            try {
                ApiHttpClientCaller.call("block/" + id, DELETE, null, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi xóa tòa", ex.getMessage());
            }
        });
    }

    //───────────────────────────────────────────────────
    // CRUD FLOOR
    //───────────────────────────────────────────────────

    private void createFloor() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 1) return;
        int blockId = blockMap.get(sel);

        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Tạo tầng mới");
        dlg.setContentText("Tên tầng:");
        dlg.showAndWait().ifPresent(name-> {
            name = name.trim();
            if (name.isEmpty()) {
                showError("Lỗi nhập", "Tên tầng không được để trống.");
                return;
            }
            try {
                // fetch block detail để check floor trùng
                String blkJson = ApiHttpClientCaller.call("block", GET, null, token);
                List<ResponseBlockDto> blocks = mapper.readValue(
                        blkJson, new TypeReference<List<ResponseBlockDto>>() {}
                );
                ResponseBlockDto block = blocks.stream()
                        .filter(b->b.getId()==blockId).findFirst().orElseThrow();
                @org.jetbrains.annotations.NotNull String finalName = name;
                if (block.getFloorNames().stream()
                        .anyMatch(n->n.equalsIgnoreCase(finalName))) {
                    showError("Trùng tên", "Tầng cùng tên đã tồn tại trong tòa này.");
                    return;
                }
                // POST
                FloorDto dto = FloorDto.builder()
                        .name(name)
                        .blockId(blockId)
                        .build();
                ApiHttpClientCaller.call("floor", POST, dto, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi tạo tầng", ex.getMessage());
            }
        });
    }

    private void editFloor() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 2) return;
        int floorId  = floorMap .get(sel);
        int blockId  = blockMap.get(sel.getParent());
        String oldName = sel.getValue();

        TextInputDialog dlg = new TextInputDialog(oldName);
        dlg.setTitle("Sửa tầng");
        dlg.setContentText("Tên tầng:");
        dlg.showAndWait().ifPresent(name-> {
            name = name.trim();
            if (name.isEmpty()) {
                showError("Lỗi nhập", "Tên tầng không được để trống.");
                return;
            }
            try {
                // fetch block detail
                String blkJson = ApiHttpClientCaller.call("block", GET, null, token);
                List<ResponseBlockDto> blocks = mapper.readValue(
                        blkJson, new TypeReference<List<ResponseBlockDto>>() {}
                );
                ResponseBlockDto block = blocks.stream()
                        .filter(b->b.getId()==blockId).findFirst().orElseThrow();
                @org.jetbrains.annotations.NotNull String finalName = name;
                if (block.getFloorNames().stream()
                        .filter(n->!n.equalsIgnoreCase(oldName))
                        .anyMatch(n->n.equalsIgnoreCase(finalName))) {
                    showError("Trùng tên", "Tầng cùng tên đã tồn tại.");
                    return;
                }
                // PUT
                FloorDto dto = FloorDto.builder()
                        .name(name)
                        .blockId(blockId)
                        .build();
                ApiHttpClientCaller.call("floor/" + floorId, PUT, dto, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi sửa tầng", ex.getMessage());
            }
        });
    }

    private void deleteFloor() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 2) return;
        int floorId = floorMap.get(sel);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa tầng này?", ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait().filter(b->b==ButtonType.OK).ifPresent(x->{
            try {
                ApiHttpClientCaller.call("floor/" + floorId, DELETE, null, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi xóa tầng", ex.getMessage());
            }
        });
    }

    //───────────────────────────────────────────────────
    // CRUD ROOM
    //───────────────────────────────────────────────────

    private void createRoom() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 2) return;
        int floorId = floorMap.get(sel);

        // form đơn giản với 4 field
        Dialog<RoomDto> dlg = new Dialog<>();
        dlg.setTitle("Tạo phòng mới");
        var ok = new ButtonType("Tạo", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        var name  = new TextField();
        var note  = new TextField();
        var state = new ComboBox<RoomState>(
                javafx.collections.FXCollections.observableArrayList(RoomState.values())
        );
        state.getSelectionModel().selectFirst();
        var typeId = new TextField();

        var grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("Tên phòng:"), 0,0); grid.add(name,0,1);
        grid.add(new Label("Ghi chú:"),    1,0); grid.add(note,1,1);
        grid.add(new Label("Trạng thái:"), 0,2); grid.add(state,0,3);
        grid.add(new Label("Loại phòng ID:"),1,2); grid.add(typeId,1,3);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn==ok) {
                return RoomDto.builder()
                        .name(name.getText().trim())
                        .note(note.getText().trim())
                        .roomState(state.getValue())
                        .roomTypeId(Integer.parseInt(typeId.getText().trim()))
                        .floorId(floorId)
                        .build();
            }
            return null;
        });

        dlg.showAndWait().ifPresent(dto-> {
            if (dto.getClass().getName().isEmpty()) {
                showError("Lỗi nhập", "Tên phòng không được để trống.");
                return;
            }
            try {
                // fetch floor để check trùng
                String flJson = ApiHttpClientCaller.call("floor/" + floorId, GET, null, token);
                ResponseFloorDto f = mapper.readValue(flJson, ResponseFloorDto.class);
                if (f.getRoomNames().stream()
                        .anyMatch(n->n.equalsIgnoreCase(dto.getClass().getName()))) {
                    showError("Trùng tên", "Phòng cùng tên đã tồn tại ở tầng này.");
                    return;
                }
                // POST
                ApiHttpClientCaller.call("room", POST, dto, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi tạo phòng", ex.getMessage());
            }
        });
    }

    private void editRoom() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 3) return;
        int roomId  = roomMap .get(sel);
        int floorId = floorMap.get(sel.getParent());
        // fetch current
        try {
            String rmJson = ApiHttpClientCaller.call("room/" + roomId, GET, null, token);
            ResponseRoomDto cur = mapper.readValue(rmJson, ResponseRoomDto.class);

            Dialog<RoomDto> dlg = new Dialog<>();
            dlg.setTitle("Sửa phòng");
            var ok = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
            dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

            var name  = new TextField(cur.getName());
            var note  = new TextField(cur.getNote());
            var state = new ComboBox<RoomState>(
                    javafx.collections.FXCollections.observableArrayList(RoomState.values())
            );
            state.getSelectionModel().select(cur.getRoomState().ordinal());
            var typeId = new TextField(String.valueOf(cur.getRoomTypeId()));

            var grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
            grid.add(new Label("Tên phòng:"),    0,0); grid.add(name,0,1);
            grid.add(new Label("Ghi chú:"),       1,0); grid.add(note,1,1);
            grid.add(new Label("Trạng thái:"),   0,2); grid.add(state,0,3);
            grid.add(new Label("Loại phòng ID:"),1,2); grid.add(typeId,1,3);
            dlg.getDialogPane().setContent(grid);

            dlg.setResultConverter(btn -> {
                if (btn==ok) {
                    return RoomDto.builder()
                            .name(name.getText().trim())
                            .note(note.getText().trim())
                            .roomState(state.getValue())
                            .roomTypeId(Integer.parseInt(typeId.getText().trim()))
                            .floorId(floorId)
                            .build();
                }
                return null;
            });

            dlg.showAndWait().ifPresent(dto-> {
                if (dto.getClass().getName().isEmpty()) {
                    showError("Lỗi nhập", "Tên phòng không được để trống.");
                    return;
                }
                try {
                    // fetch floor để check trùng (ngoại trừ chính nó)
                    String flJson = ApiHttpClientCaller.call("floor/" + floorId, GET, null, token);
                    ResponseFloorDto f = mapper.readValue(flJson, ResponseFloorDto.class);
                    if (f.getRoomNames().stream()
                            .filter(n->!n.equalsIgnoreCase(cur.getName()))
                            .anyMatch(n->n.equalsIgnoreCase(dto.getClass().getName()))) {
                        showError("Trùng tên", "Phòng cùng tên đã tồn tại.");
                        return;
                    }
                    // PUT
                    ApiHttpClientCaller.call("room/" + roomId, PUT, dto, token);
                    loadStructureFromApi();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Lỗi sửa phòng", ex.getMessage());
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi tải phòng", ex.getMessage());
        }
    }

    private void deleteRoom() {
        TreeItem<String> sel = structureTree.getSelectionModel().getSelectedItem();
        if (sel == null || getDepth(sel) != 3) return;
        int roomId = roomMap.get(sel);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa phòng này?", ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait().filter(b->b==ButtonType.OK).ifPresent(x->{
            try {
                ApiHttpClientCaller.call("room/" + roomId, DELETE, null, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi xóa phòng", ex.getMessage());
            }
        });
    }

    private void showBlockForm(Integer id) {
        detailPane.getChildren().clear();
        boolean edit = id != null;
        Label hdr = new Label(edit ? "✏️ Sửa tòa" : "➕ Tạo tòa mới");
        hdr.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        TextField tfName = new TextField();
        if (edit) {
            try {
                ResponseBlockDto b = mapper.readValue(
                        ApiHttpClientCaller.call("block/" + id, GET, null, token),
                        ResponseBlockDto.class
                );
                tfName.setText(b.getName());
            } catch (Exception e) {
                showError("Lỗi tải tòa", e.getMessage());
            }
        }

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(20));
        Label lbl = new Label("Tên tòa:"); lbl.setStyle("-fx-font-weight:bold;");
        form.add(lbl, 0, 0);
        form.add(tfName, 1, 0);

        Button btnSave = new Button(edit ? "Lưu" : "Tạo");
        Button btnCancel = new Button("Hủy");
        btnSave.setOnAction(e -> {
            // ----- Duplicate check start -----
            String name = tfName.getText().trim();
            if (name.isEmpty()) {
                showError("Lỗi nhập", "Tên không được để trống.");
                return;
            }
            // fetch all for duplicate validation
            try {
                String allJson = ApiHttpClientCaller.call("block", GET, null, token);
                List<ResponseBlockDto> all = mapper.readValue(allJson, new TypeReference<List<ResponseBlockDto>>() {
                });
                if (all.stream().filter(b -> !edit || b.getId() != id).anyMatch(b -> b.getName().equalsIgnoreCase(name))) {
                    showError("Trùng tên", "Đã tồn tại toà cùng tên.");
                    return;
                }
            } catch (Exception dupEx) {
                showError("Lỗi kiểm tra trùng", dupEx.getMessage());
                return;
            }
            // ----- Duplicate check end -----

            if (name.isEmpty()) {
                showError("Lỗi nhập", "Tên tòa không được để trống.");
                return;
            }
            try {
                BlockDto dto = BlockDto.builder().name(name).build();
                if (edit) ApiHttpClientCaller.call("block/" + id, PUT, dto, token);
                else ApiHttpClientCaller.call("block", POST, dto, token);
                loadStructureFromApi();
            } catch (Exception ex) {
                showError("Lỗi lưu tòa", ex.getMessage());
            }
        });
        btnCancel.setOnAction(e -> detailPane.getChildren().clear());

        HBox actions = new HBox(10, btnSave, btnCancel);
        actions.setPadding(new Insets(10));
        detailPane.getChildren().addAll(hdr, form, actions);
    }
    private void showFloorForm(Integer id) {
        detailPane.getChildren().clear();
        boolean edit = id != null;
        Label hdr = new Label(edit ? "✏️ Sửa tầng" : "➕ Tạo tầng mới"); hdr.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        TextField tfName = new TextField();
        Integer blockId = getSelectedBlockId();
        String oldName = null;
        if(edit) {
            try {
                ResponseFloorDto f = mapper.readValue(
                        ApiHttpClientCaller.call("floor/"+id, GET, null, token),
                        ResponseFloorDto.class
                );
                tfName.setText(f.getName()); blockId = f.getBlockId(); oldName = f.getName();
            } catch(Exception e) { showError("Lỗi tải tầng",e.getMessage()); }
        }
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(20));
        form.add(new Label("Tên tầng:"),0,0); form.add(tfName,1,0);
        Button btnSave=new Button(edit?"Lưu":"Tạo"), btnCancel=new Button("Hủy");
        String finalOldName = oldName;
        Integer finalBlockId = blockId;
        btnSave.setOnAction(e->{
            String name=tfName.getText().trim(); if(name.isEmpty()){showError("Lỗi nhập","Tên tầng không được để trống.");return;}
            try {
                // duplicate check
                String blkJson = ApiHttpClientCaller.call("block/"+ finalBlockId, GET, null, token);
                ResponseBlockDto blk = mapper.readValue(blkJson, ResponseBlockDto.class);
                String finalOld = finalOldName;
                if(blk.getFloorNames().stream()
                        .filter(n->!edit||!n.equalsIgnoreCase(finalOld))
                        .anyMatch(n->n.equalsIgnoreCase(name))) {
                    showError("Trùng tên","Tầng cùng tên đã tồn tại."); return;
                }
                FloorDto dto = FloorDto.builder().name(name).blockId(finalBlockId).build();
                if(edit) ApiHttpClientCaller.call("floor/"+id, PUT, dto, token);
                else     ApiHttpClientCaller.call("floor", POST, dto, token);
                loadStructureFromApi();
            } catch(Exception ex){ showError("Lỗi lưu tầng",ex.getMessage()); }
        });
        btnCancel.setOnAction(e->detailPane.getChildren().clear());
        HBox hb=new HBox(10,btnSave,btnCancel); hb.setPadding(new Insets(10));
        detailPane.getChildren().addAll(hdr,form,hb);
    }

    private void showRoomForm(Integer id) {
        detailPane.getChildren().clear();
        boolean edit = id != null;
        Label hdr = new Label(edit ? "✏️ Sửa phòng" : "➕ Tạo phòng mới"); hdr.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        TextField tfName=new TextField(), tfNote=new TextField(), tfType=new TextField();
        ComboBox<RoomState> cb=new ComboBox<>(FXCollections.observableArrayList(RoomState.values())); cb.getSelectionModel().selectFirst();
        Integer floorId=getSelectedFloorId(); String oldName=null;
        if(edit){ try{
            ResponseRoomDto r=mapper.readValue(
                    ApiHttpClientCaller.call("room/"+id,GET,null,token),
                    ResponseRoomDto.class
            );
            tfName.setText(r.getName()); tfNote.setText(r.getNote()); cb.getSelectionModel().select(r.getRoomState()); tfType.setText(""+r.getRoomTypeId()); oldName=r.getName(); floorId=r.getFloorId();
        }catch(Exception e){showError("Lỗi tải phòng",e.getMessage());}}
        GridPane form=new GridPane(); form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(20));
        form.add(new Label("Tên phòng:"),0,0); form.add(tfName,1,0);
        form.add(new Label("Ghi chú:"),0,1); form.add(tfNote,1,1);
        form.add(new Label("Trạng thái:"),0,2); form.add(cb,1,2);
        form.add(new Label("Loại phòng ID:"),0,3); form.add(tfType,1,3);
        Button btnSave=new Button(edit?"Lưu":"Tạo"), btnCancel=new Button("Hủy");
        String finalOldName = oldName;
        Integer finalFloorId = floorId;
        btnSave.setOnAction(e->{
            String name=tfName.getText().trim(); if(name.isEmpty()){showError("Lỗi nhập","Tên phòng không được để trống.");return;}
            try{
                // duplicate check
                String flJson=ApiHttpClientCaller.call("floor/"+ finalFloorId,GET,null,token);
                ResponseFloorDto fl=mapper.readValue(flJson,ResponseFloorDto.class);
                String finalOld= finalOldName;
                if(fl.getRoomNames().stream()
                        .filter(n->!edit||!n.equalsIgnoreCase(finalOld))
                        .anyMatch(n->n.equalsIgnoreCase(name))) {
                    showError("Trùng tên","Phòng cùng tên đã tồn tại."); return;
                }
                RoomDto dto=RoomDto.builder().name(name).note(tfNote.getText().trim())
                        .roomState(cb.getValue()).roomTypeId(Integer.parseInt(tfType.getText().trim()))
                        .floorId(finalFloorId).build();
                if(edit) ApiHttpClientCaller.call("room/"+id,PUT,dto,token);
                else     ApiHttpClientCaller.call("room",POST,dto,token);
                loadStructureFromApi();
            }catch(Exception ex){showError("Lỗi lưu phòng",ex.getMessage());}
        });
        btnCancel.setOnAction(e->detailPane.getChildren().clear());
        HBox hb=new HBox(10,btnSave,btnCancel); hb.setPadding(new Insets(10));
        detailPane.getChildren().addAll(hdr,form,hb);
    }
}
