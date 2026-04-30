package com.laundry.app.controller;

import com.laundry.app.dto.*;
import com.laundry.app.model.OrderStatus;
import com.laundry.app.service.LaundryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class LaundryController {

    private final LaundryService laundryService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@jakarta.validation.Valid @RequestBody OrderRequest request) {
        log.info("Received request to create order for customer: {}", request.getCustomerName());
        return ResponseEntity.ok(laundryService.createOrder(request));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        log.info("Updating status for order {} to {}", orderId, status);
        return ResponseEntity.ok(laundryService.updateStatus(orderId, status));
    }

    @PutMapping("/orders/{orderId}/payment")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable String orderId,
            @RequestParam com.laundry.app.model.PaymentStatus status) {
        log.info("Updating payment status for order {} to {}", orderId, status);
        return ResponseEntity.ok(laundryService.updatePaymentStatus(orderId, status));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(laundryService.getOrderById(orderId));
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        log.info("Deleting order: {}", orderId);
        laundryService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(laundryService.getAllOrders(query, status));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(laundryService.getDashboardData());
    }

    @GetMapping("/garments/library")
    public ResponseEntity<List<GarmentSettingDTO>> getGarmentLibrary() {
        return ResponseEntity.ok(laundryService.getGarmentLibrary());
    }

    @PostMapping("/garments/library")
    public ResponseEntity<GarmentSettingDTO> saveGarmentToLibrary(@jakarta.validation.Valid @RequestBody GarmentSettingDTO dto) {
        return ResponseEntity.ok(laundryService.saveGarmentSetting(dto));
    }

    @DeleteMapping("/garments/library/{id}")
    public ResponseEntity<Void> deleteGarmentFromLibrary(@PathVariable Long id) {
        laundryService.deleteGarmentSetting(id);
        return ResponseEntity.ok().build();
    }
}
