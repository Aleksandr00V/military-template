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
import ua.edu.viti.military.dto.request.DriverCreateDTO;
import ua.edu.viti.military.dto.response.DriverResponseDTO;
import ua.edu.viti.military.service.DriverService;
import ua.edu.viti.military.validation.OnCreate;
import ua.edu.viti.military.validation.OnUpdate;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Drivers", description = "API для управління водіями")
public class DriverController {

    private final DriverService driverService;

    // CREATE
    @PostMapping
    @Operation(summary = "Створити водія", description = "Реєструє нового водія в системі")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Водія створено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "409", description = "Водій з таким військовим ID вже існує")
    })
    public ResponseEntity<DriverResponseDTO> create(
            @Validated(OnCreate.class) @RequestBody DriverCreateDTO dto) {
        log.info("POST /api/drivers - створення водія: {}", dto.getMilitaryId());

        DriverResponseDTO created = driverService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ by ID
    @GetMapping("/{id}")
    @Operation(summary = "Отримати водія за ID", description = "Повертає водія за вказаним ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Водія знайдено"),
            @ApiResponse(responseCode = "404", description = "Водія не знайдено")
    })
    public ResponseEntity<DriverResponseDTO> getById(
            @Parameter(description = "ID водія") @PathVariable Long id) {
        log.info("GET /api/drivers/{}", id);

        DriverResponseDTO driver = driverService.getById(id);

        return ResponseEntity.ok(driver);
    }

    // READ all
    @GetMapping
    @Operation(summary = "Отримати всіх водіїв", description = "Повертає список всіх водіїв")
    @ApiResponse(responseCode = "200", description = "Список водіїв")
    public ResponseEntity<List<DriverResponseDTO>> getAll() {
        log.info("GET /api/drivers - отримання всіх водіїв");

        List<DriverResponseDTO> drivers = driverService.getAll();

        return ResponseEntity.ok(drivers);
    }

    // READ active drivers
    @GetMapping("/active")
    @Operation(summary = "Отримати активних водіїв", description = "Повертає список тільки активних водіїв")
    @ApiResponse(responseCode = "200", description = "Список активних водіїв")
    public ResponseEntity<List<DriverResponseDTO>> getActiveDrivers() {
        log.info("GET /api/drivers/active - отримання активних водіїв");

        List<DriverResponseDTO> drivers = driverService.getActiveDrivers();

        return ResponseEntity.ok(drivers);
    }

    // READ drivers with expiring license
    @GetMapping("/expiring-license")
    @Operation(summary = "Водії з правами що закінчуються",
            description = "Повертає водіїв, у яких права закінчуються протягом вказаної кількості днів")
    @ApiResponse(responseCode = "200", description = "Список водіїв")
    public ResponseEntity<List<DriverResponseDTO>> getDriversWithExpiringLicense(
            @Parameter(description = "Кількість днів до закінчення прав")
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/drivers/expiring-license?days={}", days);

        List<DriverResponseDTO> drivers = driverService.getDriversWithExpiringLicense(days);

        return ResponseEntity.ok(drivers);
    }

    // UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Оновити водія", description = "Оновлює дані існуючого водія")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Водія оновлено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "404", description = "Водія не знайдено")
    })
    public ResponseEntity<DriverResponseDTO> update(
            @Parameter(description = "ID водія") @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody DriverCreateDTO dto) {
        log.info("PUT /api/drivers/{} - оновлення водія", id);

        DriverResponseDTO updated = driverService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити водія", description = "Видаляє водія з системи")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Водія видалено"),
            @ApiResponse(responseCode = "404", description = "Водія не знайдено")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID водія") @PathVariable Long id) {
        log.info("DELETE /api/drivers/{}", id);

        driverService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
