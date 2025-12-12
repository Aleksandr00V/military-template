package ua.edu.viti.military.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import ua.edu.viti.military.dto.request.VehicleCategoryCreateDTO;
import ua.edu.viti.military.dto.response.VehicleCategoryResponseDTO;
import ua.edu.viti.military.entity.VehicleCategory;

import java.util.List;

/**
 * MapStruct маппер для конвертації між VehicleCategory Entity та DTO.
 * 
 * componentModel = "spring" - реєструє маппер як Spring Bean
 */
@Mapper(componentModel = "spring")
public interface VehicleCategoryMapper {
    
    /**
     * Конвертує Entity в ResponseDTO
     */
    VehicleCategoryResponseDTO toResponseDTO(VehicleCategory entity);
    
    /**
     * Конвертує список Entity в список ResponseDTO
     */
    List<VehicleCategoryResponseDTO> toResponseDTOList(List<VehicleCategory> entities);
    
    /**
     * Конвертує CreateDTO в Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehicleCategory toEntity(VehicleCategoryCreateDTO dto);
    
    /**
     * Оновлює існуючу Entity з DTO (тільки non-null поля)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(VehicleCategoryCreateDTO dto, @MappingTarget VehicleCategory entity);
}
