package ua.edu.viti.military.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import ua.edu.viti.military.dto.request.DriverCreateDTO;
import ua.edu.viti.military.dto.response.DriverResponseDTO;
import ua.edu.viti.military.entity.Driver;

import java.util.List;

/**
 * MapStruct маппер для конвертації між Driver Entity та DTO.
 */
@Mapper(componentModel = "spring")
public interface DriverMapper {
    
    /**
     * Конвертує Entity в ResponseDTO
     */
    DriverResponseDTO toResponseDTO(Driver entity);
    
    /**
     * Конвертує список Entity в список ResponseDTO
     */
    List<DriverResponseDTO> toResponseDTOList(List<Driver> entities);
    
    /**
     * Конвертує CreateDTO в Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Driver toEntity(DriverCreateDTO dto);
    
    /**
     * Оновлює існуючу Entity з DTO (тільки non-null поля)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "militaryId", ignore = true) // Не можна змінити military ID
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(DriverCreateDTO dto, @MappingTarget Driver entity);
}
