package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.LoginRequestDTO;
import ua.edu.viti.military.dto.request.RegisterRequestDTO;
import ua.edu.viti.military.dto.response.JwtResponseDTO;
import ua.edu.viti.military.entity.Role;
import ua.edu.viti.military.entity.RoleName;
import ua.edu.viti.military.entity.User;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.RoleRepository;
import ua.edu.viti.military.repository.UserRepository;
import ua.edu.viti.military.security.JwtUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final MetricsService metricsService;
    
    /**
     * Автентифікація користувача
     */
    public JwtResponseDTO login(LoginRequestDTO dto) {
        long startTime = System.nanoTime();
        log.info("Login attempt for user: {}", dto.getUsername());
        
        try {
            // 1. Автентифікація
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );
            
            // 2. Встановити в Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 3. Генерувати JWT
            String jwt = jwtUtils.generateToken(authentication);
            
            // 4. Отримати деталі користувача
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));
            
            List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            
            // 5. Метрики успішного входу
            metricsService.recordLoginSuccess();
            metricsService.recordAuthenticationDuration(startTime);
            
            log.info("User {} logged in successfully", dto.getUsername());
            
            return new JwtResponseDTO(jwt, user.getUsername(), user.getEmail(), roles);
        } catch (Exception e) {
            metricsService.recordLoginFailure();
            throw e;
        }
    }
    
    /**
     * Реєстрація нового користувача
     */
    @Transactional
    public JwtResponseDTO register(RegisterRequestDTO dto) {
        log.info("Registration attempt for user: {}", dto.getUsername());
        
        // 1. Перевірка на дублікати
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username вже зайнятий: " + dto.getUsername());
        }
        
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email вже зайнятий: " + dto.getEmail());
        }
        
        // 2. Створити користувача
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setMilitaryRank(dto.getMilitaryRank());
        user.setEnabled(true);
        
        // 3. Призначити ролі
        Set<Role> roles = new HashSet<>();
        
        if (dto.getRoles() == null || dto.getRoles().isEmpty()) {
            // За замовчуванням - VIEWER
            Role viewerRole = roleRepository.findByName(RoleName.ROLE_VIEWER)
                .orElseThrow(() -> new ResourceNotFoundException("Роль ROLE_VIEWER не знайдена"));
            roles.add(viewerRole);
        } else {
            for (String roleName : dto.getRoles()) {
                try {
                    RoleName roleEnum = RoleName.valueOf(roleName);
                    Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new ResourceNotFoundException("Роль не знайдена: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new ResourceNotFoundException("Невідома роль: " + roleName);
                }
            }
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        // Метрика реєстрації
        metricsService.recordRegistration();
        
        log.info("User {} registered successfully", dto.getUsername());
        
        // 4. Автоматичний login після реєстрації
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);
        
        List<String> roleNames = roles.stream()
            .map(role -> role.getName().name())
            .toList();
        
        return new JwtResponseDTO(jwt, user.getUsername(), user.getEmail(), roleNames);
    }
    
    /**
     * Отримати поточного автентифікованого користувача
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
