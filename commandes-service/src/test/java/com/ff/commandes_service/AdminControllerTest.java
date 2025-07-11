package com.ff.commandes_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ff.commandes_service.controller.AdminController;
import com.ff.commandes_service.dto.CountOrdersResponse;
import com.ff.commandes_service.dto.OrderStatusRequest;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.handler.GlobalExceptionHandler;
import com.ff.commandes_service.security.JwtService;
import com.ff.commandes_service.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @Mock
    private AdminService adminService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Long TEST_ADMIN_ID = 123L;
    private static final String MOCK_AUTH_HEADER = "Bearer some.jwt.token";
    private static final String MOCK_TOKEN = "some.jwt.token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AdminController adminController = new AdminController(adminService, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler()) // <-- Intègre le GlobalExceptionHandler
                .build();
    }

    @Test
    void getAllOrdersByAdmin_shouldReturnAllOrders() throws Exception {
        Orders order1 = new Orders();
        order1.setId(1L);
        order1.setAdminId(TEST_ADMIN_ID);
        Orders order2 = new Orders();
        order2.setId(2L);
        order2.setAdminId(TEST_ADMIN_ID);
        List<Orders> ordersList = Arrays.asList(order1, order2);

        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(TEST_ADMIN_ID);
        when(adminService.getAllOrdersByAdmin(TEST_ADMIN_ID)).thenReturn(ordersList);

        mockMvc.perform(get("/api/order/admin")
                        .header("Authorization", MOCK_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllOrdersByAdmin_shouldReturnBadRequest_ifNoOrdersFound() throws Exception {
        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(TEST_ADMIN_ID);
        when(adminService.getAllOrdersByAdmin(TEST_ADMIN_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/order/admin")
                        .header("Authorization", MOCK_AUTH_HEADER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No orders found for admin with id: " + TEST_ADMIN_ID))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void shouldReturnOrderCount() throws Exception {
        // Create a mock response object
        CountOrdersResponse mockRes = new CountOrdersResponse();
        mockRes.setTotalOrders(10L);
        mockRes.setPendingOrders(3L);
        mockRes.setCompletedOrders(5L);
        mockRes.setCancelledOrders(2L);

        // Mock the jwtService to return the admin ID
        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(TEST_ADMIN_ID);
        // Mock the adminService to return the mock response
        when(adminService.countOrders(TEST_ADMIN_ID)).thenReturn(mockRes);
        mockMvc.perform(get("/api/order/admin/count")
                .header("Authorization", MOCK_AUTH_HEADER))
                        .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalOrders").value(mockRes.getTotalOrders()))
                                        .andExpect(jsonPath("$.pendingOrders").value(mockRes.getPendingOrders()))
                                                .andExpect(jsonPath("$.completedOrders").value(mockRes.getCompletedOrders()))
                                                        .andExpect(jsonPath("$.cancelledOrders").value(mockRes.getCancelledOrders()));
    }

    @Test
    void shouldReturnOrderCountByStatus() throws Exception {
        when(adminService.countOrdersByStatus("SHIPPED")).thenReturn(3L);

        mockMvc.perform(get("/api/order/admin/count/status")
                        .param("status", "SHIPPED")) // <-- N'oubliez pas .param pour les @RequestParam
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3L));
    }

    @Test
    void getOrderByIdByAdmin_shouldReturnOrderById() throws Exception {
        Long orderId = 1L;
        Orders order = new Orders();
        order.setId(orderId);
        order.setAdminId(TEST_ADMIN_ID);

        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(TEST_ADMIN_ID);
        when(adminService.getOrderByIdByAdmin(orderId, TEST_ADMIN_ID)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/order/admin/" + orderId)
                        .header("Authorization", MOCK_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void getOrderByIdByAdmin_shouldReturnBadRequest_ifOrderNotFoundOrNotAuthorized() throws Exception {
        Long orderId = 999L;
        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(TEST_ADMIN_ID);
        // Simule le service lançant l'exception que le contrôleur est censé gérer
        when(adminService.getOrderByIdByAdmin(orderId, TEST_ADMIN_ID))
                .thenThrow(new IllegalArgumentException("Order not found with id: " + orderId + " for admin with id: " + TEST_ADMIN_ID));

        mockMvc.perform(get("/api/order/admin/" + orderId)
                        .header("Authorization", MOCK_AUTH_HEADER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order not found with id: " + orderId + " for admin with id: " + TEST_ADMIN_ID))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void updateOrderStatus_shouldUpdateOrderSuccessfully() throws Exception {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.SHIPPED;
        Orders updatedOrder = new Orders();
        updatedOrder.setId(orderId);
        updatedOrder.setAdminId(TEST_ADMIN_ID);
        updatedOrder.setOrderStatus(newStatus);

        OrderStatusRequest statusRequest = new OrderStatusRequest();
        statusRequest.setOrderStatus(OrderStatus.valueOf(newStatus.toString()));

        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(TEST_ADMIN_ID);
        // Le service retourne un Optional de la commande mise à jour (non vide)
        when(adminService.updateOrderStatus(eq(orderId), eq(TEST_ADMIN_ID), any(OrderStatusRequest.class)))
                .thenReturn(Optional.of(updatedOrder));

        // Le contrôleur est maintenant @PutMapping, le test utilise PUT
        mockMvc.perform(put("/api/order/admin/" + orderId + "/orderStatus")
                        .header("Authorization", MOCK_AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.orderStatus").value(newStatus.toString()));
    }

    @Test
    void updateOrderStatus_shouldReturnBadRequest_ifUpdateFails() throws Exception {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.SHIPPED;
        OrderStatusRequest statusRequest = new OrderStatusRequest();
        statusRequest.setOrderStatus(OrderStatus.valueOf(newStatus.toString()));

        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(TEST_ADMIN_ID);
        // Simule le service lançant l'exception que le contrôleur est censé gérer
        String errorMessage = "Order cannot be updated to " + newStatus + " after it has been CANCELLED"; // Example error message
        when(adminService.updateOrderStatus(eq(orderId), eq(TEST_ADMIN_ID), any(OrderStatusRequest.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(put("/api/order/admin/" + orderId + "/orderStatus")
                        .header("Authorization", MOCK_AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage)) // Expect the specific error message from the service
                .andExpect(jsonPath("$.error").value("Bad Request"));;
    }

    @Test
    void getAdminIdFromToken_shouldReturnBadRequest_ifMissingAuthHeader() throws Exception {
        mockMvc.perform(get("/api/order/admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Authorization header is missing or request is null"))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void getAdminIdFromToken_shouldReturnBadRequest_ifInvalidAuthFormat() throws Exception {
        mockMvc.perform(get("/api/order/admin")
                        .header("Authorization", "InvalidToken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Authorization header format"))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void getAdminIdFromToken_shouldReturnBadRequest_ifAdminIdNotFoundInToken() throws Exception {
        when(jwtService.extractUserId(MOCK_TOKEN)).thenReturn(null);

        mockMvc.perform(get("/api/order/admin")
                        .header("Authorization", MOCK_AUTH_HEADER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid token or admin ID not found in token"))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}