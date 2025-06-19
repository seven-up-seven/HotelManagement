package com.example.frontendquanlikhachsan.entity.history;

import com.example.frontendquanlikhachsan.entity.enums.Action;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class History {
    private int id;

    private String impactor;

    private String affectedObject;

    private Integer impactorId;

    private Integer affectedObjectId;

    private Action action;

    private LocalDateTime executeAt;

    private String content;
}
