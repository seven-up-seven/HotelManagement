package com.example.frontendquanlikhachsan.entity.history;

import com.example.frontendquanlikhachsan.entity.enums.Action;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseHistoryDto {
    private int id;

    private String impactor;

    private String affectedObject;

    private Integer impactorId;

    private Integer affectedObjectId;

    private Action action;

    private LocalDateTime executeAt;

    private String content;
}
