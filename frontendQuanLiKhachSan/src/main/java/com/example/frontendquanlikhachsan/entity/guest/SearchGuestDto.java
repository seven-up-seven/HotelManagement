package com.example.frontendquanlikhachsan.entity.guest;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchGuestDto {
    Integer id;
    String name;
    String phoneNumber;
    String identificationNumber;
    String email;
    Integer accountId;

    @Override
    public String toString() {
        // Bạn có thể tùy chỉnh format sao cho dễ đọc nhất
        return "#" + id
                + (name != null ? " – " + name : "")
                + (identificationNumber != null ? " (" + identificationNumber + ")" : "")
                + (phoneNumber != null ? " – " + phoneNumber : "")
                + (email != null ? " <" + email + ">" : "");
    }
}
