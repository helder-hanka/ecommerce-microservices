package com.ff.commandes_service.controller;

import com.ff.commandes_service.dto.OrderRequest;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.service.UserService;
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

    @PostMapping
    public Orders createOrder(@Valid @RequestBody OrderRequest orders) {
        return userService.createOrder(orders);
    }
    @GetMapping
    public List<Orders> getAllOrders() {
        return userService.getAllOrders();
    }
    @GetMapping("/{id}")
    public Orders getOrderById(@PathVariable Long id) {
        return userService.getOrderById(id);
    }
    @GetMapping("/status")
    public List<Orders> getOrdersByStatus(@RequestParam("status") String status) {
        return userService.getOrdersByStatus(OrderStatus.valueOf(status.toUpperCase()));
    }
    @PutMapping("/{id}/cancelled")
    public Optional<Orders> cancelledOrder(@PathVariable Long id){
        return userService.updateOrdersByStatus(id, OrderStatus.CANCELLED);
    }
    @PutMapping("/{id}/returned")
    public Optional<Orders> returnedOrder(@PathVariable Long id){
        return userService.updateOrdersByStatus(id, OrderStatus.RETURNED);
    }

}
