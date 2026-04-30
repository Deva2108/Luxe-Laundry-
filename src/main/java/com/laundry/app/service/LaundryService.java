package com.laundry.app.service;

import com.laundry.app.dto.*;
import com.laundry.app.model.*;
import com.laundry.app.repository.GarmentSettingRepository;
import com.laundry.app.repository.OrderRepository;
import com.laundry.app.repository.ShopProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaundryService {

    private final OrderRepository orderRepository;
    private final GarmentSettingRepository garmentSettingRepository;
    private final ShopProfileRepository profileRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public List<GarmentSettingDTO> getGarmentLibrary() {
        return garmentSettingRepository.findAll().stream()
                .map(this::mapToSettingDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GarmentSettingDTO saveGarmentSetting(GarmentSettingDTO dto) {
        Optional<GarmentSetting> existing = garmentSettingRepository.findByGarmentNameIgnoreCase(dto.getGarmentName());
        GarmentSetting setting;
        if (existing.isPresent()) {
            setting = existing.get();
            setting.setDefaultCategory(dto.getDefaultCategory());
            setting.setDefaultPrice(dto.getDefaultPrice());
        } else {
            setting = GarmentSetting.builder()
                    .garmentName(dto.getGarmentName())
                    .defaultCategory(dto.getDefaultCategory())
                    .defaultPrice(dto.getDefaultPrice())
                    .build();
        }
        return mapToSettingDTO(garmentSettingRepository.save(setting));
    }

    private GarmentSettingDTO mapToSettingDTO(GarmentSetting setting) {
        return GarmentSettingDTO.builder()
                .id(setting.getId())
                .garmentName(setting.getGarmentName())
                .defaultCategory(setting.getDefaultCategory())
                .defaultPrice(setting.getDefaultPrice())
                .build();
    }

    @Transactional(readOnly = true)
    public List<String> getUniqueGarments() {
        return orderRepository.findDistinctGarmentNames();
    }

    @Transactional
    public void deleteGarmentSetting(Long id) {
        garmentSettingRepository.deleteById(id);
    }

    @Transactional
    public void deleteOrder(String orderId) {
        LaundryOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
        orderRepository.delete(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Double taxPct = profileRepository.findById(1L)
                .map(ShopProfile::getTaxPercentage).orElse(0.0);

        String uniqueOrderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        double baseBill = request.getItems().stream()
                .mapToDouble(item -> item.getPricePerItem() * item.getQuantity())
                .sum();

        boolean isPriority = request.getIsPriority() != null && request.getIsPriority();
        double prioritySurcharge = isPriority ? baseBill * 0.20 : 0.0;
        double subtotal = baseBill + prioritySurcharge;

        double discountPct = request.getDiscountPercentage() != null ? Math.max(0.0, Math.min(request.getDiscountPercentage(), 100.0)) : 0.0;
        double discountAmount = subtotal * (discountPct / 100.0);
        
        double taxableAmount = subtotal - discountAmount;
        double taxAmount = taxableAmount * (taxPct / 100.0);
        double finalBill = taxableAmount + taxAmount;
        
        int deliveryDays = isPriority ? 1 : 2;

        LaundryOrder order = LaundryOrder.builder()
                .orderId(uniqueOrderId)
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .status(OrderStatus.RECEIVED)
                .paymentStatus(PaymentStatus.PENDING)
                .isPriority(isPriority)
                .totalBill(subtotal)
                .discountAmount(discountAmount)
                .finalBill(finalBill)
                .createdAt(LocalDateTime.now())
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(deliveryDays))
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> OrderItem.builder()
                        .garmentName(itemDto.getGarmentName())
                        .quantity(itemDto.getQuantity())
                        .pricePerItem(itemDto.getPricePerItem())
                        .category(itemDto.getCategory() != null ? itemDto.getCategory() : GarmentCategory.OTHERS)
                        .serviceType(itemDto.getServiceType() != null ? itemDto.getServiceType() : ServiceType.WASH_FOLD)
                        .laundryOrder(order)
                        .build())
                .collect(Collectors.toList());

        order.setItems(items);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(String orderId, OrderStatus status) {
        LaundryOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order " + orderId + " not found"));
        order.setStatus(status);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updatePaymentStatus(String orderId, PaymentStatus status) {
        LaundryOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order " + orderId + " not found"));
        order.setPaymentStatus(status);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        LaundryOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order " + orderId + " not found"));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(String query, OrderStatus status) {
        List<LaundryOrder> orders;
        if (status != null) {
            orders = orderRepository.findByStatusWithItems(status);
        } else if (query != null && !query.isEmpty()) {
            orders = orderRepository.searchWithItems(query);
        } else {
            orders = orderRepository.findAllWithItems();
        }
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        List<LaundryOrder> allOrders = orderRepository.findAllWithItems();
        long totalOrders = allOrders.size();
        long priorityCount = orderRepository.countByIsPriority(true);
        double totalRevenue = allOrders.stream().mapToDouble(LaundryOrder::getFinalBill).sum();

        Map<OrderStatus, Long> statusCounts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status, allOrders.stream().filter(o -> o.getStatus() == status).count());
        }
Map<GarmentCategory, Double> revenueByCategory = new EnumMap<>(GarmentCategory.class);
Map<ServiceType, Double> revenueByService = new EnumMap<>(ServiceType.class);

for (LaundryOrder order : allOrders) {
    for (OrderItem item : order.getItems()) {
        double itemRevenue = item.getPricePerItem() * item.getQuantity();
        revenueByCategory.merge(item.getCategory(), itemRevenue, Double::sum);
        revenueByService.merge(item.getServiceType(), itemRevenue, Double::sum);
    }
}

Map<String, CustomerStats> customerMap = new HashMap<>();
        for (LaundryOrder order : allOrders) {
            String key = order.getPhoneNumber();
            CustomerStats stats = customerMap.getOrDefault(key, CustomerStats.builder()
                    .customerName(order.getCustomerName())
                    .phoneNumber(order.getPhoneNumber())
                    .totalOrders(0L)
                    .totalSpent(0.0)
                    .build());
            stats.setTotalOrders(stats.getTotalOrders() + 1);
            stats.setTotalSpent(stats.getTotalSpent() + order.getFinalBill());
            customerMap.put(key, stats);
        }

        List<CustomerStats> topCustomers = customerMap.values().stream()
                .sorted(Comparator.comparingDouble(CustomerStats::getTotalSpent).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalOrders(totalOrders)
                .totalPriorityOrders(priorityCount)
                .totalRevenue(totalRevenue)
                .ordersByStatus(statusCounts)
                .revenueByCategory(revenueByCategory)
                .revenueByService(revenueByService)
                .topCustomers(topCustomers)
                .build();
    }

    private OrderResponse mapToResponse(LaundryOrder order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(order.getCustomerName())
                .phoneNumber(order.getPhoneNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .isPriority(order.getIsPriority())
                .totalBill(order.getTotalBill())
                .discountAmount(order.getDiscountAmount())
                .finalBill(order.getFinalBill())
                .createdAt(order.getCreatedAt().format(formatter))
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate().format(formatter))
                .items(order.getItems().stream()
                        .map(item -> OrderItemDTO.builder()
                                .garmentName(item.getGarmentName())
                                .quantity(item.getQuantity())
                                .pricePerItem(item.getPricePerItem())
                                .category(item.getCategory())
                                .serviceType(item.getServiceType())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
