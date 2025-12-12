package ua.edu.viti.military.mapper;

import org.mapstruct.*;
import ua.edu.viti.military.dto.response.VehicleAssignmentResponseDTO;
import ua.edu.viti.military.entity.Driver;
import ua.edu.viti.military.entity.VehicleAssignment;

import java.util.List;

/**
 * MapStruct маппер для конвертації VehicleAssignment Entity в DTO.
 */
@Mapper(componentModel = "spring")
public interface VehicleAssignmentMapper {
    
    /**
     * Конвертує Entity в ResponseDTO.
     * Маппінг вкладених об'єктів Vehicle та Driver.
     */
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.registrationNumber", target = "vehicleRegistrationNumber")
    @Mapping(source = "vehicle.model", target = "vehicleModel")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver", target = "driverName", qualifiedByName = "driverToFullName")
    @Mapping(source = "driver.licenseNumber", target = "driverLicenseNumber")
    @Mapping(target = "assignmentTypeDescription", ignore = true) // Обчислюється в DTO getter
    VehicleAssignmentResponseDTO toResponseDTO(VehicleAssignment entity);
    
    /**
     * Конвертує список Entity в список ResponseDTO
     */
    List<VehicleAssignmentResponseDTO> toResponseDTOList(List<VehicleAssignment> entities);
    
    /**
     * Кастомний метод для отримання повного імені водія
     */
    @Named("driverToFullName")
    default String driverToFullName(Driver driver) {
        if (driver == null) {
            return null;
        }
        return driver.getFirstName() + " " + driver.getLastName();
    }
}
