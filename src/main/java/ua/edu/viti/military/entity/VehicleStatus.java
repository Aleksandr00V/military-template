package ua.edu.viti.military.entity;

/**
 * Статуси транспортного засобу
 */
public enum VehicleStatus {
    ACTIVE,           // Активний (з призначеним водієм)
    IN_POOL,          // В резерві (без водія, готовий до призначення)
    IN_MAINTENANCE,   // На технічному обслуговуванні
    OUT_OF_SERVICE,   // Несправний
    DECOMMISSIONED    // Списаний
}
