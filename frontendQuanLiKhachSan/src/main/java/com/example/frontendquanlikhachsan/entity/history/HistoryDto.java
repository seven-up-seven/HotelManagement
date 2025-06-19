package com.example.frontendquanlikhachsan.entity.history;

import com.example.frontendquanlikhachsan.entity.enums.Action;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryDto {
    private String impactor;

    private String affectedObject;

    private Integer impactorId;

    private Integer affectedObjectId;

    private Action action;

    private String content;
}
