package com.ff.commandes_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ff.commandes_service.dto.*;
import com.ff.commandes_service.entity.OrderItem;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.repository.OrderItemRepository;
import com.ff.commandes_service.repository.OrderRepository;
import com.ff.commandes_service.service.kafka.OrderKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final OrderRepository orderRepository;
    private final OrderKafkaProducer kafkaOrderProducer;
    private final ProductServiceClient productServiceClient;
    private final OrderItemRepository orderItemRepository;

        @Transactional
        public OrderResponse createOrder(Long userId, List<OrderItemDto> ordersRequest) throws JsonProcessingException {

            BigDecimal totalPrice = BigDecimal.ZERO;
            List<OrderItem> createdOrderItems = new ArrayList<>();

            // --- ÉTAPE 1 : VÉRIFICATION DU STOCK POUR TOUS LES ARTICLES ---
            for (OrderItemDto productsItems : ordersRequest) {
                if (productsItems.getProductId() == null || productsItems.getProductId() <= 0) {
                    throw new IllegalArgumentException("Product ID must be greater than zero");
                }
                if (productsItems.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than zero");
                }
                Integer availableStock = productServiceClient.checkProductStock(productsItems.getProductId());

                if (availableStock == null || availableStock < productsItems.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for product ID: " + productsItems.getProductId());
                }

                // Calculer le prix total après validation du stock
                totalPrice = totalPrice.add(productsItems.getPrice().multiply(BigDecimal.valueOf(productsItems.getQuantity())));
            }


            // --- ÉTAPE 2 : CRÉATION ET SAUVEGARDE DE L'ENTITÉ Orders PRINCIPALE ---
            // C'EST LE MOMENT DE SAUVEGARDER L'ORDER PRINCIPAL
            Orders orderToSave = Orders.builder()
                    .userId(userId)
                    .totalPrice(totalPrice)
                    .orderStatus(OrderStatus.PENDING) // Statut initial de la commande
                    .orderDate(LocalDateTime.now())
                    .items(createdOrderItems) // Associez les OrderItem qui ont déjà un ID après sauvegarde
                    .build();

            Orders savedOrder = orderRepository.save(orderToSave);

            // --- ÉTAPE 3 : CRÉATION ET SAUVEGARDE DES ARTICLES DE COMMANDE (OrderItem) ---
            // C'est mieux de sauvegarder les OrderItem d'abord si votre relation est @OneToMany et la liste n'est pas @Cascade
            for (OrderItemDto productsItems : ordersRequest) {
                OrderItem newOrderItem = OrderItem.builder()
                        .productId(productsItems.getProductId())
                        .adminId(productsItems.getAdminId())
                        .order(orderToSave)
                        .name(productsItems.getName())
                        .quantity(productsItems.getQuantity())
                        .price(productsItems.getPrice())
                        .build();
                // Sauvegarder chaque OrderItem individuellement
                createdOrderItems.add(orderItemRepository.save(newOrderItem));
            }
            // --- ÉTAPE 4 : DÉCRÉMENTATION DU STOCK DES PRODUITS ---
            try {
                for (OrderItemDto productsItems : ordersRequest) {
                    productServiceClient.decrementProductStock(productsItems.getProductId(), productsItems.getQuantity());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to decrement product stock. Please try again later.");
            }

            // --- ÉTAPE 5 : PRÉPARATION DES DONNÉES POUR KAFKA ET LA RÉPONSE ---
            List<OrderCreatedEvent.OrderItemEvent> orderItemEvents = createdOrderItems.stream()
                    .map(item -> new OrderCreatedEvent.OrderItemEvent(
                            item.getId(),
                            item.getProductId(),
                            item.getAdminId(),
                            item.getName(),
                            item.getQuantity(),
                            item.getPrice()))
                    .toList();

            List<OrderItemResponse> orderItemRes = createdOrderItems.stream()
                    .map(item -> new OrderItemResponse(
                            item.getId(),
                            item.getAdminId(),
                            item.getProductId(),
                            item.getName(),
                            item.getQuantity(),
                            item.getPrice()))
                    .toList();

            // --- ÉTAPE 6 : ENVOI DE L'ÉVÉNEMENT À KAFKA TOPIC ---
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                    savedOrder.getId(),
                    savedOrder.getUserId(),
                    savedOrder.getOrderStatus().name(),
                    savedOrder.getTotalPrice(),
                    savedOrder.getOrderDate(),
                    orderItemEvents
            );
            kafkaOrderProducer.sendOrderCreatedEvent(orderCreatedEvent);

            // --- ÉTAPE 7 : RETOUR DE LA RÉPONSE ---
            return new OrderResponse(
                    savedOrder.getId(),
                    savedOrder.getUserId(),
                    savedOrder.getOrderStatus().name(),
                    savedOrder.getTotalPrice(),
                    savedOrder.getOrderDate(),
                    orderItemRes
            );
        }

    public OrderResponse getOrderById(Long id, Long userId) {
        // find order by id and userId
        Orders order = orderRepository.findById(id)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id + " for user with id: " + userId));

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderStatus().name(),
                order.getTotalPrice(),
                order.getOrderDate(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getAdminId(),
                                item.getName(),
                                item.getQuantity(),
                                item.getPrice()))
                        .toList()
        );
    }
    public List<Orders> getAllOrders(Long userId) {
        List<Orders>orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found for user with id: " + userId);
        }
        return orders;
    }

    public List<Orders> getOrdersByStatus(Long userId,OrderStatus status) {
        // find orders by userId and status
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(order-> order.getOrderStatus() == status).toList();
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found for user with id: " + userId + " and status: " + status);
        }
        return orders;
    }
    public Optional<Orders> updateOrdersByStatus(Long id,Long userId, OrderStatus orderStatus){
        // find order by id and userId
        return Optional.of(orderRepository.findById(id).filter(order -> order.getUserId().equals(userId)).map(c -> {
            if (c.getOrderStatus() == OrderStatus.CANCELLED || c.getOrderStatus() == OrderStatus.RETURNED) {
                throw new IllegalArgumentException("Order cannot be updated to " + orderStatus + " after it has been " + c.getOrderStatus());
            }
            c.setOrderStatus(orderStatus);
            if (orderStatus == OrderStatus.CANCELLED) {
                c.setCancelledDate(LocalDateTime.now());
            } else if (orderStatus == OrderStatus.RETURNED) {
                c.setReturnedDate(LocalDateTime.now());
            }
            return orderRepository.save(c);
        }).orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id + " for user with id: " + userId)));
    }
}
