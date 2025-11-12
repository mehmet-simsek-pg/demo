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
import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student", description = "Öğrenci CRUD işlemleri")
@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository repo;

    public StudentController(StudentRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Tüm öğrencileri listele")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste başarıyla döndü"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public List<Student> getAll() {
        return repo.findAll();
    }

    @Operation(
            summary = "ID ile öğrenci getir",
            description = "Belirtilen ID'ye sahip öğrenciyi döner. Bulunamazsa 404."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Öğrenci bulundu"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Öğrenci bulunamadı",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "timestamp": "2025-11-11T12:34:56.789",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Student not found",
                                      "path": "/students/999"
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@Parameter(description = "Öğrenci ID", example = "1") @PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Yeni öğrenci oluştur",
            description = "Yeni bir öğrenci kaydı oluşturur.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Eklenecek öğrenci bilgileri",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Student.class),
                            examples = @ExampleObject(name = "Student Example", value = """
                                    {
                                      "firstName": "Ali",
                                      "lastName": "Yılmaz",
                                      "email": "ali@example.com"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Öğrenci başarıyla oluşturuldu"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz istek veya validation hatası",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "timestamp": "2025-11-11T12:34:56.789",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "email must not be blank",
                                      "path": "/students"
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu tarafında beklenmeyen hata",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
        Student saved = repo.save(student);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Var olan öğrenciyi güncelle",
            description = "Belirtilen ID'ye sahip öğrencinin bilgilerini günceller.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Güncellenecek öğrenci bilgileri",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Student.class),
                            examples = @ExampleObject(name = "Update Example", value = """
                                    {
                                      "firstName": "Ali",
                                      "lastName": "Güncellendi",
                                      "email": "ali.updated@example.com"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Öğrenci güncellendi"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz istek veya validation hatası",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Öğrenci bulunamadı",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Student> update(@Parameter(description = "Öğrenci ID", example = "1")
                                          @PathVariable Long id,
                                          @Valid @RequestBody Student student) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setFirstName(student.getFirstName());
                    existing.setLastName(student.getLastName());
                    existing.setEmail(student.getEmail());
                    Student updated = repo.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Öğrenciyi sil",
            description = "Belirtilen ID'ye sahip öğrenciyi sistemden siler."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Öğrenci silindi"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Öğrenci bulunamadı",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Öğrenci ID", example = "1") @PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
