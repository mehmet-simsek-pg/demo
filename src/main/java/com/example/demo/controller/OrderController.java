package com.example.demo.controller;

import com.example.demo.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Order", description = "Sipariş (Order) CRUD işlemleri — müşteri adı, toplam tutar, durum ve oluşturulma zamanı içerir.")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository repo;

    public OrderController(OrderRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Tüm siparişleri listele", description = "Sistemde kayıtlı tüm siparişleri döner.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Sipariş listesi başarıyla döndü")})
    @GetMapping
    public List<Order> getAll() {
        return repo.findAll();
    }

    @Operation(summary = "ID ile siparişi getir", description = "Belirtilen ID numarasına sahip siparişi döner.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Sipariş bulundu"), @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))), @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))})
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@Parameter(description = "Sipariş ID", example = "1") @PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Yeni sipariş oluştur", description = "Yeni bir sipariş kaydı oluşturur. `createdAt` değeri backend tarafından otomatik atanır.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Yeni sipariş bilgileri", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class), examples = @ExampleObject(name = "Order Example", value = """
            {
              "orderNumber": "ORD-20251111-001",
              "customerName": "Ali Yılmaz",
              "totalAmount": 1299.99,
              "status": "CREATED"
            }
            """))))
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Sipariş başarıyla oluşturuldu"), @ApiResponse(responseCode = "400", description = "Geçersiz istek veya validation hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Validation Error", value = """
            {
              "timestamp": "2025-11-11T12:34:56.789",
              "status": 400,
              "error": "Bad Request",
              "message": "orderNumber must not be blank, customerName must not be blank",
              "path": "/orders"
            }
            """))), @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))})
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Order> create(@Valid @RequestBody Order order) {
        order.setCreatedAt(LocalDateTime.now());
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("CREATED");
        }
        Order saved = repo.save(order);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(summary = "Var olan siparişi güncelle", description = "Sipariş durumunu veya diğer alanları günceller.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Güncellenecek sipariş bilgileri", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class), examples = @ExampleObject(name = "Update Order", value = """
            {
              "orderNumber": "ORD-20251111-001",
              "customerName": "Ali Yılmaz",
              "totalAmount": 1299.99,
              "status": "CANCELED"
            }
            """))))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Sipariş güncellendi"), @ApiResponse(responseCode = "400", description = "Geçersiz istek veya validation hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))), @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))), @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))})
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Order> update(@PathVariable Long id, @Valid @RequestBody Order order) {
        return repo.findById(id).map(existing -> {
            existing.setOrderNumber(order.getOrderNumber());
            existing.setCustomerName(order.getCustomerName());
            existing.setTotalAmount(order.getTotalAmount());
            existing.setStatus(order.getStatus());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Siparişi sil", description = "Belirtilen ID'ye sahip siparişi sistemden siler.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Sipariş silindi (response body yok)"), @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = {@ExampleObject(name = "Order Not Found", value = """
            {
              "timestamp": "2025-11-11T12:34:56.789",
              "status": 404,
              "error": "Not Found",
              "message": "Order not found",
              "path": "/orders/999"
            }
            """)})), @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Sipariş ID", example = "1") @PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

