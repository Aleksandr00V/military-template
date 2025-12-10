package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.FuelType;
import ua.edu.viti.military.entity.Vehicle;
import ua.edu.viti.military.entity.VehicleStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByCategoryId(Long categoryId);
    // шукає по статусу
    List<Vehicle> findByStatus(VehicleStatus status);
    // підрахунок по статусу
    long countByStatus(VehicleStatus status);

    List<Vehicle> findByDriverId(Long driverId);
    List<Vehicle> findByFuelType(FuelType fuelType);

    @Query("SELECT v FROM Vehicle v WHERE (v.mileage - v.lastMaintenanceMileage) >= v.maintenanceIntervalKm")
    List<Vehicle> findVehiclesRequiringMaintenance();

    // пошук за діапазону пробігу
    List<Vehicle> findByMileageBetween(Integer minMileage, Integer maxMileage);

    // пошук по категорії
    @Query("SELECT v FROM Vehicle v JOIN FETCH v.category WHERE v.status = :status")
    List<Vehicle> findByStatusWithCategory(@Param("status") VehicleStatus status);
}
