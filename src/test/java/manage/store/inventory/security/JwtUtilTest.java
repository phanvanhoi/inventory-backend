package manage.store.inventory.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import manage.store.inventory.entity.Role;
import manage.store.inventory.entity.User;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set @Value fields via reflection
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",
                "ThisIsATestSecretKeyThatIsLongEnoughForHmacSha256Algorithm!!");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L); // 24h

        Role adminRole = new Role();
        adminRole.setRoleId(1L);
        adminRole.setRoleName("ADMIN");

        Role userRole = new Role();
        userRole.setRoleId(2L);
        userRole.setRoleName("USER");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);
        testUser.setRoles(roles);
    }

    @Test
    @DisplayName("Tạo token thành công")
    void generateToken_success() {
        String token = jwtUtil.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT has 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Lấy username từ token")
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtUtil.generateToken(testUser);

        String username = jwtUtil.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Lấy userId từ token")
    void getUserIdFromToken_returnsCorrectUserId() {
        String token = jwtUtil.generateToken(testUser);

        Long userId = jwtUtil.getUserIdFromToken(token);

        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("Lấy roles từ token")
    void getRolesFromToken_returnsCorrectRoles() {
        String token = jwtUtil.generateToken(testUser);

        List<String> roles = jwtUtil.getRolesFromToken(token);

        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
    }

    @Test
    @DisplayName("Validate token hợp lệ")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(testUser);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Validate token hết hạn")
    void validateToken_expiredToken_returnsFalse() {
        // Set expiration to -1 (already expired)
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1000L);

        String token = jwtUtil.generateToken(testUser);

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Validate token sai format")
    void validateToken_malformedToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken("not.a.valid.jwt"));
    }

    @Test
    @DisplayName("Validate token null hoặc rỗng")
    void validateToken_nullOrEmpty_returnsFalse() {
        assertFalse(jwtUtil.validateToken(null));
        assertFalse(jwtUtil.validateToken(""));
    }
}
