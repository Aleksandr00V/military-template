package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Event що публікується коли транспорт потребує технічного обслуговування
 */
@Getter
public class MaintenanceRequiredEvent extends ApplicationEvent {
    
    private final Long vehicleId;
    private final String vehicleRegistrationNumber;
    private final String vehicleModel;
    private final Integer currentMileage;
    private final Integer lastMaintenanceMileage;
    private final Integer maintenanceIntervalKm;
    private final Integer kmSinceLastMaintenance;
    private final LocalDateTime eventTime;
    
    public MaintenanceRequiredEvent(Object source,
                                     Long vehicleId,
                                     String vehicleRegistrationNumber,
                                     String vehicleModel,
                                     Integer currentMileage,
                                     Integer lastMaintenanceMileage,
                                     Integer maintenanceIntervalKm) {
        super(source);
        this.vehicleId = vehicleId;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.vehicleModel = vehicleModel;
        this.currentMileage = currentMileage;
        this.lastMaintenanceMileage = lastMaintenanceMileage;
        this.maintenanceIntervalKm = maintenanceIntervalKm;
        this.kmSinceLastMaintenance = currentMileage - lastMaintenanceMileage;
        this.eventTime = LocalDateTime.now();
    }
    
    /**
     * Відсоток перевищення інтервалу ТО
     */
    public int getOverduePercentage() {
        if (maintenanceIntervalKm == null || maintenanceIntervalKm == 0) return 0;
        return (kmSinceLastMaintenance * 100) / maintenanceIntervalKm;
    }
}
