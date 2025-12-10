package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.DriverCreateDTO;
import ua.edu.viti.military.dto.response.DriverResponseDTO;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.DriverRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;

    // CREATE
    @Transactional
    public DriverResponseDTO create(DriverCreateDTO dto) {
        log.info("Створення нового водія: {}", dto.getMilitaryId());

        // Перевірка унікальності військового ID
        if (driverRepository.existsByMilitaryId(dto.getMilitaryId())) {
            throw new DuplicateResourceException("Водій з військовим ID " + dto.getMilitaryId() + " вже існує");
        }

        // Створення Entity
        Driver driver = new Driver();
        driver.setMilitaryId(dto.getMilitaryId());
        driver.setFirstName(dto.getFirstName());
        driver.setLastName(dto.getLastName());
        driver.setMiddleName(dto.getMiddleName());
        driver.setRank(dto.getRank());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setLicenseCategories(dto.getLicenseCategories());
        driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        driver.setPhoneNumber(dto.getPhoneNumber());
        driver.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // Збереження
        Driver saved = driverRepository.save(driver);
        log.info("Водія створено з ID: {}", saved.getId());

        return toResponseDTO(saved);
    }

    // READ by ID
    public DriverResponseDTO getById(Long id) {
        log.debug("Пошук водія з ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Водія з ID " + id + " не знайдено"));

        return toResponseDTO(driver);
    }

    // READ all
    public List<DriverResponseDTO> getAll() {
        log.debug("Отримання всіх водіїв");

        return driverRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // READ active drivers
    public List<DriverResponseDTO> getActiveDrivers() {
        log.debug("Отримання активних водіїв");

        return driverRepository.findByIsActive(true)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // READ drivers with expiring license
    public List<DriverResponseDTO> getDriversWithExpiringLicense(int daysUntilExpiry) {
        log.debug("Пошук водіїв з правами що закінчуються через {} днів", daysUntilExpiry);

        LocalDate expiryDate = LocalDate.now().plusDays(daysUntilExpiry);

        return driverRepository.findByLicenseExpiryDateBefore(expiryDate)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public DriverResponseDTO update(Long id, DriverCreateDTO dto) {
        log.info("Оновлення водія з ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Водія з ID " + id + " не знайдено"));

        // Оновлення полів
        if (dto.getFirstName() != null) {
            driver.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            driver.setLastName(dto.getLastName());
        }
        if (dto.getMiddleName() != null) {
            driver.setMiddleName(dto.getMiddleName());
        }
        if (dto.getRank() != null) {
            driver.setRank(dto.getRank());
        }
        if (dto.getLicenseNumber() != null) {
            driver.setLicenseNumber(dto.getLicenseNumber());
        }
        if (dto.getLicenseCategories() != null) {
            driver.setLicenseCategories(dto.getLicenseCategories());
        }
        if (dto.getLicenseExpiryDate() != null) {
            driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        }
        if (dto.getPhoneNumber() != null) {
            driver.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getIsActive() != null) {
            driver.setIsActive(dto.getIsActive());
        }

        Driver updated = driverRepository.save(driver);
        log.info("Водія з ID {} оновлено", id);

        return toResponseDTO(updated);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        log.info("Видалення водія з ID: {}", id);

        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Водія з ID " + id + " не знайдено");
        }

        driverRepository.deleteById(id);
        log.info("Водія з ID {} видалено", id);
    }

    // Маппінг Entity -> DTO
    private DriverResponseDTO toResponseDTO(Driver entity) {
        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setId(entity.getId());
        dto.setMilitaryId(entity.getMilitaryId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setRank(entity.getRank());
        dto.setLicenseNumber(entity.getLicenseNumber());
        dto.setLicenseCategories(entity.getLicenseCategories());
        dto.setLicenseExpiryDate(entity.getLicenseExpiryDate());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
