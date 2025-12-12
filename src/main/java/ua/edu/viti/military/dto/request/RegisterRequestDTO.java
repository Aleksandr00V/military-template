package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequestDTO {
    
    @NotBlank(message = "Username обов'язковий")
    @Size(min = 3, max = 50, message = "Username має бути від 3 до 50 символів")
    private String username;
    
    @NotBlank(message = "Email обов'язковий")
    @Email(message = "Некоректний email")
    private String email;
    
    @NotBlank(message = "Пароль обов'язковий")
    @Size(min = 6, message = "Пароль має бути мінімум 6 символів")
    private String password;
    
    @NotBlank(message = "Повне ім'я обов'язкове")
    @Size(max = 100)
    private String fullName;
    
    @Size(max = 50)
    private String militaryRank;
    
    private Set<String> roles;  // Назви ролей: ["ROLE_OPERATOR"]
}
