package com.laundry.app.dto;

import com.laundry.app.model.GarmentCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarmentSettingDTO {
    private Long id;
    
    @NotBlank(message = "Garment name is required")
    private String garmentName;
    
    @NotNull(message = "Category is required")
    private GarmentCategory defaultCategory;
    
    @NotNull(message = "Default price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double defaultPrice;
}
