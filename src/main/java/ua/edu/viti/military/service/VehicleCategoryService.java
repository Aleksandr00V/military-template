package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.VehicleCategoryCreateDTO;
import ua.edu.viti.military.dto.response.VehicleCategoryResponseDTO;
import ua.edu.viti.military.entity.VehicleCategory;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.VehicleCategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehicleCategoryService {

    private final VehicleCategoryRepository categoryRepository;

    // CREATE
    @Transactional
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

        // Створення Entity
        VehicleCategory category = new VehicleCategory();
        category.setName(dto.getName());
        category.setCode(dto.getCode());
        category.setDescription(dto.getDescription());
        category.setRequiredLicense(dto.getRequiredLicense());
        category.setMaxLoadCapacity(dto.getMaxLoadCapacity());

        // Збереження
        VehicleCategory saved = categoryRepository.save(category);
        log.info("Категорію створено з ID: {}", saved.getId());

        return toResponseDTO(saved);
    }

    public VehicleCategoryResponseDTO getById(Long id) {
        log.debug("Пошук категорії з ID: {}", id);

        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + id + " не знайдено"));

        return toResponseDTO(category);
    }

    // READ all
    public List<VehicleCategoryResponseDTO> getAll() {
        log.debug("Отримання всіх категорій");

        return categoryRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Transactional
    public VehicleCategoryResponseDTO update(Long id, VehicleCategoryCreateDTO dto) {
        log.info("Оновлення категорії з ID: {}", id);

        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + id + " не знайдено"));

        // Оновлення полів
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getCode() != null) {
            category.setCode(dto.getCode());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getRequiredLicense() != null) {
            category.setRequiredLicense(dto.getRequiredLicense());
        }
        if (dto.getMaxLoadCapacity() != null) {
            category.setMaxLoadCapacity(dto.getMaxLoadCapacity());
        }

        VehicleCategory updated = categoryRepository.save(category);
        log.info("Категорію з ID {} оновлено", id);

        return toResponseDTO(updated);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        log.info("Видалення категорії з ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категорію з ID " + id + " не знайдено");
        }

        categoryRepository.deleteById(id);
        log.info("Категорію з ID {} видалено", id);
    }

    // Маппінг Entity -> DTO
    private VehicleCategoryResponseDTO toResponseDTO(VehicleCategory entity) {
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
