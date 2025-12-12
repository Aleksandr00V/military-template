package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.DriverCreateDTO;
import ua.edu.viti.military.dto.response.DriverResponseDTO;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.DriverMapper;
import ua.edu.viti.military.repository.DriverRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    /**
     * При створенні - очистити кеш списків
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "drivers", key = "'all'"),
        @CacheEvict(value = "drivers", key = "'active'")
    })
    public DriverResponseDTO create(DriverCreateDTO dto) {
        log.info("Створення нового водія: {}", dto.getMilitaryId());

        // Перевірка унікальності військового ID
        if (driverRepository.existsByMilitaryId(dto.getMilitaryId())) {
            throw new DuplicateResourceException("Водій з військовим ID " + dto.getMilitaryId() + " вже існує");
        }

        // Створення Entity через MapStruct
        Driver driver = driverMapper.toEntity(dto);
        
        // Встановити значення за замовчуванням якщо не вказано
        if (driver.getIsActive() == null) {
            driver.setIsActive(true);
        }

        // Збереження
        Driver saved = driverRepository.save(driver);
        log.info("Водія створено з ID: {}", saved.getId());

        return driverMapper.toResponseDTO(saved);
    }

    /**
     * Кешування по ID
     */
    @Cacheable(value = "drivers", key = "#id")
    public DriverResponseDTO getById(Long id) {
        log.info("Fetching driver from DATABASE: id={}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Водія з ID " + id + " не знайдено"));

        return driverMapper.toResponseDTO(driver);
    }

    /**
     * Кешування списку всіх водіїв
     */
    @Cacheable(value = "drivers", key = "'all'")
    public List<DriverResponseDTO> getAll() {
        log.info("Fetching ALL drivers from DATABASE");

        return driverMapper.toResponseDTOList(driverRepository.findAll());
    }

    /**
     * Кешування списку активних водіїв
     */
    @Cacheable(value = "drivers", key = "'active'")
    public List<DriverResponseDTO> getActiveDrivers() {
        log.info("Fetching ACTIVE drivers from DATABASE");

        return driverMapper.toResponseDTOList(driverRepository.findByIsActive(true));
    }

    // Не кешуємо - результат залежить від поточної дати
    public List<DriverResponseDTO> getDriversWithExpiringLicense(int daysUntilExpiry) {
        log.debug("Пошук водіїв з правами що закінчуються через {} днів", daysUntilExpiry);

        LocalDate expiryDate = LocalDate.now().plusDays(daysUntilExpiry);

        return driverMapper.toResponseDTOList(driverRepository.findByLicenseExpiryDateBefore(expiryDate));
    }

    /**
     * При оновленні - очистити кеш водія та списків
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "drivers", key = "#id"),
        @CacheEvict(value = "drivers", key = "'all'"),
        @CacheEvict(value = "drivers", key = "'active'")
    })
    public DriverResponseDTO update(Long id, DriverCreateDTO dto) {
        log.info("Оновлення водія з ID: {}", id);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Водія з ID " + id + " не знайдено"));

        // Оновлення полів через MapStruct (тільки non-null поля)
        driverMapper.updateEntityFromDTO(dto, driver);

        Driver updated = driverRepository.save(driver);
        log.info("Водія з ID {} оновлено", id);

        return driverMapper.toResponseDTO(updated);
    }

    /**
     * При видаленні - очистити весь кеш водіїв
     */
    @Transactional
    @CacheEvict(value = "drivers", allEntries = true)
    public void delete(Long id) {
        log.info("Видалення водія з ID: {}", id);

        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Водія з ID " + id + " не знайдено");
        }

        driverRepository.deleteById(id);
        log.info("Водія з ID {} видалено", id);
    }
}
