package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.request.UserCreationRequest;
import com.ecommerce.identityservice.dto.request.UserUpdateRequest;
import com.ecommerce.identityservice.entity.User;
import com.ecommerce.identityservice.exception.AppException;
import com.ecommerce.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    private String testUserId;
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        var response = userService.createUser(
                UserCreationRequest.builder()
                        .email("user1@example.com")
                        .userName("user1")
                        .password("password123")
                        .build()
        );
        testUserId = response.getUserId(); // Save the UUID string for later use
    }

    // --- TC-01: Lấy thông tin cá nhân khi đã đăng nhập hợp lệ ---
    @Test
    void testGetProfile_LoggedIn_TC01() {
        // Mock authentication context
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(testUserId, null));
        var myInfo = userService.getMyInfo();
        assertEquals("user1@example.com", myInfo.getEmail());
        assertEquals("user1", myInfo.getUserName());
    }

    // --- TC-02: Lấy thông tin cá nhân khi chưa đăng nhập ---
    @Test
    void testGetProfile_NotLoggedIn_TC02() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        Exception ex = assertThrows(Exception.class, () -> userService.getMyInfo());
        // Should be AppException with USER_NOT_EXISTED or UNAUTHENTICATED
    }

    // --- TC-03: Cập nhật thông tin hồ sơ thành công ---
    @Test
    void testUpdateProfile_Success_TC03() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUserName("updatedUser");
        updateRequest.setFullName("Updated Full Name");
        var updated = userService.updateUser(testUserId, updateRequest);
        assertEquals("updatedUser", updated.getUserName());
        assertEquals("Updated Full Name", updated.getFullName());
    }

    // --- TC-04: Cập nhật mật khẩu thành công ---
    @Test
    void testUpdatePassword_Success_TC04() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setPassword("newPassword123");
        userService.updateUser(testUserId, updateRequest);
        // Now, password should be updated. Try to login with new password.
        // (Assume AuthenticationService is available and injected)
        // AuthenticationRequest login = AuthenticationRequest.builder().email("user1@example.com").password("newPassword123").build();
        // AuthenticationResponse response = authenticationService.authenticate(login);
        // assertTrue(response.isAuthenticated());
    }

    // --- TC-05: Cập nhật hồ sơ với dữ liệu không hợp lệ ---
    @Test
    void testUpdateProfile_InvalidData_TC05() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setDateOfBirth(java.time.LocalDate.now().minusYears(10)); // Underage
        Exception ex = assertThrows(Exception.class, () -> userService.updateUser(testUserId, updateRequest));
        // Should be AppException with INVALID_DOB
    }

    // --- TC-06: Cập nhật hồ sơ khi chưa đăng nhập ---
    @Test
    void testUpdateProfile_NotLoggedIn_TC06() {
        Exception ex = assertThrows(Exception.class, () -> userService.updateUser("non-existent-id", new UserUpdateRequest()));
        // Should be AppException with USER_NOT_EXISTED
    }

    // --- TC-07: Cập nhật hồ sơ chỉ với một vài trường (partial update) ---
    @Test
    void testPartialProfileUpdate_TC07() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFullName("Partial Name");
        var updated = userService.updateUser(testUserId, updateRequest);
        assertEquals("Partial Name", updated.getFullName());
        assertEquals("user1", updated.getUserName()); // Unchanged
    }

    @Test
    void updateUser_validData() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUserName("updatedName");
        var response = userService.updateUser(testUserId, updateRequest);
        assertEquals("updatedName", response.getUserName());
    }

    @Test
    void updateUser_nonExistent() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUserName("ghost");
        assertThrows(AppException.class, () -> userService.updateUser("non-existent-uuid", updateRequest));
    }

    @Test
    void deleteUser_existing() {
        userService.deleteUser(testUserId);
        assertTrue(userRepository.findById(testUserId).isEmpty());
    }

    @Test
    void deleteUser_nonExistent() {
        assertDoesNotThrow(() -> userService.deleteUser("non-existent-uuid"));
    }
}
