package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleCreateDTO;
import ua.edu.viti.military.dto.request.VehicleUpdateDTO;
import ua.edu.viti.military.dto.response.VehicleResponseDTO;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.FuelType;
import ua.edu.viti.military.entity.Vehicle;
import ua.edu.viti.military.entity.VehicleCategory;
import ua.edu.viti.military.entity.VehicleStatus;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.VehicleMapper;
import ua.edu.viti.military.repository.DriverRepository;
import ua.edu.viti.military.repository.VehicleCategoryRepository;
import ua.edu.viti.military.repository.VehicleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository categoryRepository;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;

    /**
     * При створенні - очистити кеш списку транспорту
     */
    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleResponseDTO create(VehicleCreateDTO dto) {
        log.info("Створення нового транспорту: {}", dto.getRegistrationNumber());

        // Перевірка унікальності реєстраційного номера
        if (vehicleRepository.existsByRegistrationNumber(dto.getRegistrationNumber())) {
            throw new DuplicateResourceException("Транспорт з номером " + dto.getRegistrationNumber() + " вже існує");
        }

        // Знайти категорію
        VehicleCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + dto.getCategoryId() + " не знайдено"));

        // Знайти водія (якщо вказано)
        Driver driver = null;
        if (dto.getDriverId() != null) {
            driver = driverRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Водія з ID " + dto.getDriverId() + " не знайдено"));

            // Перевірка чи водій активний
            if (!driver.getIsActive()) {
                throw new BusinessLogicException("Водій " + driver.getLastName() + " неактивний. Не можна призначити на транспорт.");
            }
        }

        // Створення Entity через MapStruct
        Vehicle vehicle = vehicleMapper.toEntity(dto);
        
        // Встановити поля що не мапляться автоматично
        vehicle.setCategory(category);
        vehicle.setDriver(driver);
        vehicle.setStatus(driver != null ? VehicleStatus.ACTIVE : VehicleStatus.IN_POOL);
        if (vehicle.getLastMaintenanceMileage() == null) {
            vehicle.setLastMaintenanceMileage(0);
        }

        // Збереження
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Транспорт створено з ID: {}", saved.getId());

        return vehicleMapper.toResponseDTO(saved);
    }

    /**
     * Кешування по ID
     */
    @Cacheable(value = "vehicles", key = "#id")
    public VehicleResponseDTO getById(Long id) {
        log.info("Fetching vehicle from DATABASE: id={}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт з ID " + id + " не знайдено"));

        return vehicleMapper.toResponseDTO(vehicle);
    }

    /**
     * Кешування списку (без фільтрів)
     * При наявності фільтрів - не кешуємо
     */
    @Cacheable(value = "vehicles", key = "'all'", condition = "#status == null and #categoryId == null")
    public List<VehicleResponseDTO> getAll(VehicleStatus status, Long categoryId) {
        log.info("Fetching vehicles from DATABASE. Status: {}, Category: {}", status, categoryId);

        List<Vehicle> vehicles;

        if (status != null && categoryId != null) {
            vehicles = vehicleRepository.findByStatusWithCategory(status)
                    .stream()
                    .filter(v -> v.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        } else if (status != null) {
            vehicles = vehicleRepository.findByStatus(status);
        } else if (categoryId != null) {
            vehicles = vehicleRepository.findByCategoryId(categoryId);
        } else {
            vehicles = vehicleRepository.findAll();
        }

        return vehicleMapper.toResponseDTOList(vehicles);
    }

    // Не кешуємо - динамічний запит
    public List<VehicleResponseDTO> getVehiclesRequiringMaintenance() {
        log.debug("Пошук транспорту що потребує ТО");

        return vehicleMapper.toResponseDTOList(vehicleRepository.findVehiclesRequiringMaintenance());
    }

    /**
     * Кешування по типу палива
     */
    @Cacheable(value = "vehicles", key = "'fuelType::' + #fuelType.name()")
    public List<VehicleResponseDTO> getByFuelType(FuelType fuelType) {
        log.info("Fetching vehicles by fuel type from DATABASE: {}", fuelType);

        return vehicleMapper.toResponseDTOList(vehicleRepository.findByFuelType(fuelType));
    }

    // Не кешуємо - залежить від призначень
    public List<VehicleResponseDTO> getByDriver(Long driverId) {
        log.debug("Пошук транспорту по водію: {}", driverId);

        return vehicleMapper.toResponseDTOList(vehicleRepository.findByDriverId(driverId));
    }

    /**
     * При оновленні - очистити весь кеш транспорту
     */
    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleResponseDTO update(Long id, VehicleUpdateDTO dto) {
        log.info("Оновлення транспорту з ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт з ID " + id + " не знайдено"));

        // Оновлення полів через MapStruct (тільки non-null поля)
        vehicleMapper.updateEntityFromDTO(dto, vehicle);

        // Окрема обробка водія (потрібна бізнес-логіка)
        if (dto.getDriverId() != null) {
            Driver driver = driverRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Водія не знайдено"));

            if (!driver.getIsActive()) {
                throw new BusinessLogicException("Водій неактивний");
            }

            vehicle.setDriver(driver);
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Транспорт з ID {} оновлено", id);

        return vehicleMapper.toResponseDTO(updated);
    }

    /**
     * При видаленні - очистити кеш
     */
    @Transactional
    @CacheEvict(value = "vehicles", allEntries = true)
    public void delete(Long id) {
        log.info("Списання транспорту з ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт з ID " + id + " не знайдено"));

        vehicle.setStatus(VehicleStatus.DECOMMISSIONED);
        vehicle.setDriver(null); // Знімаємо водія зі списаної машини

        vehicleRepository.save(vehicle);
        log.info("Транспорт з ID {} списано", id);
    }

    // Перевірка статусу ТО
    public void validateMaintenanceStatus(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт не знайдено"));

        if (vehicle.getMaintenanceIntervalKm() != null && vehicle.getLastMaintenanceMileage() != null) {
            int kmSinceLastMaintenance = vehicle.getMileage() - vehicle.getLastMaintenanceMileage();

            if (kmSinceLastMaintenance >= vehicle.getMaintenanceIntervalKm()) {
                throw new BusinessLogicException(
                        String.format("Транспорт %s потребує ТО! Пробіг після останнього ТО: %d км",
                                vehicle.getRegistrationNumber(), kmSinceLastMaintenance)
                );
            }
        }
    }
}
