package com.ff.commandes_service;

import com.ff.commandes_service.dto.OrderStatusRequest;
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

    // Définir un ID admin pour les tests si nécessaire
    private static final Long TEST_ADMIN_ID = 1L;

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
                .adminId(TEST_ADMIN_ID) // Important pour la logique isOrderAdmin si elle est déplacée au service
                .build();

        // Créer un OrderStatusRequest pour simuler l'entrée du contrôleur
        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.setOrderStatus(newStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        // MODIFIÉ: Utilisation de la nouvelle signature updateOrderStatus(id, adminId, OrderStatusRequest)
        Optional<Orders> updatedOrderOpt = adminService.updateOrderStatus(orderId, TEST_ADMIN_ID, orderStatusRequest);

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
                .adminId(TEST_ADMIN_ID)
                .build();

        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.setOrderStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(cancelledOrder));

        // Act & Assert
        // MODIFIÉ: Utilisation de la nouvelle signature updateOrderStatus(id, adminId, OrderStatusRequest)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, TEST_ADMIN_ID, orderStatusRequest));

        assertEquals("Order cannot be updated to OrderStatusRequest(orderStatus=SHIPPED) after it has been CANCELLED", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_shouldThrowException_whenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        // Créer un OrderStatusRequest pour simuler l'entrée du contrôleur
        OrderStatus newStatus = OrderStatus.VALIDATED;
        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.setOrderStatus(newStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        // MODIFIÉ: Utilisation de la nouvelle signature updateOrderStatus(id, adminId, OrderStatusRequest)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, TEST_ADMIN_ID, orderStatusRequest));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    // NOUVEAU TEST: Vérifier que l'admin est bien celui de la commande lors de l'update
    @Test
    void updateOrderStatus_shouldThrowException_whenAdminNotAuthorized() {
        Long orderId = 1L;
        Long unauthorizedAdminId = 999L; // Un ID admin différent
        OrderStatus newStatus = OrderStatus.SHIPPED;

        Orders existingOrder = Orders.builder()
                .id(orderId)
                .productId(101L)
                .userId(201L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50.00))
                .orderStatus(OrderStatus.PENDING)
                .orderDate(java.time.LocalDateTime.now())
                .adminId(TEST_ADMIN_ID) // La commande appartient à TEST_ADMIN_ID
                .build();

        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.setOrderStatus(newStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, unauthorizedAdminId, orderStatusRequest));

        // Le message d'erreur dépend de l'implémentation de isOrderAdmin dans votre AdminService.
        // Si isOrderAdmin est une méthode privée appelée à l'intérieur de updateOrderStatus,
        // et qu'elle lance cette exception, ce test est valide.
        assertEquals("Order with id: 1 does not belong to admin with id: 999", exception.getMessage());
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

    // MODIFIÉ: Renommage et adaptation pour getOrderByIdByAdmin
    @Test
    void getOrderByIdByAdmin_shouldReturnOrder_whenExistsAndAdminAuthorized() {
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
                .adminId(TEST_ADMIN_ID) // Assurez-vous que la commande appartient bien à TEST_ADMIN_ID
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        // MODIFIÉ: Appel à getOrderByIdByAdmin avec l'ID de l'admin
        Optional<Orders> result = adminService.getOrderByIdByAdmin(orderId, TEST_ADMIN_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository).findById(orderId);
    }

    // NOUVEAU TEST: Gérer le cas où l'admin n'est pas autorisé pour getOrderByIdByAdmin
    @Test
    void getOrderByIdByAdmin_shouldThrowException_whenAdminNotAuthorized() {
        Long orderId = 1L;
        Long unauthorizedAdminId = 999L;

        Orders order = Orders.builder()
                .id(orderId)
                .adminId(TEST_ADMIN_ID) // La commande appartient à TEST_ADMIN_ID
                .build();

        // orderRepository.findById(orderId) trouve la commande
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.getOrderByIdByAdmin(orderId, unauthorizedAdminId));

        // MODIFIÉ: Le message attendu doit correspondre EXACTEMENT à ce que votre service lance
        assertEquals("Order not found with id: " + orderId + " for admin with id: " + unauthorizedAdminId, exception.getMessage());
        verify(orderRepository).findById(orderId);
    }

    // MODIFIÉ: Renommage et adaptation pour getOrderByIdByAdmin
    @Test
    void getOrderByIdByAdmin_shouldReturnEmpty_whenNotExists() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.getOrderByIdByAdmin(orderId, TEST_ADMIN_ID));
        assertEquals("Order not found with id: " + orderId + " for admin with id: " + TEST_ADMIN_ID, exception.getMessage());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getAllOrdersByAdmin_shouldReturnListOfOrders() { // Renommage du test pour plus de clarté
        // Arrange
        Orders order1 = Orders.builder()
                .id(1L)
                .productId(101L)
                .userId(201L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50.00))
                .orderStatus(OrderStatus.PENDING)
                .orderDate(java.time.LocalDateTime.now())
                .adminId(TEST_ADMIN_ID) // Important pour le filtrage par admin
                .build();

        Orders order2 = Orders.builder()
                .id(2L)
                .productId(102L)
                .userId(202L)
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(25.00))
                .orderStatus(OrderStatus.VALIDATED)
                .orderDate(java.time.LocalDateTime.now())
                .adminId(TEST_ADMIN_ID) // Important pour le filtrage par admin
                .build();

        when(orderRepository.findAllProductByAdminId(TEST_ADMIN_ID)).thenReturn(List.of(order1, order2));

        // Act
        List<Orders> result = adminService.getAllOrdersByAdmin(TEST_ADMIN_ID);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(orderRepository).findAllProductByAdminId(TEST_ADMIN_ID);
    }

    @Test
    void getAllOrdersByAdmin_shouldReturnEmptyList_whenNoOrders() { // Renommage du test
        // Arrange
        when(orderRepository.findAllProductByAdminId(TEST_ADMIN_ID)).thenReturn(List.of());

        // Act
        List<Orders> result = adminService.getAllOrdersByAdmin(TEST_ADMIN_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findAllProductByAdminId(TEST_ADMIN_ID);
    }

    // Le test countOrders_shouldReturnCorrectCount est un doublon, il a été laissé pour référence mais peut être supprimé.
    @Test
    void countOrders_shouldReturnCorrectCount_Dupe() { // Renommage pour indiquer que c'est un doublon
        // Arrange
        long expectedCount = 10L;

        when(orderRepository.count()).thenReturn(expectedCount);

        // Act
        long actualCount = adminService.countOrders();

        // Assert
        assertEquals(expectedCount, actualCount);
        verify(orderRepository).count();
    }

    // MODIFIÉ: updateOrderStatusByString est remplacé par le test updateOrderStatus général
    // et il est important de noter que le contrôleur passe un OrderStatusRequest
    // Si votre service a toujours une méthode updateOrderStatus(Long id, OrderStatus status),
    // alors le test précédent était correct pour cette signature.
    // MAIS, si le contrôleur appelle updateOrderStatus(id, adminId, OrderStatusRequest),
    // alors le service doit avoir cette signature, et les tests doivent la refléter.
    // Je vais adapter ce test pour correspondre à la signature plus récente avec OrderStatusRequest et adminId.
    @Test
    void updateOrderStatus_shouldUpdateOrderSuccessfully_fromRequest() {
        // Arrange
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
                .adminId(TEST_ADMIN_ID) // Assurez-vous que l'ordre appartient à TEST_ADMIN_ID
                .build();

        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.setOrderStatus(newStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        // MODIFIÉ: Appel à la signature correcte updateOrderStatus(id, adminId, OrderStatusRequest)
        Optional<Orders> updatedOrderOpt = adminService.updateOrderStatus(orderId, TEST_ADMIN_ID, orderStatusRequest);

        // Assert
        assertTrue(updatedOrderOpt.isPresent());
        Orders updatedOrder = updatedOrderOpt.get();

        assertEquals(newStatus, updatedOrder.getOrderStatus());
        assertNotNull(updatedOrder.getShippedDate());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(updatedOrder);
    }


    // Ce test updateOrderStatusByString_shouldThrowException_whenOrderIsCancelled est un doublon
    // du premier updateOrderStatus_shouldThrowException_whenOrderIsCancelled,
    // mais il utilise une String pour le statut.
    // Si la méthode updateOrderStatus(Long id, OrderStatus status) existe toujours, gardez-le.
    // Sinon, il doit être adapté pour updateOrderStatus(Long id, Long adminId, OrderStatusRequest).
    // Je vais le laisser tel quel pour l'instant en supposant que la surcharge existe.
    // Cependant, il est plus probable que le service utilise le DTO pour l'update.
    @Test
    void updateOrderStatusByString_shouldThrowException_whenOrderIsCancelled() {
        // Arrange
        Long orderId = 2L;
        OrderStatus newStatus = OrderStatus.CANCELLED;

        Orders cancelledOrder = Orders.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CANCELLED)
                .adminId(TEST_ADMIN_ID)
                .build();

        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.setOrderStatus(newStatus);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(cancelledOrder));

        // Act & Assert
        // Si cette signature existe encore dans AdminService, ce test est valide.
        // Sinon, il faut l'adapter ou le supprimer.
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
              adminService.updateOrderStatus(orderId, TEST_ADMIN_ID, orderStatusRequest));
                //adminService.updateOrderStatus(orderId, OrderStatus.valueOf(status.toUpperCase())));

        assertEquals("Order cannot be updated to OrderStatusRequest(orderStatus=CANCELLED) after it has been CANCELLED", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    // Idem pour ce test, il est en doublon et utilise l'ancienne signature
    @Test
    void updateOrderStatusByString_shouldThrowException_whenOrderNotFound() {
        // Arrange
        Long orderId = 999L;
        OrderStatus newStatus = OrderStatus.VALIDATED;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.setOrderStatus(newStatus);

        // Act & Assert
        // Si cette signature existe encore dans AdminService, ce test est valide.
        // Sinon, il faut l'adapter ou le supprimer.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                adminService.updateOrderStatus(orderId, TEST_ADMIN_ID, orderStatusRequest));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }
}
