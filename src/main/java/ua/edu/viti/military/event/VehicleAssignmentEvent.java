package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.AssignmentType;

import java.time.LocalDateTime;

/**
 * Event що публікується при операціях з транспортом (призначення, зняття водія, відправка на ТО і т.д.)
 */
@Getter
public class VehicleAssignmentEvent extends ApplicationEvent {
    
    private final Long assignmentId;
    private final Long vehicleId;
    private final String vehicleRegistrationNumber;
    private final String vehicleModel;
    private final Long driverId;
    private final String driverName;
    private final AssignmentType assignmentType;
    private final String performedBy;
    private final String notes;
    private final LocalDateTime eventTime;
    
    public VehicleAssignmentEvent(Object source,
                                   Long assignmentId,
                                   Long vehicleId,
                                   String vehicleRegistrationNumber,
                                   String vehicleModel,
                                   Long driverId,
                                   String driverName,
                                   AssignmentType assignmentType,
                                   String performedBy,
                                   String notes) {
        super(source);
        this.assignmentId = assignmentId;
        this.vehicleId = vehicleId;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.vehicleModel = vehicleModel;
        this.driverId = driverId;
        this.driverName = driverName;
        this.assignmentType = assignmentType;
        this.performedBy = performedBy;
        this.notes = notes;
        this.eventTime = LocalDateTime.now();
    }
    
    /**
     * Опис операції українською
     */
    public String getOperationDescription() {
        return switch (assignmentType) {
            case ASSIGN_DRIVER -> "Призначення водія " + driverName + " на " + vehicleRegistrationNumber;
            case UNASSIGN_DRIVER -> "Зняття водія з " + vehicleRegistrationNumber;
            case SEND_TO_MAINTENANCE -> "Відправка " + vehicleRegistrationNumber + " на ТО";
            case RETURN_FROM_MAINTENANCE -> "Повернення " + vehicleRegistrationNumber + " з ТО";
            case DEPLOY -> "Відправка " + vehicleRegistrationNumber + " на завдання";
            case RETURN_FROM_DEPLOY -> "Повернення " + vehicleRegistrationNumber + " з завдання";
            case DECOMMISSION -> "Списання " + vehicleRegistrationNumber;
            case REACTIVATE -> "Відновлення " + vehicleRegistrationNumber;
        };
    }
}
