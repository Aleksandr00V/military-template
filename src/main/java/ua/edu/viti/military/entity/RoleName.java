package ua.edu.viti.military.entity;

/**
 * Enum для ролей користувачів системи.
 * Префікс ROLE_ вимагається Spring Security.
 */
public enum RoleName {
    ROLE_ADMIN,      // Повний доступ: створення, редагування, видалення
    ROLE_OPERATOR,   // Операції: призначення водіїв, ТО, списання
    ROLE_VIEWER      // Тільки перегляд
}
