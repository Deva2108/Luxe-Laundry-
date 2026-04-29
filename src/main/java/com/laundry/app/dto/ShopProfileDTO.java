package com.laundry.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProfileDTO {
    @NotBlank(message = "Shop name is required")
    private String shopName;
    private String ownerName;
    private String email;
    private String phoneNumber;
    private String address;
    
    @Min(value = 0, message = "Tax cannot be negative")
    @Max(value = 100, message = "Tax cannot exceed 100%")
    private Double taxPercentage;
    
    private String currencySymbol;
}
