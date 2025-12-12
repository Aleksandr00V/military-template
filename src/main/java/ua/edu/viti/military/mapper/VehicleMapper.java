package ua.edu.viti.military.mapper;

import org.mapstruct.*;
import ua.edu.viti.military.dto.request.VehicleCreateDTO;
import ua.edu.viti.military.dto.request.VehicleUpdateDTO;
import ua.edu.viti.military.dto.response.VehicleResponseDTO;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.Vehicle;

import java.util.List;

/**
 * MapStruct маппер для конвертації між Vehicle Entity та DTO.
 * 
 * uses = {VehicleCategoryMapper.class} - використовує VehicleCategoryMapper 
 * для маппінгу вкладеного об'єкта category
 */
@Mapper(componentModel = "spring", uses = {VehicleCategoryMapper.class})
public interface VehicleMapper {
    
    /**
     * Конвертує Entity в ResponseDTO.
     * Маппінг вкладених об'єктів:
     * - category → VehicleCategoryResponseDTO (через VehicleCategoryMapper)
     * - driver.id → driverId
     * - driver name → driverName (потребує кастомного маппінгу)
     */
    @Mapping(source = "category", target = "category")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver", target = "driverName", qualifiedByName = "driverToFullName")
    VehicleResponseDTO toResponseDTO(Vehicle entity);
    
    /**
     * Конвертує список Entity в список ResponseDTO
     */
    List<VehicleResponseDTO> toResponseDTOList(List<Vehicle> entities);
    
    /**
     * Конвертує CreateDTO в Entity.
     * Поля category та driver встановлюються окремо в Service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(VehicleCreateDTO dto);
    
    /**
     * Оновлює існуючу Entity з UpdateDTO (тільки non-null поля)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationNumber", ignore = true) // Не можна змінити номер
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "driver", ignore = true) // Driver встановлюється окремо
    @Mapping(target = "engineNumber", ignore = true)
    @Mapping(target = "chassisNumber", ignore = true)
    @Mapping(target = "manufactureYear", ignore = true)
    @Mapping(target = "fuelType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(VehicleUpdateDTO dto, @MappingTarget Vehicle entity);
    
    /**
     * Кастомний метод для отримання повного імені водія
     */
    @Named("driverToFullName")
    default String driverToFullName(Driver driver) {
        if (driver == null) {
            return null;
        }
        return driver.getLastName() + " " + driver.getFirstName();
    }
}
