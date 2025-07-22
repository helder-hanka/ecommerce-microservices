package com.ff.commandes_service.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ff.commandes_service.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaProducer {
    private static final String TOPIC = "order_created_topic";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) throws JsonProcessingException {
        // Convert OrderCreatedEvent to JSON string
        String message = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(TOPIC, message);
        log.info("Sent OrderCreatedEvent: {}", event);
    }
}
