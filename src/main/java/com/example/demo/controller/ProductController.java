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
import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product", description = "Ürün CRUD işlemleri — ürün adı, kategori, fiyat ve stok bilgilerini içerir.")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @Operation(
            summary = "Tüm ürünleri listele",
            description = "Sistemde kayıtlı tüm ürünleri döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ürün listesi başarıyla döndü")
    })
    @GetMapping
    public List<Product> getAll() {
        return repo.findAll();
    }

    @Operation(
            summary = "ID ile ürünü getir",
            description = "Belirtilen ID'ye sahip ürünü döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ürün bulundu"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ürün bulunamadı",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Not Found",
                                    value = """
                {
                  "timestamp": "2025-11-11T12:34:56.789",
                  "status": 404,
                  "error": "Not Found",
                  "message": "Product not found",
                  "path": "/products/999"
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@Parameter(description = "Ürün ID", example = "1")
                                               @PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Yeni ürün oluştur",
            description = "Yeni bir ürün kaydı ekler.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Eklenecek ürün bilgileri",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Product.class),
                            examples = @ExampleObject(
                                    name = "Product Example",
                                    value = """
                {
                  "name": "Klavye",
                  "category": "Elektronik",
                  "price": 499.90,
                  "stock": 25
                }
                """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ürün başarıyla oluşturuldu"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz istek veya validation hatası",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                {
                  "timestamp": "2025-11-11T12:34:56.789",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "name must not be blank, price must be zero or positive",
                  "path": "/products"
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        Product saved = repo.save(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Var olan ürünü güncelle",
            description = "Belirtilen ID'ye sahip ürünün bilgilerini günceller.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Güncellenecek ürün bilgileri",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Product.class),
                            examples = @ExampleObject(
                                    name = "Update Product",
                                    value = """
                {
                  "name": "Gaming Klavye",
                  "category": "Elektronik",
                  "price": 799.90,
                  "stock": 15
                }
                """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ürün güncellendi"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz istek veya validation hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ürün bulunamadı",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @Valid @RequestBody Product product) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setName(product.getName());
                    existing.setCategory(product.getCategory());
                    existing.setPrice(product.getPrice());
                    existing.setStock(product.getStock());
                    Product updated = repo.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Ürünü sil",
            description = "Belirtilen ID'ye sahip ürünü sistemden kaldırır."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ürün silindi"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ürün bulunamadı",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Ürün ID", example = "1") @PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
