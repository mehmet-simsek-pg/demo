package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "orderNumber must not be blank")
    private String orderNumber;

    @NotBlank(message = "customerName must not be blank")
    private String customerName;

    @PositiveOrZero(message = "totalAmount must be zero or positive")
    private Double totalAmount;

    private String status;          // Örn: CREATED, PAID, CANCELED
    private LocalDateTime createdAt;

    public Order() {
    }

    public Order(Long id, String orderNumber, String customerName,
                 Double totalAmount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
