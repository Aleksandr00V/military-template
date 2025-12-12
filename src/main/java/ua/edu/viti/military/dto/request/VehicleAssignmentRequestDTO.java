package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ua.edu.viti.military.entity.AssignmentType;

/**
 * DTO для створення запису про призначення транспорту
 */
@Data
public class VehicleAssignmentRequestDTO {
    
    @NotNull(message = "ID транспорту обов'язковий")
    @Positive(message = "ID транспорту має бути позитивним")
    private Long vehicleId;
    
    /**
     * ID водія (обов'язковий для ASSIGN_DRIVER, опційний для інших)
     */
    @Positive(message = "ID водія має бути позитивним")
    private Long driverId;
    
    @NotNull(message = "Тип операції обов'язковий")
    private AssignmentType assignmentType;
    
    @Size(max = 500, message = "Примітки не можуть перевищувати 500 символів")
    private String notes;
}
