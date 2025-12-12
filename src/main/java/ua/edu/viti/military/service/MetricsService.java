package ua.edu.viti.military.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.edu.viti.military.entity.AssignmentType;

import java.util.concurrent.TimeUnit;

/**
 * Сервіс для збору метрик через Micrometer.
 * Інтегрується з Prometheus/Grafana через /actuator/metrics
 */
@Service
@Slf4j
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    // Counters для операцій з транспортом
    private final Counter vehicleAssignedCounter;
    private final Counter vehicleUnassignedCounter;
    private final Counter vehicleMaintenanceCounter;
    private final Counter vehicleDecommissionedCounter;
    
    // Counters для авторизації
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter registrationCounter;
    
    // Timers для вимірювання часу операцій
    private final Timer assignmentOperationTimer;
    private final Timer authenticationTimer;
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Ініціалізація counters для операцій з транспортом
        this.vehicleAssignedCounter = Counter.builder("military.vehicle.assigned")
            .description("Кількість призначень водіїв на транспорт")
            .tag("type", "assign")
            .register(meterRegistry);
        
        this.vehicleUnassignedCounter = Counter.builder("military.vehicle.unassigned")
            .description("Кількість знять водіїв з транспорту")
            .tag("type", "unassign")
            .register(meterRegistry);
        
        this.vehicleMaintenanceCounter = Counter.builder("military.vehicle.maintenance")
            .description("Кількість відправок на ТО")
            .tag("type", "maintenance")
            .register(meterRegistry);
        
        this.vehicleDecommissionedCounter = Counter.builder("military.vehicle.decommissioned")
            .description("Кількість списань транспорту")
            .tag("type", "decommission")
            .register(meterRegistry);
        
        // Ініціалізація counters для авторизації
        this.loginSuccessCounter = Counter.builder("military.auth.login")
            .description("Успішні входи в систему")
            .tag("result", "success")
            .register(meterRegistry);
        
        this.loginFailureCounter = Counter.builder("military.auth.login")
            .description("Невдалі спроби входу")
            .tag("result", "failure")
            .register(meterRegistry);
        
        this.registrationCounter = Counter.builder("military.auth.registration")
            .description("Кількість реєстрацій")
            .register(meterRegistry);
        
        // Ініціалізація timers
        this.assignmentOperationTimer = Timer.builder("military.operation.duration")
            .description("Час виконання операцій з транспортом")
            .tag("operation", "assignment")
            .register(meterRegistry);
        
        this.authenticationTimer = Timer.builder("military.auth.duration")
            .description("Час виконання автентифікації")
            .register(meterRegistry);
    }
    
    // ==================== Vehicle Operations ====================
    
    /**
     * Реєструє операцію з транспортом
     */
    public void recordVehicleOperation(AssignmentType type) {
        switch (type) {
            case ASSIGN_DRIVER -> vehicleAssignedCounter.increment();
            case UNASSIGN_DRIVER -> vehicleUnassignedCounter.increment();
            case SEND_TO_MAINTENANCE, RETURN_FROM_MAINTENANCE -> vehicleMaintenanceCounter.increment();
            case DECOMMISSION -> vehicleDecommissionedCounter.increment();
            default -> log.debug("Operation type {} not tracked separately", type);
        }
        log.debug("Recorded vehicle operation: {}", type);
    }
    
    /**
     * Вимірює час операції з транспортом
     */
    public void recordOperationDuration(long startTimeNanos) {
        long duration = System.nanoTime() - startTimeNanos;
        assignmentOperationTimer.record(duration, TimeUnit.NANOSECONDS);
        log.debug("Operation duration: {} ms", TimeUnit.NANOSECONDS.toMillis(duration));
    }
    
    // ==================== Authentication ====================
    
    /**
     * Реєструє успішний вхід
     */
    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
        log.debug("Recorded successful login");
    }
    
    /**
     * Реєструє невдалу спробу входу
     */
    public void recordLoginFailure() {
        loginFailureCounter.increment();
        log.debug("Recorded failed login attempt");
    }
    
    /**
     * Реєструє нову реєстрацію
     */
    public void recordRegistration() {
        registrationCounter.increment();
        log.debug("Recorded new registration");
    }
    
    /**
     * Вимірює час автентифікації
     */
    public void recordAuthenticationDuration(long startTimeNanos) {
        long duration = System.nanoTime() - startTimeNanos;
        authenticationTimer.record(duration, TimeUnit.NANOSECONDS);
    }
    
    // ==================== Custom Gauges ====================
    
    /**
     * Створює gauge для активних транспортних засобів
     */
    public <T extends Number> void registerActiveVehiclesGauge(T number) {
        meterRegistry.gauge("military.vehicles.active", number);
    }
    
    /**
     * Створює gauge для активних водіїв
     */
    public <T extends Number> void registerActiveDriversGauge(T number) {
        meterRegistry.gauge("military.drivers.active", number);
    }
}
