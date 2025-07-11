package com.ff.commandes_service.controller;

import com.ff.commandes_service.dto.CountOrdersResponse;
import com.ff.commandes_service.dto.OrderStatusRequest;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.security.JwtService;
import com.ff.commandes_service.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/order/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity <Optional<List<Orders>>>getAllOrdersByAdmin(HttpServletRequest request) {
        Long adminId = getAdminIdFromToken(request);
        List<Orders> orders = adminService.getAllOrdersByAdmin(adminId);
        if (orders.isEmpty()) {
           throw new IllegalArgumentException("No orders found for admin with id: " + adminId);
        }
        return ResponseEntity.ok(Optional.of(orders));

    }
    @GetMapping("/count")
    public CountOrdersResponse countOrders(HttpServletRequest request) {
        Long adminId = getAdminIdFromToken(request);
        return adminService.countOrders(adminId);
    }
    @GetMapping("/count/status")
    public long countOrdersByStatus(String status) {
        return adminService.countOrdersByStatus(status);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Orders>> getOrderByIdByAdmin(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = getAdminIdFromToken(request);
        return ResponseEntity.ok(adminService.getOrderByIdByAdmin(id, adminId));
    }
    @PutMapping("/{id}/orderStatus")
    public Optional<Orders> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusRequest orderStatus, HttpServletRequest request) {
        Long adminId = getAdminIdFromToken(request);
        return adminService.updateOrderStatus(id, adminId, orderStatus);
    }
    private Long getAdminIdFromToken(HttpServletRequest request) {
        if(request == null || request.getHeader("Authorization") == null) {
            throw new IllegalArgumentException("Authorization header is missing or request is null");
        }
        String authHeader = request.getHeader("Authorization");
        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        String token = authHeader.substring(7);
        Long adminId = jwtService.extractUserId(token);
        if (adminId == null) {
            throw new IllegalArgumentException("Invalid token or admin ID not found in token");
        }

        return adminId;
    }
    private void isOrderAdmin(Long id, HttpServletRequest request, Orders order){
        Long adminIdOrder = order.getAdminId();

        Long adminId = getAdminIdFromToken(request);
        if (!adminId.equals(adminIdOrder)) {
            throw new IllegalArgumentException("You are not authorized to access this order");
        }
    }
}
