package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.entity.VehicleStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUpdateDTO {



    @Size(max = 100, message = "Модель не може бути довшою за 100 символів")
    private String model;

    @PositiveOrZero(message = "Пробіг не може бути від'ємним")
    private Integer mileage;

    @Positive(message = "Витрата палива має бути позитивною")
    private Double fuelConsumption;

    @Positive(message = "Інтервал ТО має бути позитивним")
    private Integer maintenanceIntervalKm;

    private LocalDate lastMaintenanceDate;

    @PositiveOrZero(message = "Пробіг на останньому ТО не може бути від'ємним")
    private Integer lastMaintenanceMileage;

    private Long driverId;

    private VehicleStatus status;
}
