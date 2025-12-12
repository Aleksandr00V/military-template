package ua.edu.viti.military.entity;

/**
 * Типи операцій призначення транспорту
 */
public enum AssignmentType {
    ASSIGN_DRIVER,      // Призначення водія на транспорт
    UNASSIGN_DRIVER,    // Зняття водія з транспорту
    SEND_TO_MAINTENANCE,// Відправка на технічне обслуговування
    RETURN_FROM_MAINTENANCE, // Повернення з ТО
    DEPLOY,             // Відправка на завдання
    RETURN_FROM_DEPLOY, // Повернення з завдання
    DECOMMISSION,       // Списання
    REACTIVATE          // Повернення в експлуатацію
}
