package ua.edu.viti.military.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDTO {
    private Long id;
    private String militaryId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String rank;
    private String licenseNumber;
    private String licenseCategories;
    private LocalDate licenseExpiryDate;
    private String phoneNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
