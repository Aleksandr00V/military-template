package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.VehicleStatus;

import java.time.LocalDateTime;

/**
 * Event що публікується при зміні статусу транспортного засобу
 */
@Getter
public class VehicleStatusChangedEvent extends ApplicationEvent {
    
    private final Long vehicleId;
    private final String vehicleRegistrationNumber;
    private final VehicleStatus previousStatus;
    private final VehicleStatus newStatus;
    private final String changedBy;
    private final LocalDateTime eventTime;
    
    public VehicleStatusChangedEvent(Object source,
                                      Long vehicleId,
                                      String vehicleRegistrationNumber,
                                      VehicleStatus previousStatus,
                                      VehicleStatus newStatus,
                                      String changedBy) {
        super(source);
        this.vehicleId = vehicleId;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.eventTime = LocalDateTime.now();
    }
    
    /**
     * Чи є зміна критичною (наприклад, списання)
     */
    public boolean isCritical() {
        return newStatus == VehicleStatus.DECOMMISSIONED;
    }
}
