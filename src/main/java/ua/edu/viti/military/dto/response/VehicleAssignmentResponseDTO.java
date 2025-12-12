package ua.edu.viti.military.dto.response;

import lombok.Data;
import ua.edu.viti.military.entity.AssignmentType;

import java.time.LocalDateTime;

/**
 * DTO для відповіді з інформацією про призначення
 */
@Data
public class VehicleAssignmentResponseDTO {
    
    private Long id;
    
    // Інформація про транспорт
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String vehicleModel;
    
    // Інформація про водія (може бути null)
    private Long driverId;
    private String driverName;
    private String driverLicenseNumber;
    
    // Деталі операції
    private AssignmentType assignmentType;
    private String assignmentTypeDescription;
    private String notes;
    private String performedBy;
    private LocalDateTime performedAt;
    
    /**
     * Отримати опис типу операції українською
     */
    public String getAssignmentTypeDescription() {
        if (assignmentType == null) return null;
        
        return switch (assignmentType) {
            case ASSIGN_DRIVER -> "Призначення водія";
            case UNASSIGN_DRIVER -> "Зняття водія";
            case SEND_TO_MAINTENANCE -> "Відправка на ТО";
            case RETURN_FROM_MAINTENANCE -> "Повернення з ТО";
            case DEPLOY -> "Відправка на завдання";
            case RETURN_FROM_DEPLOY -> "Повернення з завдання";
            case DECOMMISSION -> "Списання";
            case REACTIVATE -> "Повернення в експлуатацію";
        };
    }
}
