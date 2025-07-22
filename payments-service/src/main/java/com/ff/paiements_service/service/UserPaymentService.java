package com.ff.paiements_service.service;

import com.ff.paiements_service.dto.PaymentFailedEvent;
import com.ff.paiements_service.dto.PaymentInitiatedEvent;
import com.ff.paiements_service.dto.PaymentPostRequest;
import com.ff.paiements_service.dto.PaymentSuccessEvent;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.repository.PaymentRepository;
import com.ff.paiements_service.service.kafka.KafkaPaymentProducer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserPaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaPaymentProducer kafkaPaymentProducer;

    @Transactional
    public Payment createPayment(Long userId, PaymentPostRequest paymentRequest) {
        System.out.println(("EST CE QUE TU PASSE PAR LA JE NE CROIS PAS :::::::"));
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        // Logique pour initier le paiement
        // Ici, vous auriez normalement l'appel à une passerelle de paiement (Stripe, PayPal, etc.)
        Payment payment = paymentRepository.findByOrderId(paymentRequest.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order ID: " + paymentRequest.getOrderId()));
        // Vérifier si l'ID utilisateur correspond à celui du paiement
        if (!payment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User ID does not match the payment's user ID");
        }
        // Mettre à jour les détails du paiement avant de l'envoyer à la passerelle
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setPaymentStatus(paymentRequest.getPaymentStatus()); // Statut initial avant le traitement du paiement
        payment.setPaymentDate(LocalDateTime.now());
        // Enregistrer le paiement dans la base de données
        paymentRepository.save(payment);

        // --- NOUVEAU: Publier l'événement d'initiation sur Kafka ---
        PaymentInitiatedEvent initiatedEvent = new PaymentInitiatedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAdminId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentDate(),
                payment.getPaymentStatus()
        );
        kafkaPaymentProducer.sendPaymentInitiatedEvent(initiatedEvent);
        // Simuler le traitement du paiement (remplacez ceci par l'appel à une API de paiement réelle)
        boolean paymentGatewaySuccess = Math.random() < 0.8; // 80% de chance de succès
        if (paymentGatewaySuccess){
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId("TXN-" + System.currentTimeMillis()); // ID de transaction fictif
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);
            // --- NOUVEAU: Publier l'événement de succès sur Kafka ---
            PaymentSuccessEvent successEvent = new PaymentSuccessEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAdminId(),
                    payment.getAmount(),
                    payment.getTransactionId(),
                    payment.getPaymentDate(),
                    PaymentStatus.COMPLETED
            );
            kafkaPaymentProducer.sendPaymentSuccessEvent(successEvent);

        }else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            // --- NOUVEAU: Publier l'événement d'échec sur Kafka ---
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAdminId(),
                    payment.getPaymentStatus(),
                    payment.getPaymentDate(),
                    "Payment gateway error" // Message d'erreur fictif
            );
            kafkaPaymentProducer.sendPaymentFailedEvent(failedEvent);
            // ------------------------------------------
            throw new RuntimeException("Payment failed for order: " + payment.getOrderId());
        }

        return payment;
       /* var payment = Payment.builder()
                .userId(userId)
                .adminId(paymentRequest.getAdminId())
                .orderId(paymentRequest.getOrderId())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .amount(paymentRequest.getAmount())
                .paymentStatus(paymentRequest.getPaymentStatus())
                .paymentDate(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);*/
    }
}
