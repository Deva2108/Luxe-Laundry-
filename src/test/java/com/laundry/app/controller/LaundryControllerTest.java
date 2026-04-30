package com.laundry.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laundry.app.dto.OrderItemDTO;
import com.laundry.app.dto.OrderRequest;
import com.laundry.app.dto.OrderResponse;
import com.laundry.app.model.OrderStatus;
import com.laundry.app.service.LaundryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LaundryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LaundryService laundryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin")
    void createOrder_ShouldReturnCreated() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Alice");
        request.setPhoneNumber("0987654321");
        request.setItems(List.of(new OrderItemDTO("Pants", 1, 50.0, null, null)));

        OrderResponse response = OrderResponse.builder()
                .orderId("ORD-ALICE")
                .customerName("Alice")
                .finalBill(50.0)
                .status(OrderStatus.RECEIVED)
                .build();

        when(laundryService.createOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-ALICE"))
                .andExpect(jsonPath("$.customerName").value("Alice"));
    }

    @Test
    void createOrder_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin")
    void deleteOrder_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/orders/ORD-123"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin")
    void getDashboard_ShouldReturnDashboardData() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk());
    }
}
