package com.example.demo.controller;

import com.example.demo.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.demo.entity.AppUser;
import com.example.demo.repository.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Auth", description = "Kullanıcı kayıt ve login işlemleri")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepository userRepo;

    public AuthController(AppUserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @Operation(
            summary = "Yeni kullanıcı kaydı (register)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Kullanıcı kayıt bilgileri (şifre plaintext; demo amaçlı)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppUser.class),
                            examples = @ExampleObject(
                                    name = "Register Example",
                                    value = """
                {
                  "username": "testuser",
                  "password": "123456",
                  "fullName": "Test User"
                }
                """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kullanıcı oluşturuldu"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz istek veya validation hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name="Validation Error", value="""
            {
              "timestamp":"2025-11-11T12:34:56.789",
              "status":400,
              "error":"Bad Request",
              "message":"username must not be blank, password must not be blank",
              "path":"/auth/register"
            }
            """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Kullanıcı adı zaten mevcut",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name="Conflict", value="""
            {
              "timestamp":"2025-11-11T12:34:56.789",
              "status":409,
              "error":"Conflict",
              "message":"Username already exists",
              "path":"/auth/register"
            }
            """))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping(path="/register", consumes="application/json", produces="application/json")
    public ResponseEntity<?> register(@Valid @RequestBody AppUser user) {
        if (userRepo.existsByUsername(user.getUsername())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        AppUser saved = userRepo.save(user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("username", saved.getUsername());
        resp.put("fullName", saved.getFullName());
        resp.put("message", "User registered");

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(
            summary = "Kullanıcı login",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Login bilgileri",
                    content = @Content(
                            mediaType = "application/json",
                            // Map<String,String> için ayrı bir DTO yazmadıysan schema belirtmek zorunda değilsin
                            examples = @ExampleObject(
                                    name = "Login Example",
                                    value = """
                {
                  "username": "testuser",
                  "password": "123456"
                }
                """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login başarılı"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Geçersiz kullanıcı adı veya şifre",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name="Unauthorized", value="""
            {
              "timestamp":"2025-11-11T12:34:56.789",
              "status":401,
              "error":"Unauthorized",
              "message":"Invalid username or password",
              "path":"/auth/login"
            }
            """))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping(path="/login", consumes="application/json", produces="application/json")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        return userRepo.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .map(u -> {
                    String token = "dummy-token-" + u.getId();

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("message", "Login successful");
                    resp.put("token", token);
                    resp.put("username", u.getUsername());
                    resp.put("fullName", u.getFullName());

                    return ResponseEntity.ok(resp);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("message", "Invalid username or password");
                    return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(error);
                });
    }
}
