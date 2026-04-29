package com.laundry.app.dto;

import com.laundry.app.model.GarmentCategory;
import com.laundry.app.model.ServiceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    @NotBlank(message = "Garment name is required")
    private String garmentName;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotNull(message = "Price per item is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double pricePerItem;
    
    private GarmentCategory category;
    private ServiceType serviceType;
}
