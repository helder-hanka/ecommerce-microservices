package com.ff.commandes_service;

import com.ff.commandes_service.controller.AdminController;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AdminController adminController = new AdminController(adminService);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }
    @Test
    void shouldReturnAllOrders() throws Exception {
        Orders order1 = new Orders(); order1.setId(1L);
        Orders order2 = new Orders(); order2.setId(2L);
        List<Orders> ordersList = Arrays.asList(order1, order2);

        when(adminService.getAllOrders()).thenReturn(Optional.of(ordersList));

        mockMvc.perform(get("/api/order/admin"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        Orders order = new Orders(); order.setId(1L);

        when(adminService.getOrderById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/order/admin/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOrderCount() throws Exception {
        when(adminService.countOrders()).thenReturn(5L);

        mockMvc.perform(get("/api/order/admin/count"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnOrderCountByStatus() throws Exception {
        when(adminService.countOrdersByStatus("SHIPPED")).thenReturn(3L);

        mockMvc.perform(get("/api/order/admin/count/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnAllOrderCount() throws Exception {
        when(adminService.countAllOrders()).thenReturn(10L);

        mockMvc.perform(get("/api/order/admin/count/all"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        Orders order = new Orders(); order.setId(1L); order.setOrderStatus(OrderStatus.SHIPPED);

        when(adminService.updateOrderStatus(1L, OrderStatus.SHIPPED)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/order/admin/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateOrderStatusByString() throws Exception {
        Orders order = new Orders(); order.setId(1L); order.setOrderStatus(OrderStatus.SHIPPED);

        when(adminService.updateOrderStatus(1L, OrderStatus.SHIPPED)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/order/admin/1/status/SHIPPED"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnOrdersByStatus() throws Exception {
        Orders order1 = new Orders(); order1.setId(1L); order1.setOrderStatus(OrderStatus.SHIPPED);
        Orders order2 = new Orders(); order2.setId(2L); order2.setOrderStatus(OrderStatus.SHIPPED);
        List<Orders> ordersList = Arrays.asList(order1, order2);

        when(adminService.getAllOrders()).thenReturn(Optional.of(ordersList));

        mockMvc.perform(get("/api/order/admin")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnOrdersByStatusWithEmptyList() throws Exception {
        when(adminService.getAllOrders()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/order/admin")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReturnOrdersByStatusWithNoOrders() throws Exception {
        when(adminService.getAllOrders()).thenReturn(Optional.of(List.of()));

        mockMvc.perform(get("/api/order/admin")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk());
    }
}
