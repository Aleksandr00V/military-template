package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleCategoryCreateDTO;
import ua.edu.viti.military.dto.response.VehicleCategoryResponseDTO;
import ua.edu.viti.military.entity.VehicleCategory;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.VehicleCategoryMapper;
import ua.edu.viti.military.repository.VehicleCategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;
    private final VehicleCategoryMapper categoryMapper;

    /**
     * При створенні - очистити кеш списку всіх категорій
     */
    @Transactional
    @CacheEvict(value = "vehicleCategories", key = "'all'")
    public VehicleCategoryResponseDTO create(VehicleCategoryCreateDTO dto) {
        log.info("Створення нової категорії: {}", dto.getName());

        // Перевірка унікальності коду
        if (categoryRepository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("Категорія з кодом " + dto.getCode() + " вже існує");
        }

        // Перевірка унікальності назви
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Категорія з назвою " + dto.getName() + " вже існує");
        }

        // Створення Entity через MapStruct
        VehicleCategory category = categoryMapper.toEntity(dto);

        // Збереження
        VehicleCategory saved = categoryRepository.save(category);
        log.info("Категорію створено з ID: {}", saved.getId());

        return categoryMapper.toResponseDTO(saved);
    }

    /**
     * Кешування по ID.
     * Ключ: vehicleCategories::1 (де 1 - це id)
     * Логування відбувається тільки при cache miss
     */
    @Cacheable(value = "vehicleCategories", key = "#id")
    public VehicleCategoryResponseDTO getById(Long id) {
        log.info("Fetching category from DATABASE: id={}", id);

        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + id + " не знайдено"));

        return categoryMapper.toResponseDTO(category);
    }

    /**
     * Кешування списку всіх категорій.
     * Ключ: vehicleCategories::all
     */
    @Cacheable(value = "vehicleCategories", key = "'all'")
    public List<VehicleCategoryResponseDTO> getAll() {
        log.info("Fetching ALL categories from DATABASE");

        return categoryMapper.toResponseDTOList(categoryRepository.findAll());
    }

    /**
     * При оновленні - очистити кеш конкретної категорії та списку
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "vehicleCategories", key = "#id"),
        @CacheEvict(value = "vehicleCategories", key = "'all'")
    })
    public VehicleCategoryResponseDTO update(Long id, VehicleCategoryCreateDTO dto) {
        log.info("Оновлення категорії з ID: {}", id);

        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + id + " не знайдено"));

        // Оновлення полів через MapStruct (тільки non-null поля)
        categoryMapper.updateEntityFromDTO(dto, category);

        VehicleCategory updated = categoryRepository.save(category);
        log.info("Категорію з ID {} оновлено", id);

        return categoryMapper.toResponseDTO(updated);
    }

    /**
     * При видаленні - очистити весь кеш категорій
     */
    @Transactional
    @CacheEvict(value = "vehicleCategories", allEntries = true)
    public void delete(Long id) {
        log.info("Видалення категорії з ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категорію з ID " + id + " не знайдено");
        }

        categoryRepository.deleteById(id);
        log.info("Категорію з ID {} видалено", id);
    }
}
