package com.laundry.app.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStats {
    private String customerName;
    private String phoneNumber;
    private Long totalOrders;
    private Double totalSpent;
}
