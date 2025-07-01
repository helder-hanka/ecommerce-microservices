package com.ff.commandes_service.controller;

import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/order/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping
    public Optional<List<Orders>>getAllOrders() {
        return adminService.getAllOrders();
    }
    @GetMapping("/count")
    public long countOrders() {
        return adminService.countOrders();
    }
    @GetMapping("/count/status")
    public long countOrdersByStatus(String status) {
        return adminService.countOrdersByStatus(status);
    }
    @GetMapping("/count/all")
    public long countAllOrders() {
        return adminService.countAllOrders();
    }
    @GetMapping("/{id}")
    public Optional<Orders> getOrderById(@PathVariable Long id) {
        return adminService.getOrderById(id);
    }
    @GetMapping("/{id}/status")
    public Optional<Orders> updateOrderStatus(@PathVariable Long id, OrderStatus status) {
        return adminService.updateOrderStatus(id, status);
    }
    @GetMapping("/{id}/status/{status}")
    public Optional<Orders> updateOrderStatusByString(@PathVariable Long id, @PathVariable String status) {
        return adminService.updateOrderStatus(id, OrderStatus.valueOf(status.toUpperCase()));
    }
}
