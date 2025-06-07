package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class GuestManagingController {
    @FXML
    private TableColumn<ResponseGuestDto, Integer> colAge;

    @FXML
    private TableColumn<ResponseGuestDto, String> colEmail;

    @FXML
    private TableColumn<ResponseGuestDto, Integer> colId;

    @FXML
    private TableColumn<ResponseGuestDto, String> colIdentificationNumber;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableColumn<?, ?> colPhoneNumber;

    @FXML
    private TableColumn<?, ?> colSex;

    @FXML
    private VBox detailPane;

    @FXML
    private TableView<?> tableGuest;

    @FXML
    void onCreateGuest(ActionEvent event) {

    }
}
