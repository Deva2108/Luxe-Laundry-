package com.laundry.app.dto;

import com.laundry.app.model.GarmentCategory;
import com.laundry.app.model.OrderStatus;
import com.laundry.app.model.ServiceType;
import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalOrders;
    private Long totalPriorityOrders;
    private Double totalRevenue;
    private Map<OrderStatus, Long> ordersByStatus;
    private Map<GarmentCategory, Double> revenueByCategory;
    private Map<ServiceType, Double> revenueByService;
    private List<CustomerStats> topCustomers;
}
