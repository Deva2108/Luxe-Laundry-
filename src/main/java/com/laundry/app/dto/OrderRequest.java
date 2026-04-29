package com.laundry.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotEmpty(message = "Order must have at least one item")
    @jakarta.validation.Valid
    private List<OrderItemDTO> items;
    
    private Boolean isPriority;
    private Double discountPercentage;
}
