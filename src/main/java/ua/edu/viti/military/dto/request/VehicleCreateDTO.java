package ua.edu.viti.military.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.entity.FuelType;
import ua.edu.viti.military.validation.OnCreate;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для створення нового транспортного засобу")
public class VehicleCreateDTO {

    @Schema(description = "Модель транспорту", example = "КамАЗ-5320", maxLength = 100)
    @NotBlank(groups = OnCreate.class, message = "Модель обов'язкова")
    @Size(max = 100, message = "Модель не може бути довшою за 100 символів")
    private String model;

    @Schema(description = "Реєстраційний номер", example = "AA1234BB", maxLength = 20)
    @NotBlank(groups = OnCreate.class, message = "Реєстраційний номер обов'язковий")
    @Size(max = 20, message = "Реєстраційний номер не може бути довшим за 20 символів")
    private String registrationNumber;

    @Schema(description = "ID категорії транспорту", example = "1", minimum = "1")
    @NotNull(groups = OnCreate.class, message = "Категорія обов'язкова")
    @Positive(message = "ID категорії має бути позитивним")
    private Long categoryId;

    @Schema(description = "Номер двигуна", example = "740.10-1000400", maxLength = 50)
    @Size(max = 50, message = "Номер двигуна не може бути довшим за 50 символів")
    private String engineNumber;

    @Schema(description = "Номер шасі", example = "XTC532000Y1234567", maxLength = 50)
    @Size(max = 50, message = "Номер шасі не може бути довшим за 50 символів")
    private String chassisNumber;

    @Schema(description = "Рік випуску", example = "2020")
    private Integer manufactureYear;

    @Schema(description = "Поточний пробіг (км)", example = "50000", minimum = "0")
    @NotNull(groups = OnCreate.class, message = "Пробіг обов'язковий")
    @PositiveOrZero(message = "Пробіг не може бути від'ємним")
    private Integer mileage;

    @Schema(description = "Тип палива", example = "DIESEL", allowableValues = {"DIESEL", "PETROL", "HYBRID", "ELECTRIC"})
    @NotNull(groups = OnCreate.class, message = "Тип палива обов'язковий")
    private FuelType fuelType;

    @Schema(description = "Витрата палива (л/100км)", example = "25.5", minimum = "0")
    @Positive(message = "Витрата палива має бути позитивною")
    private Double fuelConsumption;

    @Schema(description = "Інтервал ТО (км)", example = "10000", minimum = "1")
    @Positive(message = "Інтервал ТО має бути позитивним")
    private Integer maintenanceIntervalKm;

    @Schema(description = "Дата останнього ТО", example = "2024-06-15", type = "string", format = "date")
    private LocalDate lastMaintenanceDate;

    @Schema(description = "Пробіг на останньому ТО (км)", example = "45000", minimum = "0")
    @PositiveOrZero(message = "Пробіг на останньому ТО не може бути від'ємним")
    private Integer lastMaintenanceMileage;

    @Schema(description = "ID призначеного водія (опційно)", example = "1")
    private Long driverId;
}
