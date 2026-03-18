package manage.store.inventory.security;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Token hợp lệ - set Authentication vào SecurityContext")
    void doFilter_validToken_setsAuthentication() throws Exception {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(1L);
        when(jwtUtil.getRolesFromToken(token)).thenReturn(List.of("ADMIN", "USER"));

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        assertEquals(1L, principal.getUserId());
        assertEquals("testuser", principal.getUsername());
        assertTrue(principal.getRoles().contains("ADMIN"));
        assertTrue(principal.getRoles().contains("USER"));
    }

    @Test
    @DisplayName("Token hợp lệ - authorities có prefix ROLE_")
    void doFilter_validToken_authoritiesHaveRolePrefix() throws Exception {
        request.addHeader("Authorization", "Bearer valid.jwt.token");

        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid.jwt.token")).thenReturn("testuser");
        when(jwtUtil.getUserIdFromToken("valid.jwt.token")).thenReturn(1L);
        when(jwtUtil.getRolesFromToken("valid.jwt.token")).thenReturn(List.of("ADMIN"));

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Không có Authorization header - không set auth")
    void doFilter_noAuthorizationHeader_continuesWithoutAuth() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Token không hợp lệ - không set auth")
    void doFilter_invalidToken_continuesWithoutAuth() throws Exception {
        request.addHeader("Authorization", "Bearer invalid.token");

        when(jwtUtil.validateToken("invalid.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Header không có prefix Bearer - không set auth")
    void doFilter_noBearerPrefix_continuesWithoutAuth() throws Exception {
        request.addHeader("Authorization", "Basic sometoken");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Bearer token rỗng - không set auth")
    void doFilter_emptyBearerToken_continuesWithoutAuth() throws Exception {
        request.addHeader("Authorization", "Bearer ");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
