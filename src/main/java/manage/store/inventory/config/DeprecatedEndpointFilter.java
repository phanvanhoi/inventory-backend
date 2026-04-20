package manage.store.inventory.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Option C (G0-1, 2026-04-19): Dual routing for /api/reports/*.
 * Old endpoint still works but responses get deprecation headers.
 * Sunset: 2026-07-19 (3 months after V19 deploy).
 */
@Component
public class DeprecatedEndpointFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DeprecatedEndpointFilter.class);
    private static final String SUNSET_DATE = "2026-07-19";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/reports")) {
            response.setHeader("X-Deprecated", "true");
            response.setHeader("X-Sunset-Date", SUNSET_DATE);
            response.setHeader("X-Replacement", "/api/orders");
            log.warn("Deprecated endpoint called: {} {} (sunset {})",
                    request.getMethod(), path, SUNSET_DATE);
        }
        chain.doFilter(request, response);
    }
}
