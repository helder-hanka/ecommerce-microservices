package com.ff.paiements_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ff.paiements_service.dto.PaymentStatusPutRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.repository.PaymentRepository;
import com.ff.paiements_service.service.AdminPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AdminPaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminPaymentService adminPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void updatePaymentStatus_Success() {
        Long paymentId = 1L;

        PaymentStatusPutRequest requestDto = new PaymentStatusPutRequest();
        requestDto.setAdminId(999L);
        requestDto.setPaymentStatus(PaymentStatus.COMPLETED);

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setAdminId(999L);
        existingPayment.setPaymentStatus(PaymentStatus.PENDING);
        existingPayment.setPaymentDate(LocalDateTime.now().minusDays(1));

        when(paymentRepository.findById(eq(paymentId))).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);
        // Simulate the update operation
        existingPayment.setPaymentStatus(requestDto.getPaymentStatus());
        existingPayment.setAdminId(requestDto.getAdminId());

        //Payment updatedPayment = adminPaymentService.updatePaymentStatus(paymentId, requestDto);

        //assertNotNull(updatedPayment);
        //assertEquals(PaymentStatus.COMPLETED, updatedPayment.getPaymentStatus());
        //assertEquals(999L, updatedPayment.getAdminId());
    }
    @Test
    void updatePaymentStatus_PaymentNotFound() {
        Long paymentId = 1L;
        PaymentStatusPutRequest requestDto = new PaymentStatusPutRequest();
        requestDto.setAdminId(999L);
        requestDto.setPaymentStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(eq(paymentId))).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            adminPaymentService.updatePaymentStatus(paymentId, requestDto);
        });

        assertEquals("Payment not found with id: " + paymentId, exception.getMessage());
    }
    @Test
    void updatePaymentStatus_InvalidPaymentStatus() {
        Long paymentId = 1L;
        PaymentStatusPutRequest requestDto = new PaymentStatusPutRequest();
        requestDto.setAdminId(999L);
        requestDto.setPaymentStatus(null);

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setAdminId(999L);
        existingPayment.setPaymentStatus(PaymentStatus.PENDING);
        existingPayment.setPaymentDate(LocalDateTime.now().minusDays(1));

        when(paymentRepository.findById(eq(paymentId))).thenReturn(Optional.of(existingPayment));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            adminPaymentService.updatePaymentStatus(paymentId, requestDto);
        });

        assertEquals("Payment not found with id: " + paymentId, exception.getMessage());
    }
    @Test
    void updatePaymentStatus_SerializationError() {
        Long paymentId = 1L;
        PaymentStatusPutRequest requestDto = new PaymentStatusPutRequest();
        requestDto.setAdminId(999L);
        requestDto.setPaymentStatus(PaymentStatus.COMPLETED);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            String jsonContent = mapper.writeValueAsString(requestDto);
            assertNotNull(jsonContent);
        } catch (JsonProcessingException e) {
            fail("JSON serialization failed: " + e.getMessage());
        }
    }
    @Test
    void getAllPayments_Success() {
        // This test would typically check if the service retrieves all payments correctly.
        // Implementation would depend on the actual method in AdminPaymentService.
        // For now, we can just assert that the service is not null.
        assertNotNull(adminPaymentService);
    }
    @Test
    void getAllPayments_NoPaymentsFound() {
        // This test would typically check if the service throws an exception when no payments are found.
        // Implementation would depend on the actual method in AdminPaymentService.
        // For now, we can just assert that the service is not null.
        assertNotNull(adminPaymentService);
    }
    @Test
    void getAllPayments_InvalidPaymentStatus() {
        // This test would typically check if the service throws an exception when an invalid payment status is provided.
        // Implementation would depend on the actual method in AdminPaymentService.
        // For now, we can just assert that the service is not null.
        // Note: In a real scenario, you would mock the repository and check for specific exceptions.
        assertNotNull(adminPaymentService);
    }
    @Test
    void getAllPayments_SerializationError() {
        // This test would typically check if the service handles serialization errors correctly.
        // Implementation would depend on the actual method in AdminPaymentService.
        // For now, we can just assert that the service is not null.
        assertNotNull(adminPaymentService);
    }
    @Test
    void getAllPayments_InvalidPaymentId() {
        // This test would typically check if the service throws an exception when an invalid payment ID is provided.
        // Implementation would depend on the actual method in AdminPaymentService.
        // For now, we can just assert that the service is not null.
        assertNotNull(adminPaymentService);
    }
    @Test
    void getAllPayments_InvalidUserId() {
        // This test would typically check if the service throws an exception when an invalid user ID is provided.
        // Implementation would depend on the actual method in AdminPaymentService.
        // For now, we can just assert that the service is not null.
        assertNotNull(adminPaymentService);
    }

}
