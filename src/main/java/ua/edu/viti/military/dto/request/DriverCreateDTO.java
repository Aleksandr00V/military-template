package ua.edu.viti.military.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.validation.OnCreate;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для створення/оновлення водія")
public class DriverCreateDTO {

    @Schema(description = "Військовий ідентифікатор", example = "МВ-001", maxLength = 50)
    @NotBlank(groups = OnCreate.class, message = "Військовий ID обов'язковий")
    @Size(max = 50, message = "Військовий ID не може бути довшим за 50 символів")
    private String militaryId;

    @Schema(description = "Ім'я водія", example = "Іван", maxLength = 50)
    @Size(max = 50, message = "Ім'я не може бути довшим за 50 символів")
    private String firstName;

    @Schema(description = "Прізвище водія", example = "Петренко", maxLength = 50)
    @Size(max = 50, message = "Прізвище не може бути довшим за 50 символів")
    private String lastName;

    @Schema(description = "По батькові", example = "Васильович", maxLength = 50)
    @Size(max = 50, message = "По батькові не може бути довшим за 50 символів")
    private String middleName;

    @Schema(description = "Військове звання", example = "Сержант", maxLength = 50)
    @Size(max = 50, message = "Звання не може бути довшим за 50 символів")
    private String rank;

    @Schema(description = "Номер водійського посвідчення", example = "АВС123456", maxLength = 50)
    @Size(max = 50, message = "Номер посвідчення не може бути довшим за 50 символів")
    private String licenseNumber;

    @Schema(description = "Категорії водійських прав", example = "B, C, D", maxLength = 50)
    @Size(max = 50, message = "Категорії прав не можуть бути довшими за 50 символів")
    private String licenseCategories;

    @Schema(description = "Дата закінчення дії посвідчення", example = "2026-12-31", type = "string", format = "date")
    private LocalDate licenseExpiryDate;

    @Schema(description = "Номер телефону", example = "+380501234567", maxLength = 20)
    @Size(max = 20, message = "Номер телефону не може бути довшим за 20 символів")
    private String phoneNumber;

    @Schema(description = "Чи активний водій", example = "true", defaultValue = "true")
    private Boolean isActive = true;
}
