package com.ff.commandes_service;

import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.repository.OrderRepository;
import com.ff.commandes_service.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AdminServiceTest {
    private OrderRepository orderRepository;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        adminService = new AdminService(orderRepository);
    }

    @Test
    void updateOrderStatus_shouldUpdateOrderSuccessfully(){
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.SHIPPED;

        Orders existingOrder = Orders.builder()
                .id(orderId)
                .productId(101L)
                .userId(201L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50.00))
                .orderStatus(OrderStatus.PENDING)
                .orderDate(java.time.LocalDateTime.now())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder)); when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Orders> updatedOrderOpt = adminService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertTrue(updatedOrderOpt.isPresent());
        Orders updatedOrder = updatedOrderOpt.get();

        assertEquals(OrderStatus.SHIPPED, updatedOrder.getOrderStatus());
        assertNotNull(updatedOrder.getShippedDate());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(updatedOrder);
    }
    @Test
    void updateOrderStatus_shouldThrowException_whenOrderIsCancelled() {
        // Arrange
        Long orderId = 2L;
        Orders cancelledOrder = Orders.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(cancelledOrder));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, OrderStatus.SHIPPED));

        assertEquals("Order cannot be updated to SHIPPED after it has been CANCELLED", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_shouldThrowException_whenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, OrderStatus.VALIDATED));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }
    @Test
    void countOrdersByStatus_shouldReturnCorrectCount() {
        // Arrange
        OrderStatus status = OrderStatus.PENDING;
        long expectedCount = 5L;

        when(orderRepository.countByOrderStatus(status)).thenReturn(expectedCount);

        // Act
        long actualCount = adminService.countOrdersByStatus(status);

        // Assert
        assertEquals(expectedCount, actualCount);
        verify(orderRepository).countByOrderStatus(status);
    }
    @Test
    void countOrdersByStatus_shouldReturnCorrectCountByString() {
        // Arrange
        String status = "PENDING";
        long expectedCount = 5L;

        when(orderRepository.countByOrderStatus(OrderStatus.valueOf(status.toUpperCase()))).thenReturn(expectedCount);

        // Act
        long actualCount = adminService.countOrdersByStatus(status);

        // Assert
        assertEquals(expectedCount, actualCount);
        verify(orderRepository).countByOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
    }
    @Test
    void countAllOrders_shouldReturnCorrectCount() {
        // Arrange
        long expectedCount = 10L;

        when(orderRepository.count()).thenReturn(expectedCount);

        // Act
        long actualCount = adminService.countAllOrders();

        // Assert
        assertEquals(expectedCount, actualCount);
        verify(orderRepository).count();
    }
    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        // Arrange
        Long orderId = 1L;
        Orders order = Orders.builder()
                .id(orderId)
                .productId(101L)
                .userId(201L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50.00))
                .orderStatus(OrderStatus.PENDING)
                .orderDate(java.time.LocalDateTime.now())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Optional<Orders> result = adminService.getOrderById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository).findById(orderId);
    }
    @Test
    void getOrderById_shouldReturnEmpty_whenNotExists() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<Orders> result = adminService.getOrderById(orderId);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository).findById(orderId);
    }
    @Test
    void getAllOrders_shouldReturnListOfOrders() {
        // Arrange
        Orders order1 = Orders.builder()
                .id(1L)
                .productId(101L)
                .userId(201L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50.00))
                .orderStatus(OrderStatus.PENDING)
                .orderDate(java.time.LocalDateTime.now())
                .build();

        Orders order2 = Orders.builder()
                .id(2L)
                .productId(102L)
                .userId(202L)
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(25.00))
                .orderStatus(OrderStatus.VALIDATED)
                .orderDate(java.time.LocalDateTime.now())
                .build();

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        // Act
        Optional<List<Orders>> result = adminService.getAllOrders();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(orderRepository).findAll();
    }
    @Test
    void getAllOrders_shouldReturnEmpty_whenNoOrders() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(List.of());

        // Act
        Optional<List<Orders>> result = adminService.getAllOrders();

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
        verify(orderRepository).findAll();
    }
    @Test
    void countOrders_shouldReturnCorrectCount() {
        // Arrange
        long expectedCount = 10L;

        when(orderRepository.count()).thenReturn(expectedCount);

        // Act
        long actualCount = adminService.countOrders();

        // Assert
        assertEquals(expectedCount, actualCount);
        verify(orderRepository).count();
    }
    @Test
    void updateOrderStatusByString_shouldUpdateOrderSuccessfully() {
        // Arrange
        Long orderId = 1L;
        String status = "SHIPPED";

        Orders existingOrder = Orders.builder()
                .id(orderId)
                .productId(101L)
                .userId(201L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50.00))
                .orderStatus(OrderStatus.PENDING)
                .orderDate(java.time.LocalDateTime.now())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Orders> updatedOrderOpt = adminService.updateOrderStatus(orderId, OrderStatus.valueOf(status.toUpperCase()));

        // Assert
        assertTrue(updatedOrderOpt.isPresent());
        Orders updatedOrder = updatedOrderOpt.get();

        assertEquals(OrderStatus.SHIPPED, updatedOrder.getOrderStatus());
        assertNotNull(updatedOrder.getShippedDate());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(updatedOrder);
    }
    @Test
    void updateOrderStatusByString_shouldThrowException_whenOrderIsCancelled() {
        // Arrange
        Long orderId = 2L;
        String status = "SHIPPED";

        Orders cancelledOrder = Orders.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(cancelledOrder));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, OrderStatus.valueOf(status.toUpperCase())));

        assertEquals("Order cannot be updated to SHIPPED after it has been CANCELLED", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }
    @Test
    void updateOrderStatusByString_shouldThrowException_whenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        String status = "VALIDATED";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, OrderStatus.valueOf(status.toUpperCase())));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }
    @Test
    void getOrderById_shouldReturnOrder_whenExistsWithStatus() {
        // Arrange
        Long orderId = 1L;
        Orders order = Orders.builder()
                .id(orderId)
                .productId(101L)
                .userId(201L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50.00))
                .orderStatus(OrderStatus.PENDING)
                .orderDate(java.time.LocalDateTime.now())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Optional<Orders> result = adminService.getOrderById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository).findById(orderId);
    }
}
