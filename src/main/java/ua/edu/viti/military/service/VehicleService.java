package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleCreateDTO;
import ua.edu.viti.military.dto.request.VehicleUpdateDTO;
import ua.edu.viti.military.dto.response.VehicleCategoryResponseDTO;
import ua.edu.viti.military.dto.response.VehicleResponseDTO;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.FuelType;
import ua.edu.viti.military.entity.Vehicle;
import ua.edu.viti.military.entity.VehicleCategory;
import ua.edu.viti.military.entity.VehicleStatus;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
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

    // CREATE
    @Transactional
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

        // Створення Entity
        Vehicle vehicle = new Vehicle();
        vehicle.setModel(dto.getModel());
        vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        vehicle.setCategory(category);
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setChassisNumber(dto.getChassisNumber());
        vehicle.setManufactureYear(dto.getManufactureYear());
        vehicle.setMileage(dto.getMileage());
        vehicle.setFuelType(dto.getFuelType());
        vehicle.setFuelConsumption(dto.getFuelConsumption());
        vehicle.setMaintenanceIntervalKm(dto.getMaintenanceIntervalKm());
        vehicle.setLastMaintenanceDate(dto.getLastMaintenanceDate());
        vehicle.setLastMaintenanceMileage(dto.getLastMaintenanceMileage() != null ? dto.getLastMaintenanceMileage() : 0);
        vehicle.setDriver(driver);
        vehicle.setStatus(VehicleStatus.OPERATIONAL);

        // Збереження
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Транспорт створено з ID: {}", saved.getId());

        return toResponseDTO(saved);
    }

    // READ by ID
    public VehicleResponseDTO getById(Long id) {
        log.debug("Пошук транспорту з ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт з ID " + id + " не знайдено"));

        return toResponseDTO(vehicle);
    }

    // READ all with filters
    public List<VehicleResponseDTO> getAll(VehicleStatus status, Long categoryId) {
        log.debug("Отримання транспорту. Статус: {}, Категорія: {}", status, categoryId);

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

        return vehicles.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // READ vehicles requiring maintenance
    public List<VehicleResponseDTO> getVehiclesRequiringMaintenance() {
        log.debug("Пошук транспорту що потребує ТО");

        return vehicleRepository.findVehiclesRequiringMaintenance()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // READ by fuel type
    public List<VehicleResponseDTO> getByFuelType(FuelType fuelType) {
        log.debug("Пошук транспорту по типу палива: {}", fuelType);

        return vehicleRepository.findByFuelType(fuelType)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // READ by driver
    public List<VehicleResponseDTO> getByDriver(Long driverId) {
        log.debug("Пошук транспорту по водію: {}", driverId);

        return vehicleRepository.findByDriverId(driverId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public VehicleResponseDTO update(Long id, VehicleUpdateDTO dto) {
        log.info("Оновлення транспорту з ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт з ID " + id + " не знайдено"));

        // Оновлення полів
        if (dto.getModel() != null) {
            vehicle.setModel(dto.getModel());
        }
        if (dto.getMileage() != null) {
            vehicle.setMileage(dto.getMileage());
        }
        if (dto.getFuelConsumption() != null) {
            vehicle.setFuelConsumption(dto.getFuelConsumption());
        }
        if (dto.getMaintenanceIntervalKm() != null) {
            vehicle.setMaintenanceIntervalKm(dto.getMaintenanceIntervalKm());
        }
        if (dto.getLastMaintenanceDate() != null) {
            vehicle.setLastMaintenanceDate(dto.getLastMaintenanceDate());
        }
        if (dto.getLastMaintenanceMileage() != null) {
            vehicle.setLastMaintenanceMileage(dto.getLastMaintenanceMileage());
        }
        if (dto.getDriverId() != null) {
            Driver driver = driverRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Водія не знайдено"));

            if (!driver.getIsActive()) {
                throw new BusinessLogicException("Водій неактивний");
            }

            vehicle.setDriver(driver);
        }
        if (dto.getStatus() != null) {
            vehicle.setStatus(dto.getStatus());
        }

        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Транспорт з ID {} оновлено", id);

        return toResponseDTO(updated);
    }

    // DELETE (soft delete - зміна статусу на WRITTEN_OFF)
    @Transactional
    public void delete(Long id) {
        log.info("Списання транспорту з ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Транспорт з ID " + id + " не знайдено"));

        vehicle.setStatus(VehicleStatus.WRITTEN_OFF);
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

    // Маппінг Entity -> DTO
    private VehicleResponseDTO toResponseDTO(Vehicle entity) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setId(entity.getId());
        dto.setModel(entity.getModel());
        dto.setRegistrationNumber(entity.getRegistrationNumber());

        // Категорія
        dto.setCategory(toCategoryDTO(entity.getCategory()));

        dto.setEngineNumber(entity.getEngineNumber());
        dto.setChassisNumber(entity.getChassisNumber());
        dto.setManufactureYear(entity.getManufactureYear());
        dto.setMileage(entity.getMileage());
        dto.setFuelType(entity.getFuelType());
        dto.setFuelConsumption(entity.getFuelConsumption());
        dto.setMaintenanceIntervalKm(entity.getMaintenanceIntervalKm());
        dto.setLastMaintenanceDate(entity.getLastMaintenanceDate());
        dto.setLastMaintenanceMileage(entity.getLastMaintenanceMileage());

        // Водій - тільки ID та ім'я
        if (entity.getDriver() != null) {
            dto.setDriverId(entity.getDriver().getId());
            dto.setDriverName(entity.getDriver().getLastName() + " " + entity.getDriver().getFirstName());
        }

        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    // Маппінг Category -> CategoryDTO
    private VehicleCategoryResponseDTO toCategoryDTO(VehicleCategory entity) {
        VehicleCategoryResponseDTO dto = new VehicleCategoryResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setRequiredLicense(entity.getRequiredLicense());
        dto.setMaxLoadCapacity(entity.getMaxLoadCapacity());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
