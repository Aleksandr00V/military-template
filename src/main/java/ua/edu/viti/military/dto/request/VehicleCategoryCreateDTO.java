package ua.edu.viti.military.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.validation.OnCreate;
import ua.edu.viti.military.validation.OnUpdate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для створення/оновлення категорії транспорту")
public class VehicleCategoryCreateDTO {

    @Schema(description = "Назва категорії", example = "Вантажні автомобілі", maxLength = 100)
    @NotBlank(groups = OnCreate.class, message = "Назва категорії обов'язкова")
    @Size(max = 100, groups = {OnCreate.class, OnUpdate.class}, message = "Назва не може бути довшою за 100 символів")
    private String name;

    @Schema(description = "Унікальний код категорії", example = "TRUCK", maxLength = 20)
    @NotBlank(groups = OnCreate.class, message = "Код категорії обов'язковий")
    @Size(max = 20, groups = {OnCreate.class, OnUpdate.class}, message = "Код не може бути довшим за 20 символів")
    private String code;

    @Schema(description = "Опис категорії", example = "Транспорт для перевезення вантажів", maxLength = 500)
    @Size(max = 500, groups = {OnCreate.class, OnUpdate.class}, message = "Опис не може бути довшим за 500 символів")
    private String description;

    @Schema(description = "Необхідна категорія водійських прав", example = "C", maxLength = 20)
    @Size(max = 20, groups = {OnCreate.class, OnUpdate.class}, message = "Категорія прав не може бути довшою за 20 символів")
    private String requiredLicense;

    @Schema(description = "Максимальна вантажопідйомність (кг)", example = "5000", minimum = "1")
    @Positive(groups = {OnCreate.class, OnUpdate.class}, message = "Вантажопідйомність має бути позитивною")
    private Integer maxLoadCapacity;
}
