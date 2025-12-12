package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.LoginRequestDTO;
import ua.edu.viti.military.dto.request.RegisterRequestDTO;
import ua.edu.viti.military.dto.response.JwtResponseDTO;
import ua.edu.viti.military.dto.response.MessageResponseDTO;
import ua.edu.viti.military.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API для автентифікації та реєстрації")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "Вхід користувача", description = "Автентифікація та отримання JWT токена")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        log.info("Login request for user: {}", dto.getUsername());
        JwtResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Реєстрація користувача", description = "Створення нового облікового запису")
    public ResponseEntity<JwtResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        log.info("Registration request for user: {}", dto.getUsername());
        JwtResponseDTO response = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Поточний користувач", description = "Отримати ім'я поточного користувача")
    public ResponseEntity<MessageResponseDTO> getCurrentUser() {
        String username = authService.getCurrentUsername();
        return ResponseEntity.ok(new MessageResponseDTO("Поточний користувач: " + username));
    }
}
