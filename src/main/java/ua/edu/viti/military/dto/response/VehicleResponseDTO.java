package ua.edu.viti.military.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.entity.FuelType;
import ua.edu.viti.military.entity.VehicleStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDTO {
    private Long id;
    private String model;
    private String registrationNumber;

    // Вкладена CategoryDTO замість об'єкту Category
    private VehicleCategoryResponseDTO category;

    private String engineNumber;
    private String chassisNumber;
    private Integer manufactureYear;
    private Integer mileage;
    private FuelType fuelType;
    private Double fuelConsumption;
    private Integer maintenanceIntervalKm;
    private LocalDate lastMaintenanceDate;
    private Integer lastMaintenanceMileage;

    // Тільки ID та ім'я водія, без повного об'єкту
    private Long driverId;
    private String driverName;

    private VehicleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
