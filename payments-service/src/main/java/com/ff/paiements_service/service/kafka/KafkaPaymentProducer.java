package com.ff.paiements_service.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ff.paiements_service.dto.PaymentFailedEvent;
import com.ff.paiements_service.dto.PaymentInitiatedEvent;
import com.ff.paiements_service.dto.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentInitiatedEvent(PaymentInitiatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payment_initiated_topic", message);
            log.info("Sent PaymentInitiatedEvent: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error converting PaymentInitiatedEvent to JSON", e);
        }
    }

    public void sendPaymentSuccessEvent(PaymentSuccessEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payment_success_topic", message); // Nouveau topic pour le succès
            log.info("Sent PaymentSuccessEvent: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error converting PaymentSuccessEvent to JSON", e);
        }
    }

    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payment_failed_topic", message); // Nouveau topic pour l'échec
            log.info("Sent PaymentFailedEvent: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Error converting PaymentFailedEvent to JSON", e);
        }
    }
}