package com.ff.paiements_service.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ff.paiements_service.dto.OrderCreatedEvent;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.entity.PaymentMethod;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository; // Pour enregistrer le paiement initial

//    Ce consommateur peut être utilisé pour pré-créer un enregistrement de paiement PENDING dans la base de données
//    du service de paiement dès qu'une commande est créée, avant même que l'utilisateur n'initie le paiement.
    @KafkaListener(topics = "order_created_topic", groupId = "payment_service_group")
    public void consumeOrderCreatedEvent(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent: {}", event);

            // Créer une entrée de paiement initiale dans la base de données du service de paiement
            // Cette entrée représente un paiement en attente pour la commande
            Payment existingPayment = paymentRepository.findByOrderId(event.getOrderId()).orElse(null);

            if (existingPayment == null) {
                // Si aucune entrée de paiement n'existe pour cette commande, en créer une nouvelle
                // Note: On suppose que l'ID de transaction est généré plus tard, donc on initialise avec un message par défaut
            if (event.getTransactionId() == null || event.getTransactionId().isEmpty()) {
                event.setTransactionId("TRANSACTION-: Not yet initiated");
            }

                Payment payment = new Payment();
                payment.setOrderId(event.getOrderId());
                payment.setUserId(event.getUserId());
                payment.setAdminId(event.getAdminId());
                payment.setTransactionId(event.getTransactionId());
                payment.setAmount(event.getTotalAmount());
                payment.setPaymentStatus(PaymentStatus.valueOf(event.getStatus())); // Statut initial
                payment.setPaymentMethod(PaymentMethod.PAYPAL); // Sera mis à jour lors de l'initiation réelle
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);
                log.info("Created PENDING payment entry for orderId: {}", event.getOrderId());
            } else {
                log.info("Payment entry already exists for orderId: {}", event.getOrderId());
            }

        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent from Kafka", e);
        }
    }
}