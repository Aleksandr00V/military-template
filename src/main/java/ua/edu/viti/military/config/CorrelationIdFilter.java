package ua.edu.viti.military.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Фільтр для додавання Correlation ID до кожного запиту.
 * Це дозволяє трейсити запити через всі логи.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_URI_MDC_KEY = "requestUri";
    public static final String REQUEST_METHOD_MDC_KEY = "requestMethod";
    public static final String USER_MDC_KEY = "userId";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Отримати або згенерувати Correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString().substring(0, 8);
            }
            
            // Додати в MDC для логування
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put(REQUEST_URI_MDC_KEY, request.getRequestURI());
            MDC.put(REQUEST_METHOD_MDC_KEY, request.getMethod());
            
            // Додати користувача якщо автентифікований
            if (request.getUserPrincipal() != null) {
                MDC.put(USER_MDC_KEY, request.getUserPrincipal().getName());
            }
            
            // Додати Correlation ID у відповідь
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            filterChain.doFilter(request, response);
            
        } finally {
            // Очистити MDC після запиту
            MDC.clear();
        }
    }
}
