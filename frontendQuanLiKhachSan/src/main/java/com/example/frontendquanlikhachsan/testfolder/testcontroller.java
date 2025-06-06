package com.example.frontendquanlikhachsan.testfolder;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class testcontroller {
    @FXML
    private TableView<ResponseAccountDto> accountTable;
    @FXML
    private TableColumn<ResponseAccountDto, Integer> idCol;
    @FXML
    private TableColumn<ResponseAccountDto, String> usernameCol;
    @FXML
    private TableColumn<ResponseAccountDto, String> passwordCol;
    @FXML
    private TableColumn<ResponseAccountDto, Integer> userRoleIdCol;
    @FXML
    private TableColumn<ResponseAccountDto, String> userRoleNameCol;
    @FXML
    private Button loadBtn;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        usernameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        passwordCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPassword()));
        userRoleIdCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUserRoleId()));
        userRoleNameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserRoleName()));

        loadBtn.setOnAction(event -> loadAccounts());
    }

    private void loadAccounts() {
        try {
            String token = ""; // Replace with actual token
            String json = ApiHttpClientCaller.call("account", ApiHttpClientCaller.Method.GET, null, token);
            List<ResponseAccountDto> list = mapper.readValue(json, new TypeReference<List<ResponseAccountDto>>() {});
            ObservableList<ResponseAccountDto> observableList = FXCollections.observableArrayList(list);
            accountTable.setItems(observableList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
