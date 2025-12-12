package ua.edu.viti.military.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Асинхронний обробник подій для військового автопарку.
 * Використовує @TransactionalEventListener для гарантованої обробки після коміту транзакції.
 */
@Slf4j
@Component
public class VehicleEventListener {
    
    /**
     * Обробка події призначення/операції з транспортом.
     * Виконується асинхронно після успішного коміту транзакції.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVehicleAssignmentEvent(VehicleAssignmentEvent event) {
        log.info("========== VEHICLE ASSIGNMENT EVENT ==========");
        log.info("Operation: {}", event.getOperationDescription());
        log.info("Assignment ID: {}", event.getAssignmentId());
        log.info("Vehicle: {} ({})", event.getVehicleRegistrationNumber(), event.getVehicleModel());
        log.info("Driver ID: {}, Name: {}", event.getDriverId(), event.getDriverName());
        log.info("Type: {}", event.getAssignmentType());
        log.info("Performed by: {}", event.getPerformedBy());
        log.info("Notes: {}", event.getNotes());
        log.info("Event time: {}", event.getEventTime());
        log.info("Thread: {}", Thread.currentThread().getName());
        log.info("==============================================");
        
        // Тут можна додати додаткову логіку:
        // - Відправка email/SMS повідомлень
        // - Запис в аудит-лог
        // - Інтеграція з зовнішніми системами
        // - Оновлення статистики
        
        simulateAsyncProcessing("assignment processing");
    }
    
    /**
     * Обробка події зміни статусу транспорту.
     * Критичні зміни (списання) логуються з підвищеним рівнем.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVehicleStatusChangedEvent(VehicleStatusChangedEvent event) {
        if (event.isCritical()) {
            log.warn("========== CRITICAL STATUS CHANGE ==========");
        } else {
            log.info("========== VEHICLE STATUS CHANGE ==========");
        }
        
        log.info("Vehicle: {} (ID: {})", event.getVehicleRegistrationNumber(), event.getVehicleId());
        log.info("Status change: {} -> {}", event.getPreviousStatus(), event.getNewStatus());
        log.info("Changed by: {}", event.getChangedBy());
        log.info("Event time: {}", event.getEventTime());
        log.info("Critical: {}", event.isCritical());
        log.info("Thread: {}", Thread.currentThread().getName());
        log.info("=============================================");
        
        // При списанні можна ініціювати додаткові процеси
        if (event.isCritical()) {
            log.warn("Vehicle {} has been DECOMMISSIONED - initiating decommission procedures", 
                    event.getVehicleRegistrationNumber());
            simulateAsyncProcessing("decommission notification");
        }
    }
    
    /**
     * Обробка події потреби технічного обслуговування.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMaintenanceRequiredEvent(MaintenanceRequiredEvent event) {
        log.warn("========== MAINTENANCE REQUIRED ==========");
        log.warn("Vehicle: {} ({}) ID: {}", 
                event.getVehicleRegistrationNumber(), 
                event.getVehicleModel(), 
                event.getVehicleId());
        log.warn("Current mileage: {} km", event.getCurrentMileage());
        log.warn("Last maintenance at: {} km", event.getLastMaintenanceMileage());
        log.warn("Km since last maintenance: {} km", event.getKmSinceLastMaintenance());
        log.warn("Maintenance interval: {} km", event.getMaintenanceIntervalKm());
        log.warn("Overdue: {}%", event.getOverduePercentage());
        log.warn("Event time: {}", event.getEventTime());
        log.warn("Thread: {}", Thread.currentThread().getName());
        log.warn("==========================================");
        
        // Тут можна:
        // - Створити заявку на ТО
        // - Повідомити відповідальних осіб
        // - Автоматично заблокувати можливість призначення
        
        simulateAsyncProcessing("maintenance alert processing");
    }
    
    /**
     * Симуляція асинхронної обробки (для демонстрації).
     */
    private void simulateAsyncProcessing(String processName) {
        try {
            log.debug("Starting async {}", processName);
            Thread.sleep(100); // Симуляція обробки
            log.debug("Completed async {}", processName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async processing interrupted: {}", processName);
        }
    }
}
