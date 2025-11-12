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
import com.example.demo.entity.Course;
import com.example.demo.repository.CourseRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Course", description = "Ders (Course) CRUD işlemleri")
@RestController
@RequestMapping(
        value = "/courses",
        produces = "application/json"
)
public class CourseController {

    private final CourseRepository repo;

    public CourseController(CourseRepository repo) {
        this.repo = repo;
    }

    @Operation(
            summary = "Tüm dersleri listele",
            description = "Sistemde kayıtlı tüm derslerin listesini döner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ders listesi başarıyla döndü")
    })
    @GetMapping
    public List<Course> getAll() {
        return repo.findAll();
    }

    @Operation(
            summary = "ID ile dersi getir",
            description = "Belirtilen ID numarasına sahip dersi döner. Ders bulunamazsa 404 döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ders bulundu"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ders bulunamadı",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not Found", value = """
                                    {
                                      "timestamp": "2025-11-11T12:34:56.789",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Course not found",
                                      "path": "/courses/999"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Yeni ders oluştur",
            description = "Yeni bir ders kaydı oluşturur.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Eklenecek ders bilgileri",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Course.class),
                            examples = @ExampleObject(name = "Course Example", value = """
                                    {
                                      "code": "API101",
                                      "title": "API Testing Giriş",
                                      "description": "HTTP, status code ve JSON body testleri",
                                      "credit": 4
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ders başarıyla oluşturuldu"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz istek veya validation hatası",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "timestamp": "2025-11-11T12:34:56.789",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "code must not be blank, title must not be blank",
                                      "path": "/courses"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu tarafında beklenmeyen hata",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Course> create(@Valid @RequestBody Course course) {
        Course saved = repo.save(course);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Var olan dersi güncelle",
            description = "Belirtilen ID'ye sahip dersin bilgilerini günceller.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Güncellenecek ders bilgileri",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Course.class),
                            examples = @ExampleObject(name = "Update Example", value = """
                                    {
                                      "code": "API201",
                                      "title": "API Testing İleri",
                                      "description": "Auth, token ve hata yönetimi",
                                      "credit": 5
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ders güncellendi"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz istek veya validation hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ders bulunamadı",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Course> update(
            @Parameter(description = "Ders ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Course course) {

        return repo.findById(id)
                .map(existing -> {
                    existing.setCode(course.getCode());
                    existing.setTitle(course.getTitle());
                    existing.setDescription(course.getDescription());
                    existing.setCredit(course.getCredit());
                    Course updated = repo.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Dersi sil",
            description = "Belirtilen ID'ye sahip dersi sistemden siler."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ders silindi"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ders bulunamadı",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Sunucu hatası",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Ders ID", example = "1")
            @PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
