package ua.edu.viti.military.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCategoryResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String requiredLicense;
    private Integer maxLoadCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
