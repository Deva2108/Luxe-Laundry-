package com.laundry.app.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProfileDTO {
    private String shopName;
    private String ownerName;
    private String email;
    private String phoneNumber;
    private String address;
    private Double taxPercentage;
    private String currencySymbol;
}
