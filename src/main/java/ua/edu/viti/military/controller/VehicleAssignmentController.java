package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.VehicleAssignmentRequestDTO;
import ua.edu.viti.military.dto.response.VehicleAssignmentResponseDTO;
import ua.edu.viti.military.entity.AssignmentType;
import ua.edu.viti.military.service.VehicleAssignmentService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST контролер для управління операціями призначення транспорту.
 * Надає endpoint-и для призначення/зняття водіїв, відправки на ТО, списання тощо.
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Assignments", description = "API для операцій призначення транспорту")
public class VehicleAssignmentController {
    
    private final VehicleAssignmentService assignmentService;
    
    /**
     * Призначити водія на транспорт
     */
    @PostMapping("/assign-driver")
    @Operation(summary = "Призначити водія на транспорт")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Водія успішно призначено"),
        @ApiResponse(responseCode = "400", description = "Помилка валідації або бізнес-логіки"),
        @ApiResponse(responseCode = "404", description = "Транспорт або водій не знайдено")
    })
    public ResponseEntity<VehicleAssignmentResponseDTO> assignDriver(
            @Valid @RequestBody VehicleAssignmentRequestDTO dto) {
        
        log.info("REST request to assign driver {} to vehicle {}", dto.getDriverId(), dto.getVehicleId());
        VehicleAssignmentResponseDTO result = assignmentService.assignDriver(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Зняти водія з транспорту
     */
    @PostMapping("/unassign-driver")
    @Operation(summary = "Зняти водія з транспорту")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Водія успішно знято"),
        @ApiResponse(responseCode = "400", description = "Транспорт не має водія"),
        @ApiResponse(responseCode = "404", description = "Транспорт не знайдено")
    })
    public ResponseEntity<VehicleAssignmentResponseDTO> unassignDriver(
            @Valid @RequestBody VehicleAssignmentRequestDTO dto) {
        
        log.info("REST request to unassign driver from vehicle {}", dto.getVehicleId());
        VehicleAssignmentResponseDTO result = assignmentService.unassignDriver(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Відправити транспорт на технічне обслуговування
     */
    @PostMapping("/send-to-maintenance")
    @Operation(summary = "Відправити транспорт на ТО")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Транспорт відправлено на ТО"),
        @ApiResponse(responseCode = "400", description = "Транспорт вже на ТО або списаний"),
        @ApiResponse(responseCode = "404", description = "Транспорт не знайдено")
    })
    public ResponseEntity<VehicleAssignmentResponseDTO> sendToMaintenance(
            @Valid @RequestBody VehicleAssignmentRequestDTO dto) {
        
        log.info("REST request to send vehicle {} to maintenance", dto.getVehicleId());
        VehicleAssignmentResponseDTO result = assignmentService.sendToMaintenance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Повернути транспорт з технічного обслуговування
     */
    @PostMapping("/return-from-maintenance")
    @Operation(summary = "Повернути транспорт з ТО")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Транспорт повернуто з ТО"),
        @ApiResponse(responseCode = "400", description = "Транспорт не на ТО"),
        @ApiResponse(responseCode = "404", description = "Транспорт не знайдено")
    })
    public ResponseEntity<VehicleAssignmentResponseDTO> returnFromMaintenance(
            @Valid @RequestBody VehicleAssignmentRequestDTO dto) {
        
        log.info("REST request to return vehicle {} from maintenance", dto.getVehicleId());
        VehicleAssignmentResponseDTO result = assignmentService.returnFromMaintenance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Списати транспорт
     */
    @PostMapping("/decommission")
    @Operation(summary = "Списати транспорт")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Транспорт списано"),
        @ApiResponse(responseCode = "400", description = "Транспорт вже списано"),
        @ApiResponse(responseCode = "404", description = "Транспорт не знайдено")
    })
    public ResponseEntity<VehicleAssignmentResponseDTO> decommission(
            @Valid @RequestBody VehicleAssignmentRequestDTO dto) {
        
        log.info("REST request to decommission vehicle {}", dto.getVehicleId());
        VehicleAssignmentResponseDTO result = assignmentService.decommission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Історія операцій для транспорту
     */
    @GetMapping("/vehicle/{vehicleId}/history")
    @Operation(summary = "Отримати історію операцій для транспорту")
    public ResponseEntity<List<VehicleAssignmentResponseDTO>> getVehicleHistory(
            @Parameter(description = "ID транспорту")
            @PathVariable Long vehicleId) {
        
        log.info("REST request to get assignment history for vehicle {}", vehicleId);
        List<VehicleAssignmentResponseDTO> history = assignmentService.getVehicleHistory(vehicleId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Історія операцій для водія
     */
    @GetMapping("/driver/{driverId}/history")
    @Operation(summary = "Отримати історію операцій для водія")
    public ResponseEntity<List<VehicleAssignmentResponseDTO>> getDriverHistory(
            @Parameter(description = "ID водія")
            @PathVariable Long driverId) {
        
        log.info("REST request to get assignment history for driver {}", driverId);
        List<VehicleAssignmentResponseDTO> history = assignmentService.getDriverHistory(driverId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Останні операції (для dashboard)
     */
    @GetMapping("/recent")
    @Operation(summary = "Отримати останні 10 операцій")
    public ResponseEntity<List<VehicleAssignmentResponseDTO>> getRecentAssignments() {
        
        log.info("REST request to get recent assignments");
        List<VehicleAssignmentResponseDTO> recent = assignmentService.getRecentAssignments();
        return ResponseEntity.ok(recent);
    }
    
    /**
     * Статистика операцій за період
     */
    @GetMapping("/statistics/count")
    @Operation(summary = "Підрахувати кількість операцій за період")
    public ResponseEntity<Long> countAssignments(
            @Parameter(description = "Тип операції")
            @RequestParam AssignmentType type,
            
            @Parameter(description = "Початок періоду (ISO формат)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            
            @Parameter(description = "Кінець періоду (ISO формат)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        log.info("REST request to count assignments of type {} between {} and {}", type, start, end);
        Long count = assignmentService.countAssignmentsByTypeAndPeriod(type, start, end);
        return ResponseEntity.ok(count);
    }
}
