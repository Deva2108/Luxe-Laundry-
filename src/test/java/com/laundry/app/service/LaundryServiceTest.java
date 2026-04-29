package com.laundry.app.service;

import com.laundry.app.dto.OrderItemDTO;
import com.laundry.app.dto.OrderRequest;
import com.laundry.app.dto.OrderResponse;
import com.laundry.app.model.*;
import com.laundry.app.repository.GarmentSettingRepository;
import com.laundry.app.repository.OrderRepository;
import com.laundry.app.repository.ShopProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaundryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GarmentSettingRepository garmentSettingRepository;

    @Mock
    private ShopProfileRepository profileRepository;

    @InjectMocks
    private LaundryService laundryService;

    private ShopProfile mockProfile;

    @BeforeEach
    void setUp() {
        mockProfile = ShopProfile.builder()
                .id(1L)
                .shopName("LuxeLaundry")
                .taxPercentage(10.0)
                .build();
    }

    @Test
    void createOrder_ShouldCalculateCorrectTotals() {
        // Arrange
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        
        OrderRequest request = new OrderRequest();
        request.setCustomerName("John Doe");
        request.setPhoneNumber("1234567890");
        request.setIsPriority(true); // 20% surcharge
        request.setDiscountPercentage(10.0);
        
        OrderItemDTO item = new OrderItemDTO();
        item.setGarmentName("Shirt");
        item.setQuantity(2);
        item.setPricePerItem(100.0);
        request.setItems(List.of(item));

        when(orderRepository.save(any(LaundryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = laundryService.createOrder(request);

        // Assert
        // Base bill = 2 * 100 = 200
        // Priority surcharge = 200 * 0.2 = 40
        // Subtotal = 200 + 40 = 240
        // Discount (10%) = 240 * 0.1 = 24
        // Taxable = 240 - 24 = 216
        // Tax (10%) = 216 * 0.1 = 21.6
        // Final bill = 216 + 21.6 = 237.6

        assertEquals(240.0, response.getTotalBill());
        assertEquals(24.0, response.getDiscountAmount());
        assertEquals(237.6, response.getFinalBill());
        assertTrue(response.getOrderId().startsWith("ORD-"));
        assertEquals(OrderStatus.RECEIVED, response.getStatus());
        verify(orderRepository, times(1)).save(any(LaundryOrder.class));
    }

    @Test
    void createOrder_ShouldClampDiscount() {
        // Arrange
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        
        OrderRequest request = new OrderRequest();
        request.setDiscountPercentage(150.0); // Should be clamped to 100
        request.setItems(List.of(new OrderItemDTO("Shirt", 1, 100.0, GarmentCategory.TOPS, ServiceType.WASH_FOLD)));

        when(orderRepository.save(any(LaundryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = laundryService.createOrder(request);

        // Assert
        assertEquals(0.0, response.getFinalBill()); // 100% discount
    }

    @Test
    void updateStatus_ShouldChangeStatus() {
        // Arrange
        String orderId = "ORD-123";
        LaundryOrder order = LaundryOrder.builder().orderId(orderId).status(OrderStatus.RECEIVED).build();
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(LaundryOrder.class))).thenReturn(order);

        // Act
        OrderResponse response = laundryService.updateStatus(orderId, OrderStatus.READY);

        // Assert
        assertEquals(OrderStatus.READY, response.getStatus());
        verify(orderRepository).save(order);
    }
}
