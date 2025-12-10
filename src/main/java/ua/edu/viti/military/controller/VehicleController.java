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
import ua.edu.viti.military.dto.request.VehicleCreateDTO;
import ua.edu.viti.military.dto.request.VehicleUpdateDTO;
import ua.edu.viti.military.dto.response.VehicleResponseDTO;
import ua.edu.viti.military.entity.FuelType;
import ua.edu.viti.military.entity.VehicleStatus;
import ua.edu.viti.military.service.VehicleService;
import ua.edu.viti.military.validation.OnCreate;
import ua.edu.viti.military.validation.OnUpdate;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicles", description = "API для управління транспортними засобами")
public class VehicleController {

    private final VehicleService vehicleService;

    // CREATE
    @PostMapping
    @Operation(summary = "Створити транспорт", description = "Реєструє новий транспортний засіб")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Транспорт створено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "404", description = "Категорію або водія не знайдено"),
            @ApiResponse(responseCode = "409", description = "Транспорт з таким номером вже існує")
    })
    public ResponseEntity<VehicleResponseDTO> create(
            @Validated(OnCreate.class) @RequestBody VehicleCreateDTO dto) {
        log.info("POST /api/vehicles - створення транспорту: {}", dto.getRegistrationNumber());

        VehicleResponseDTO created = vehicleService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ by ID
    @GetMapping("/{id}")
    @Operation(summary = "Отримати транспорт за ID", description = "Повертає транспорт за вказаним ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Транспорт знайдено"),
            @ApiResponse(responseCode = "404", description = "Транспорт не знайдено")
    })
    public ResponseEntity<VehicleResponseDTO> getById(
            @Parameter(description = "ID транспорту") @PathVariable Long id) {
        log.info("GET /api/vehicles/{}", id);

        VehicleResponseDTO vehicle = vehicleService.getById(id);

        return ResponseEntity.ok(vehicle);
    }

    // READ all with filters
    @GetMapping
    @Operation(summary = "Отримати транспорт", description = "Повертає список транспорту з можливістю фільтрації")
    @ApiResponse(responseCode = "200", description = "Список транспорту")
    public ResponseEntity<List<VehicleResponseDTO>> getAll(
            @Parameter(description = "Фільтр по статусу")
            @RequestParam(required = false) VehicleStatus status,
            @Parameter(description = "Фільтр по категорії")
            @RequestParam(required = false) Long categoryId) {
        log.info("GET /api/vehicles - status: {}, categoryId: {}", status, categoryId);

        List<VehicleResponseDTO> vehicles = vehicleService.getAll(status, categoryId);

        return ResponseEntity.ok(vehicles);
    }

    // READ vehicles requiring maintenance
    @GetMapping("/requiring-maintenance")
    @Operation(summary = "Транспорт що потребує ТО",
            description = "Повертає транспорт, який потребує технічного обслуговування")
    @ApiResponse(responseCode = "200", description = "Список транспорту")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesRequiringMaintenance() {
        log.info("GET /api/vehicles/requiring-maintenance");

        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesRequiringMaintenance();

        return ResponseEntity.ok(vehicles);
    }

    // READ by fuel type
    @GetMapping("/by-fuel-type/{fuelType}")
    @Operation(summary = "Транспорт по типу палива", description = "Повертає транспорт за типом палива")
    @ApiResponse(responseCode = "200", description = "Список транспорту")
    public ResponseEntity<List<VehicleResponseDTO>> getByFuelType(
            @Parameter(description = "Тип палива") @PathVariable FuelType fuelType) {
        log.info("GET /api/vehicles/by-fuel-type/{}", fuelType);

        List<VehicleResponseDTO> vehicles = vehicleService.getByFuelType(fuelType);

        return ResponseEntity.ok(vehicles);
    }

    // READ by driver
    @GetMapping("/by-driver/{driverId}")
    @Operation(summary = "Транспорт водія", description = "Повертає транспорт закріплений за водієм")
    @ApiResponse(responseCode = "200", description = "Список транспорту")
    public ResponseEntity<List<VehicleResponseDTO>> getByDriver(
            @Parameter(description = "ID водія") @PathVariable Long driverId) {
        log.info("GET /api/vehicles/by-driver/{}", driverId);

        List<VehicleResponseDTO> vehicles = vehicleService.getByDriver(driverId);

        return ResponseEntity.ok(vehicles);
    }

    // UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Оновити транспорт", description = "Оновлює дані транспортного засобу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Транспорт оновлено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані або бізнес-правило порушено"),
            @ApiResponse(responseCode = "404", description = "Транспорт не знайдено")
    })
    public ResponseEntity<VehicleResponseDTO> update(
            @Parameter(description = "ID транспорту") @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody VehicleUpdateDTO dto) {
        log.info("PUT /api/vehicles/{} - оновлення транспорту", id);

        VehicleResponseDTO updated = vehicleService.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    // DELETE (soft delete - списання)
    @DeleteMapping("/{id}")
    @Operation(summary = "Списати транспорт",
            description = "Списує транспорт (змінює статус на WRITTEN_OFF)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Транспорт списано"),
            @ApiResponse(responseCode = "404", description = "Транспорт не знайдено")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID транспорту") @PathVariable Long id) {
        log.info("DELETE /api/vehicles/{} - списання транспорту", id);

        vehicleService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
