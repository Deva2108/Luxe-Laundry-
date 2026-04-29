package com.laundry.app.dto;

import com.laundry.app.model.OrderStatus;
import com.laundry.app.model.PaymentStatus;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String customerName;
    private String phoneNumber;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private Boolean isPriority;
    private Double totalBill;
    private Double discountAmount;
    private Double finalBill;
    private String createdAt;
    private String estimatedDeliveryDate;
    private List<OrderItemDTO> items;
}
