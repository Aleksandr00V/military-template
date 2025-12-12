package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleAssignmentRequestDTO;
import ua.edu.viti.military.dto.response.VehicleAssignmentResponseDTO;
import ua.edu.viti.military.entity.*;
import ua.edu.viti.military.event.VehicleAssignmentEvent;
import ua.edu.viti.military.event.VehicleStatusChangedEvent;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.VehicleAssignmentMapper;
import ua.edu.viti.military.repository.DriverRepository;
import ua.edu.viti.military.repository.VehicleAssignmentRepository;
import ua.edu.viti.military.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервіс для управління операціями призначення транспорту.
 * Використовує транзакції з різними рівнями ізоляції для забезпечення цілісності даних.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleAssignmentService {
    
    private final VehicleAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleAssignmentMapper assignmentMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MetricsService metricsService;
    
    /**
     * Призначити водія на транспорт.
     * 
     * Транзакція з REPEATABLE_READ для запобігання phantom reads -
     * гарантує що стан транспорту не зміниться під час операції.
     */
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class
    )
    public VehicleAssignmentResponseDTO assignDriver(VehicleAssignmentRequestDTO dto) {
        long startTime = System.nanoTime();
        log.info("Assigning driver {} to vehicle {}", dto.getDriverId(), dto.getVehicleId());
        
        // 1. Перевірити що вказано водія
        if (dto.getDriverId() == null) {
            throw new BusinessLogicException("ID водія обов'язковий для призначення");
        }
        
        // 2. Знайти транспорт
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Транспорт", "id", dto.getVehicleId()));
        
        // 3. Знайти водія
        Driver driver = driverRepository.findById(dto.getDriverId())
            .orElseThrow(() -> new ResourceNotFoundException("Водій", "id", dto.getDriverId()));
        
        // 4. Бізнес-валідація
        validateAssignDriver(vehicle, driver);
        
        // 5. Оновити зв'язок
        vehicle.setDriver(driver);
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicleRepository.save(vehicle);
        
        // 6. Створити запис в журналі
        VehicleAssignment assignment = createAssignment(vehicle, driver, AssignmentType.ASSIGN_DRIVER, dto.getNotes());
        VehicleAssignment saved = assignmentRepository.save(assignment);
        
        // 7. Публікація події
        publishAssignmentEvent(saved, vehicle, driver);
        
        // 8. Метрики
        metricsService.recordVehicleOperation(AssignmentType.ASSIGN_DRIVER);
        metricsService.recordOperationDuration(startTime);
        
        log.info("Driver {} assigned to vehicle {} successfully. Assignment ID: {}", 
            driver.getId(), vehicle.getId(), saved.getId());
        
        return assignmentMapper.toResponseDTO(saved);
    }
    
    /**
     * Зняти водія з транспорту
     */
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class
    )
    public VehicleAssignmentResponseDTO unassignDriver(VehicleAssignmentRequestDTO dto) {
        log.info("Unassigning driver from vehicle {}", dto.getVehicleId());
        
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Транспорт", "id", dto.getVehicleId()));
        
        if (vehicle.getDriver() == null) {
            throw new BusinessLogicException("Транспорт не має призначеного водія");
        }
        
        Driver previousDriver = vehicle.getDriver();
        
        // Зняти водія
        vehicle.setDriver(null);
        vehicle.setStatus(VehicleStatus.IN_POOL);
        vehicleRepository.save(vehicle);
        
        // Запис в журнал
        VehicleAssignment assignment = createAssignment(vehicle, previousDriver, AssignmentType.UNASSIGN_DRIVER, dto.getNotes());
        VehicleAssignment saved = assignmentRepository.save(assignment);
        
        // Публікація події
        publishAssignmentEvent(saved, vehicle, previousDriver);
        
        log.info("Driver unassigned from vehicle {} successfully", vehicle.getId());
        
        return assignmentMapper.toResponseDTO(saved);
    }
    
    /**
     * Відправити транспорт на технічне обслуговування
     */
    @Transactional(rollbackFor = Exception.class)
    public VehicleAssignmentResponseDTO sendToMaintenance(VehicleAssignmentRequestDTO dto) {
        log.info("Sending vehicle {} to maintenance", dto.getVehicleId());
        
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Транспорт", "id", dto.getVehicleId()));
        
        if (vehicle.getStatus() == VehicleStatus.IN_MAINTENANCE) {
            throw new BusinessLogicException("Транспорт вже на технічному обслуговуванні");
        }
        
        if (vehicle.getStatus() == VehicleStatus.DECOMMISSIONED) {
            throw new BusinessLogicException("Не можна відправити списаний транспорт на ТО");
        }
        
        VehicleStatus previousStatus = vehicle.getStatus();
        
        // Оновити статус
        vehicle.setStatus(VehicleStatus.IN_MAINTENANCE);
        vehicleRepository.save(vehicle);
        
        // Запис в журнал
        VehicleAssignment assignment = createAssignment(vehicle, vehicle.getDriver(), AssignmentType.SEND_TO_MAINTENANCE, dto.getNotes());
        VehicleAssignment saved = assignmentRepository.save(assignment);
        
        // Публікація подій
        publishAssignmentEvent(saved, vehicle, vehicle.getDriver());
        publishStatusChangedEvent(vehicle, previousStatus, VehicleStatus.IN_MAINTENANCE);
        
        log.info("Vehicle {} sent to maintenance. Assignment ID: {}", vehicle.getId(), saved.getId());
        
        return assignmentMapper.toResponseDTO(saved);
    }
    
    /**
     * Повернути транспорт з технічного обслуговування
     */
    @Transactional(rollbackFor = Exception.class)
    public VehicleAssignmentResponseDTO returnFromMaintenance(VehicleAssignmentRequestDTO dto) {
        log.info("Returning vehicle {} from maintenance", dto.getVehicleId());
        
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Транспорт", "id", dto.getVehicleId()));
        
        if (vehicle.getStatus() != VehicleStatus.IN_MAINTENANCE) {
            throw new BusinessLogicException("Транспорт не на технічному обслуговуванні");
        }
        
        VehicleStatus previousStatus = vehicle.getStatus();
        VehicleStatus newStatus = vehicle.getDriver() != null ? VehicleStatus.ACTIVE : VehicleStatus.IN_POOL;
        
        // Оновити статус
        vehicle.setStatus(newStatus);
        vehicleRepository.save(vehicle);
        
        // Запис в журнал
        VehicleAssignment assignment = createAssignment(vehicle, vehicle.getDriver(), AssignmentType.RETURN_FROM_MAINTENANCE, dto.getNotes());
        VehicleAssignment saved = assignmentRepository.save(assignment);
        
        // Публікація подій
        publishAssignmentEvent(saved, vehicle, vehicle.getDriver());
        publishStatusChangedEvent(vehicle, previousStatus, newStatus);
        
        log.info("Vehicle {} returned from maintenance. Assignment ID: {}", vehicle.getId(), saved.getId());
        
        return assignmentMapper.toResponseDTO(saved);
    }
    
    /**
     * Списати транспорт
     */
    @Transactional(rollbackFor = Exception.class)
    public VehicleAssignmentResponseDTO decommission(VehicleAssignmentRequestDTO dto) {
        log.info("Decommissioning vehicle {}", dto.getVehicleId());
        
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Транспорт", "id", dto.getVehicleId()));
        
        if (vehicle.getStatus() == VehicleStatus.DECOMMISSIONED) {
            throw new BusinessLogicException("Транспорт вже списано");
        }
        
        Driver previousDriver = vehicle.getDriver();
        VehicleStatus previousStatus = vehicle.getStatus();
        
        // Зняти водія і списати
        vehicle.setDriver(null);
        vehicle.setStatus(VehicleStatus.DECOMMISSIONED);
        vehicleRepository.save(vehicle);
        
        // Запис в журнал
        VehicleAssignment assignment = createAssignment(vehicle, previousDriver, AssignmentType.DECOMMISSION, dto.getNotes());
        VehicleAssignment saved = assignmentRepository.save(assignment);
        
        // Публікація подій (критична зміна!)
        publishAssignmentEvent(saved, vehicle, previousDriver);
        publishStatusChangedEvent(vehicle, previousStatus, VehicleStatus.DECOMMISSIONED);
        
        log.info("Vehicle {} decommissioned. Assignment ID: {}", vehicle.getId(), saved.getId());
        
        return assignmentMapper.toResponseDTO(saved);
    }
    
    /**
     * Отримати історію операцій для транспорту
     */
    public List<VehicleAssignmentResponseDTO> getVehicleHistory(Long vehicleId) {
        return assignmentMapper.toResponseDTOList(
            assignmentRepository.findByVehicleIdOrderByPerformedAtDesc(vehicleId)
        );
    }
    
    /**
     * Отримати історію операцій для водія
     */
    public List<VehicleAssignmentResponseDTO> getDriverHistory(Long driverId) {
        return assignmentMapper.toResponseDTOList(
            assignmentRepository.findByDriverIdOrderByPerformedAtDesc(driverId)
        );
    }
    
    /**
     * Отримати останні операції (для dashboard)
     */
    public List<VehicleAssignmentResponseDTO> getRecentAssignments() {
        return assignmentMapper.toResponseDTOList(
            assignmentRepository.findTop10ByOrderByPerformedAtDesc()
        );
    }
    
    /**
     * Статистика операцій за період
     */
    public Long countAssignmentsByTypeAndPeriod(AssignmentType type, LocalDateTime start, LocalDateTime end) {
        return assignmentRepository.countByTypeAndPeriod(type, start, end);
    }
    
    // ==================== Private методи ====================
    
    private void validateAssignDriver(Vehicle vehicle, Driver driver) {
        // Перевірка статусу транспорту
        if (vehicle.getStatus() == VehicleStatus.DECOMMISSIONED) {
            throw new BusinessLogicException("Не можна призначити водія на списаний транспорт");
        }
        
        if (vehicle.getStatus() == VehicleStatus.IN_MAINTENANCE) {
            throw new BusinessLogicException("Не можна призначити водія на транспорт в ТО");
        }
        
        // Перевірка чи вже є водій
        if (vehicle.getDriver() != null) {
            throw new BusinessLogicException("Транспорт вже має призначеного водія: " + 
                vehicle.getDriver().getFirstName() + " " + vehicle.getDriver().getLastName());
        }
        
        // Перевірка активності водія
        if (!driver.getIsActive()) {
            throw new BusinessLogicException("Водій неактивний і не може бути призначений");
        }
        
        // Перевірка терміну дії посвідчення
        if (driver.getLicenseExpiryDate() != null && 
            driver.getLicenseExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessLogicException("Термін дії посвідчення водія закінчився");
        }
    }
    
    private VehicleAssignment createAssignment(Vehicle vehicle, Driver driver, AssignmentType type, String notes) {
        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setVehicle(vehicle);
        assignment.setDriver(driver);
        assignment.setAssignmentType(type);
        assignment.setNotes(notes);
        assignment.setPerformedBy(getCurrentUser());
        return assignment;
    }
    
    private String getCurrentUser() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
    
    // ==================== Event Publishing ====================
    
    /**
     * Публікація події про операцію з транспортом
     */
    private void publishAssignmentEvent(VehicleAssignment assignment, Vehicle vehicle, Driver driver) {
        String driverName = driver != null 
            ? driver.getFirstName() + " " + driver.getLastName() 
            : null;
        
        VehicleAssignmentEvent event = new VehicleAssignmentEvent(
            this,
            assignment.getId(),
            vehicle.getId(),
            vehicle.getRegistrationNumber(),
            vehicle.getModel(),
            driver != null ? driver.getId() : null,
            driverName,
            assignment.getAssignmentType(),
            assignment.getPerformedBy(),
            assignment.getNotes()
        );
        
        log.debug("Publishing VehicleAssignmentEvent: {}", event.getAssignmentType());
        eventPublisher.publishEvent(event);
    }
    
    /**
     * Публікація події про зміну статусу транспорту
     */
    private void publishStatusChangedEvent(Vehicle vehicle, VehicleStatus previousStatus, VehicleStatus newStatus) {
        VehicleStatusChangedEvent event = new VehicleStatusChangedEvent(
            this,
            vehicle.getId(),
            vehicle.getRegistrationNumber(),
            previousStatus,
            newStatus,
            getCurrentUser()
        );
        
        log.debug("Publishing VehicleStatusChangedEvent: {} -> {}", previousStatus, newStatus);
        eventPublisher.publishEvent(event);
    }
}
