package ua.edu.viti.military.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Журнал операцій призначення транспортних засобів.
 * Зберігає історію всіх операцій: призначення водіїв, ТО, завдання.
 */
@Entity
@Table(name = "vehicle_assignments")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Транспортний засіб
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    /**
     * Водій (може бути null для операцій без водія)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;
    
    /**
     * Тип операції
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AssignmentType assignmentType;
    
    /**
     * Примітки до операції
     */
    @Column(length = 500)
    private String notes;
    
    /**
     * Хто виконав операцію (username)
     */
    @Column(name = "performed_by", length = 100)
    private String performedBy;
    
    /**
     * Дата та час виконання операції
     */
    @CreatedDate
    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;
    
    /**
     * Optimistic Locking - запобігає конфліктам при одночасних оновленнях
     */
    @Version
    private Long version;
}
