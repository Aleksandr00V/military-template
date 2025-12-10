package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.VehicleCategoryCreateDTO;
import ua.edu.viti.military.dto.response.VehicleCategoryResponseDTO;
import ua.edu.viti.military.service.VehicleCategoryService;
import ua.edu.viti.military.validation.OnCreate;
import ua.edu.viti.military.validation.OnUpdate;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Categories", description = "API для управління категоріями транспорту")
public class VehicleCategoryController {

    private final VehicleCategoryService categoryService;

    // CREATE
    @PostMapping
    @Operation(summary = "Створити категорію", description = "Створює нову категорію транспорту")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Категорію створено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "409", description = "Категорія з таким кодом/назвою вже існує")
    })
    public ResponseEntity<VehicleCategoryResponseDTO> create(
            @Validated(OnCreate.class) @RequestBody VehicleCategoryCreateDTO dto) {
        log.info("POST /api/vehicle-categories - створення категорії: {}", dto.getName());

        VehicleCategoryResponseDTO created = categoryService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ by ID
    @GetMapping("/{id}")
    @Operation(summary = "Отримати категорію за ID", description = "Повертає категорію за вказаним ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Категорію знайдено"),
            @ApiResponse(responseCode = "404", description = "Категорію не знайдено")
    })
    public ResponseEntity<VehicleCategoryResponseDTO> getById(
            @Parameter(description = "ID категорії") @PathVariable Long id) {
        log.info("GET /api/vehicle-categories/{}", id);

        VehicleCategoryResponseDTO category = categoryService.getById(id);

        return ResponseEntity.ok(category);
    }

    // READ all
    @GetMapping
    @Operation(summary = "Отримати всі категорії", description = "Повертає список всіх категорій транспорту")
    @ApiResponse(responseCode = "200", description = "Список категорій")
    public ResponseEntity<List<VehicleCategoryResponseDTO>> getAll() {
        log.info("GET /api/vehicle-categories - отримання всіх категорій");

        List<VehicleCategoryResponseDTO> categories = categoryService.getAll();

        return ResponseEntity.ok(categories);
    }

    // UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Оновити категорію", description = "Оновлює існуючу категорію транспорту")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Категорію оновлено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "404", description = "Категорію не знайдено")
    })
    public ResponseEntity<VehicleCategoryResponseDTO> update(
            @Parameter(description = "ID категорії") @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody VehicleCategoryCreateDTO dto) {
        log.info("PUT /api/vehicle-categories/{} - оновлення категорії", id);

        VehicleCategoryResponseDTO updated = categoryService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити категорію", description = "Видаляє категорію транспорту за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Категорію видалено"),
            @ApiResponse(responseCode = "404", description = "Категорію не знайдено")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID категорії") @PathVariable Long id) {
        log.info("DELETE /api/vehicle-categories/{}", id);

        categoryService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
