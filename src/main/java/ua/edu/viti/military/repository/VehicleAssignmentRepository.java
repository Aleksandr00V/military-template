package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.AssignmentType;
import ua.edu.viti.military.entity.VehicleAssignment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository для роботи з журналом операцій призначення транспорту
 */
@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignment, Long> {
    
    /**
     * Історія операцій для конкретного транспорту (новіші спочатку)
     */
    List<VehicleAssignment> findByVehicleIdOrderByPerformedAtDesc(Long vehicleId);
    
    /**
     * Історія операцій для конкретного водія (новіші спочатку)
     */
    List<VehicleAssignment> findByDriverIdOrderByPerformedAtDesc(Long driverId);
    
    /**
     * Операції певного типу
     */
    List<VehicleAssignment> findByAssignmentType(AssignmentType assignmentType);
    
    /**
     * Операції за період
     */
    List<VehicleAssignment> findByPerformedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Операції певного типу для транспорту
     */
    List<VehicleAssignment> findByVehicleIdAndAssignmentType(Long vehicleId, AssignmentType assignmentType);
    
    /**
     * Останні N операцій (для dashboard)
     */
    List<VehicleAssignment> findTop10ByOrderByPerformedAtDesc();
    
    /**
     * Підрахунок операцій певного типу за період
     */
    @Query("SELECT COUNT(a) FROM VehicleAssignment a " +
           "WHERE a.assignmentType = :type " +
           "AND a.performedAt BETWEEN :start AND :end")
    Long countByTypeAndPeriod(
        @Param("type") AssignmentType type,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    /**
     * Перевірка чи транспорт має активне призначення водія
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM VehicleAssignment a " +
           "WHERE a.vehicle.id = :vehicleId " +
           "AND a.assignmentType = 'ASSIGN_DRIVER' " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM VehicleAssignment ua " +
           "  WHERE ua.vehicle.id = :vehicleId " +
           "  AND ua.assignmentType = 'UNASSIGN_DRIVER' " +
           "  AND ua.performedAt > a.performedAt" +
           ")")
    boolean hasActiveDriverAssignment(@Param("vehicleId") Long vehicleId);
}
