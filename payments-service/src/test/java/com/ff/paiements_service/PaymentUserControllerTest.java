package com.ff.paiements_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ff.paiements_service.controller.PaymentUserController;
import com.ff.paiements_service.dto.PaymentPostRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.entity.PaymentMethod;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.security.JwtService;
import com.ff.paiements_service.service.PaymentService;
import com.ff.paiements_service.service.UserPaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections; // Ajouté
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@WebMvcTest(PaymentUserController.class)
@Import(PaymentUserControllerTest.MockServiceConfig.class)
public class PaymentUserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserPaymentService userPaymentService;

    @Autowired
    private PaymentService paymentService;

    private Payment mockPayment;
    private final Long TEST_USER_ID = 1L;
    private final String TEST_USER_ROLE = "USER";

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public UserPaymentService userPaymentService() {
            return Mockito.mock(UserPaymentService.class);
        }

        @Bean
        public PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockPayment = Payment.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .orderId(101L)
                .paymentMethod(PaymentMethod.BANK_CARD)
                .amount(BigDecimal.valueOf(100.00))
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();

        // Simuler l'authentification avec un rôle
        // C'est la méthode manuelle, mais pour @WebMvcTest, `with(user(...))` est plus idiomatique
        // UsernamePasswordAuthenticationToken authentication =
        //         new UsernamePasswordAuthenticationToken(
        //                 "testuser@example.com", TEST_USER_ID,
        //                 Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + TEST_USER_ROLE))
        //         );
        // SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    // --- Corrected Utility Method for User Authentication ---
    private RequestPostProcessor authenticatedUserWithUserId(String username, Long userId, String role) {
        // Create the authorities list
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

        // Create a UserDetails object (the principal)
        User principal = new User(username, "password", authorities); // Password can be anything, it's not used here

        // Create the UsernamePasswordAuthenticationToken manually,
        // putting the userId directly into the 'credentials' field as expected by your JwtAuthenticationFilter
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, userId, authorities);

        // Return a RequestPostProcessor that sets this authentication object
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }


    @Test
    void createPayment_ShouldReturnPayment() throws Exception {
        PaymentPostRequest request = new PaymentPostRequest();
        request.setOrderId(101L);
        request.setAdminId(1L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setPaymentMethod(PaymentMethod.BANK_CARD);
        request.setPaymentStatus(PaymentStatus.PENDING);

        when(userPaymentService.createPayment(eq(TEST_USER_ID), any(PaymentPostRequest.class))).thenReturn(mockPayment);

        mockMvc.perform(post("/api/payments/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        // Utilise notre méthode utilitaire pour simuler l'utilisateur
                        .with(authenticatedUserWithUserId("testuser", TEST_USER_ID, TEST_USER_ROLE))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockPayment.getId()))
                .andExpect(jsonPath("$.userId").value(mockPayment.getUserId()))
                .andExpect(jsonPath("$.adminId").value(mockPayment.getAdminId()))
                .andExpect(jsonPath("$.transactionId").value(mockPayment.getTransactionId()))
                .andExpect(jsonPath("$.orderId").value(mockPayment.getOrderId()))
                .andExpect(jsonPath("$.amount").value(mockPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$.paymentStatus").value(mockPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$.paymentMethod").value(mockPayment.getPaymentMethod().name()));

        verify(userPaymentService, times(1)).createPayment(eq(TEST_USER_ID), any(PaymentPostRequest.class));
    }

    @Test
    void createPayment_InvalidAmount_ShouldReturnBadRequest() throws Exception {
        PaymentPostRequest request = new PaymentPostRequest();
        request.setOrderId(101L);
        request.setAdminId(1L);
        request.setAmount(BigDecimal.valueOf(0)); // Montant invalide
        request.setPaymentMethod(PaymentMethod.BANK_CARD);
        request.setPaymentStatus(PaymentStatus.PENDING);

        mockMvc.perform(post("/api/payments/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()) // Nécessaire pour POST
                        .with(user("testuser").password("pass").roles(TEST_USER_ROLE))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_InvalidPaymentMethod_ShouldReturnBadRequest() throws Exception {
        PaymentPostRequest request = new PaymentPostRequest();
        request.setOrderId(101L);
        request.setAdminId(123L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setPaymentStatus(PaymentStatus.PENDING);
        request.setPaymentMethod(null); // Méthode de paiement invalide

        mockMvc.perform(post("/api/payments/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()) // Nécessaire pour POST
                        .with(user("testuser").password("pass").roles(TEST_USER_ROLE))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentById_ShouldReturnPayment() throws Exception {
        Long paymentId = 1L;
        when(paymentService.findPaymentById(paymentId)).thenReturn(mockPayment);

        mockMvc.perform(get("/api/payments/user/{id}", paymentId)
                        .with(user("testuser").password("pass").roles(TEST_USER_ROLE)) // Simule l'utilisateur pour GET
                )
                .andExpect(status().isOk())
                        .andExpectAll(jsonPath("$.id").value(mockPayment.getId()),
                                jsonPath("$.userId").value(mockPayment.getUserId()),
                                jsonPath("$.orderId").value(mockPayment.getOrderId()),
                                jsonPath("$.adminId").value(mockPayment.getAdminId()),
                                jsonPath("$.transactionId").value(mockPayment.getTransactionId()),
                                jsonPath("$.amount").value(mockPayment.getAmount().doubleValue()),
                                jsonPath("$.paymentStatus").value(mockPayment.getPaymentStatus().name()),
                                jsonPath("$.paymentMethod").value(mockPayment.getPaymentMethod().name()));
        verify(paymentService, times(1)).findPaymentById(paymentId);
    }

    @Test
    void getAllPaymentsForCurrentUser_ShouldReturnPayments() throws Exception {
        when(paymentService.getAllPaymentsByUserId(TEST_USER_ID)).thenReturn(List.of(mockPayment));

        mockMvc.perform(get("/api/payments/user")
                        .with(authenticatedUserWithUserId("testUser", TEST_USER_ID, TEST_USER_ROLE)) // Simule l'utilisateur pour GET
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(mockPayment.getId()))
                .andExpect(jsonPath("$[0].userId").value(mockPayment.getUserId()))
                .andExpect(jsonPath("$[0].orderId").value(mockPayment.getOrderId()))
                .andExpect(jsonPath("$[0].amount").value(mockPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$[0].paymentStatus").value(mockPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$[0].paymentMethod").value(mockPayment.getPaymentMethod().name()));

        verify(paymentService, times(1)).getAllPaymentsByUserId(TEST_USER_ID);
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPayments() throws Exception {
        Long orderId = 101L;
        //when(paymentService.getPaymentsByOrderId(orderId)).thenReturn(List.of(mockPayment));
        when(paymentService.getPaymentsByOrderId(orderId)).thenReturn(Optional.ofNullable(mockPayment));

        mockMvc.perform(get("/api/payments/user/orderId/{orderId}", orderId)
                        .with(user("testuser").password("pass").roles(TEST_USER_ROLE)) // Simule l'utilisateur pour GET
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockPayment.getId()))
                .andExpect(jsonPath("$.userId").value(mockPayment.getUserId()))
                .andExpect(jsonPath("$.adminId").value(mockPayment.getAdminId()))
                .andExpect(jsonPath("$.transactionId").value(mockPayment.getTransactionId()))
                .andExpect(jsonPath("$.orderId").value(mockPayment.getOrderId()))
                .andExpect(jsonPath("$.amount").value(mockPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$.paymentStatus").value(mockPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$.paymentMethod").value(mockPayment.getPaymentMethod().name()));

        verify(paymentService, times(1)).getPaymentsByOrderId(orderId);
    }

}