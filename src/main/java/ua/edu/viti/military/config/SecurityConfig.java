package ua.edu.viti.military.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ua.edu.viti.military.security.CustomUserDetailsService;
import ua.edu.viti.military.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Для @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Вимкнути CSRF (не потрібен для JWT)
            .csrf(csrf -> csrf.disable())
            
            // Stateless сесії (JWT)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Налаштування доступу до endpoints
            .authorizeHttpRequests(auth -> auth
                // Публічні endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // GET запити - дозволено для всіх автентифікованих
                .requestMatchers(HttpMethod.GET, "/api/vehicles/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/vehicle-categories/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/drivers/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/assignments/**").authenticated()
                
                // Операції з призначеннями - OPERATOR та ADMIN
                .requestMatchers("/api/assignments/**").hasAnyRole("OPERATOR", "ADMIN")
                
                // POST, PUT, DELETE - тільки OPERATOR та ADMIN
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("OPERATOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("OPERATOR", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("OPERATOR", "ADMIN")
                
                // DELETE - тільки ADMIN
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                
                // Решта - автентифіковані
                .anyRequest().authenticated()
            )
            
            // Додати JWT фільтр
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
