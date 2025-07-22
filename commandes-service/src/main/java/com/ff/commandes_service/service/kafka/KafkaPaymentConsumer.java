package com.ff.commandes_service.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ff.commandes_service.dto.PaymentSuccessEvent;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment_success_topic", groupId = "order_service_group")
    @Transactional
    public void consumePaymentSuccessEvent(String message) {
        try {
            // Convertir le message JSON en un objet PaymentSuccessEvent
            PaymentSuccessEvent event = objectMapper.readValue(message, PaymentSuccessEvent.class);
            log.info("Received PaymentSuccessEvent: {}", event);

            // Mettre à jour l'état de la commande dans la base de données
            Orders order = orderRepository.findById(event.getOrderId()).orElseThrow(() ->
            new IllegalArgumentException("Order not found for ID: " + event.getOrderId()));
            order.setOrderStatus(OrderStatus.VALIDATED);
            order.setValidatedDate(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order with ID {} updated to VALIDATED status", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing PaymentSuccessEvent from Kafka", e);
        }
    }
    @KafkaListener(topics = "payment_failed_topic", groupId = "order_service_group")
    @Transactional
    public void consumePaymentFailedEvent(String message) {
        try {
            // Convertir le message JSON en un objet PaymentSuccessEvent
            PaymentSuccessEvent event = objectMapper.readValue(message, PaymentSuccessEvent.class);
            log.info("Received PaymentFailedEvent: {}", event);

            // Mettre à jour l'état de la commande dans la base de données
            Orders order = orderRepository.findById(event.getOrderId()).orElseThrow(() ->
            new IllegalArgumentException("Order not found for ID: " + event.getOrderId()));
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setCancelledDate(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order with ID {} updated to CANCELED status", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent from Kafka", e);
        }
    }
}
