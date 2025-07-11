package com.ff.commandes_service.controller;

import com.ff.commandes_service.dto.OrderRequest;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.security.JwtService;
import com.ff.commandes_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order/users")
public class UserController {
    private final UserService userService;
    private  final JwtService jwtService;

    @PostMapping
    public Orders createOrder(@Valid @RequestBody OrderRequest orders, HttpServletRequest request) {
        //Get userId from token
        Long userId = getUsersTokenAndVerifyIsExist(request);
        return userService.createOrder(userId, orders);
    }

    @GetMapping
    public List<Orders> getAllOrders(HttpServletRequest request) {
        //Get userId from token
        Long userId = getUsersTokenAndVerifyIsExist(request);
        return userService.getAllOrders(userId);
    }
    @GetMapping("/status")
    public List<Orders> getOrdersByStatus(@RequestParam("status") String status, HttpServletRequest request) {
        Long userId = getUsersTokenAndVerifyIsExist(request);
        return userService.getOrdersByStatus(userId, OrderStatus.valueOf(status.toUpperCase()));
    }
    @GetMapping("/{id}")
    public Orders getOrderById(@PathVariable Long id, HttpServletRequest request) {
        //Get userId from token
        Long userId = getUsersTokenAndVerifyIsExist(request);
        return userService.getOrderById(id, userId);
    }
    @PutMapping("/{id}/cancelled")
    public Optional<Orders> cancelledOrder(@PathVariable Long id, HttpServletRequest request) {
        // Get userId from token
        Long userId = getUsersTokenAndVerifyIsExist(request);
        return userService.updateOrdersByStatus(id,userId, OrderStatus.CANCELLED);
    }
    @PutMapping("/{id}/returned")
    public Optional<Orders> returnedOrder(@PathVariable Long id, HttpServletRequest request) {
        // Get userId from token
        Long userId = getUsersTokenAndVerifyIsExist(request);
        return userService.updateOrdersByStatus(id,userId, OrderStatus.RETURNED);
    }
    private Long getUsersTokenAndVerifyIsExist(HttpServletRequest request) {
        if (request == null || request.getHeader("Authorization") == null) {
            throw new IllegalArgumentException("Authorization header is missing or request is null");
        }
        String authHeader = request.getHeader("Authorization");
        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        if(userId == null){
            throw new IllegalArgumentException("User ID not found in token");
        }
        return userId;
    }

}
