package com.example.frontendquanlikhachsan.entity.room;

import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Room {
    private int id;

    private String name;

    private String note;

    private RoomState roomState;

    private int roomTypeId;

    private RoomType roomType;

    private int  floorId;

    private Floor floor;

    private List<BookingConfirmationForm> bookingConfirmationForms = new ArrayList<>();

    private List<RentalForm> rentalForms = new ArrayList<>();
}
